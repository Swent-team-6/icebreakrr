package com.github.se.icebreakrr.model.message

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.functions.FirebaseFunctions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val SEND_MEETING_REQUEST = "sendMeetingRequest"
private const val SEND_MEETING_RESPONSE = "sendMeetingResponse"
private const val SEND_MEETING_CONFIRMATION = "sendMeetingConfirmation"
private const val SEND_MEETING_CANCELLATION = "sendMeetingCancellation"
private const val SEND_ENGAGEMENT_NOTIFICATION = "sendEngagementNotification"
private const val FIVE_HUNDRED_METERS_IN_KM = 0.5
private const val EARTH_RADIUS_IN_KM = 6371.0
/*
   Class that manages the interaction between messages, the Profile backend and the user of the app
*/

class MeetingRequestViewModel(
    private val profilesViewModel: ProfilesViewModel,
    private val functions: FirebaseFunctions,
) : ViewModel() {

  var meetingRequestState by mutableStateOf(MeetingRequest())
  var meetingResponseState by mutableStateOf(MeetingResponse())
  var meetingConfirmationState by mutableStateOf(MeetingConfirmation())
  var meetingCancellationState by mutableStateOf(MeetingCancellation())

  var senderToken = ""
  var senderUID = ""
  var senderName = ""

  companion object {
    class Factory(
        private val profilesViewModel: ProfilesViewModel,
        private val functions: FirebaseFunctions,
    ) : ViewModelProvider.Factory {

      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingRequestViewModel::class.java)) {
          return MeetingRequestViewModel(profilesViewModel, functions) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
      }
    }
  }

  enum class CancellationType(val reason: String) {
    DISTANCE("distance"),
    TIME("time"),
    BLOCKED("blocked"),
    REPORTED("reported")
  }
  /**
   * Set the initial values of the meeting VM that are needed to send messages
   *
   * @param senderToken: The FCM token of our user
   * @param senderUID: The uid of our user
   * @param senderName: The name of our user
   */
  fun setInitialValues(senderToken: String, senderUID: String, senderName: String) {
    this.senderToken = senderToken
    this.senderUID = senderUID
    this.senderName = senderName
  }

  /**
   * Sets the good token for our message, locally and in the profile's backend
   *
   * @param newToken the current token of the user to which we want to send the message
   */
  fun onRemoteTokenChange(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
    senderToken = newToken
    val currentProfile = profilesViewModel.getSelfProfileValue()
    if (currentProfile != null) {
      val updatedProfile = currentProfile.copy(fcmToken = newToken)
      profilesViewModel.updateProfile(updatedProfile) {}
    }
  }
  /**
   * Sets the good token for our message just locally
   *
   * @param newToken the current token of the user to which we want to send the message
   */
  fun onLocalTokenChange(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
  }
  /**
   * Sets the message of the meeting request
   *
   * @param newMessage the message we want to send
   */
  fun onMeetingRequestChange(newMessage: String) {
    meetingRequestState = meetingRequestState.copy(message = newMessage)
  }
  /**
   * Sets the message of the meeting response
   *
   * @param targetToken: the FCM token of the target user
   * @param newMessage: the response message we want to send
   * @param accepted: the acceptation status of the meeting request
   */
  fun setMeetingResponse(targetToken: String, newMessage: String, accepted: Boolean) {
    meetingResponseState =
        meetingResponseState.copy(
            targetToken = targetToken, message = newMessage, accepted = accepted)
  }

  /**
   * Sets the message of the meeting cancellation
   *
   * @param targetToken: the FCM token of the target user
   * @param cancellationReason: the reason for the cancellation of the meeting request
   * @param otherUserName: the name of the other user
   */
  fun setMeetingCancellation(
      targetToken: String,
      cancellationReason: CancellationType,
      otherUserName: String
  ) {
    meetingCancellationState =
        meetingCancellationState.copy(
            targetToken = targetToken,
            message = cancellationReason.toString(),
            nameTargetUser = otherUserName)
  }

  /**
   * Sets the message of the meeting confirmation
   *
   * @param targetToken: the FCM token of the target user
   * @param newLocation: the chosen location to send in the form "latitude, longitude"
   */
  fun setMeetingConfirmation(targetToken: String, newLocation: String) {
    meetingConfirmationState =
        meetingConfirmationState.copy(
            targetToken = targetToken,
            message = "meeting request successfull",
            location = newLocation)
  }

  /** Send a meeting request to the target user, by calling a Firebase Cloud Function */
  fun sendMeetingRequest() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to senderUID,
              "senderName" to senderName,
              "message" to meetingRequestState.message,
          )
      try {
        val result = functions.getHttpsCallable(SEND_MEETING_REQUEST).call(data).await()
        meetingRequestState = meetingRequestState.copy(message = "")
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  /** Send a meeting response to the target user, by calling a Firebase Cloud Function */
  fun sendMeetingResponse() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingResponseState.targetToken,
              "senderToken" to senderToken,
              "senderUID" to senderUID,
              "senderName" to senderName,
              "message" to meetingResponseState.message,
              "accepted" to meetingResponseState.accepted.toString())
      try {
        val result = functions.getHttpsCallable(SEND_MEETING_RESPONSE).call(data).await()
        meetingResponseState = meetingResponseState.copy(message = "")
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  /** Send a meeting confirmation to the target user, by calling a Firebase Cloud Function */
  fun sendMeetingConfirmation() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingConfirmationState.targetToken,
              "senderUID" to senderUID,
              "senderName" to senderName,
              "message" to meetingConfirmationState.message,
              "location" to meetingConfirmationState.location)
      try {
        val result = functions.getHttpsCallable(SEND_MEETING_CONFIRMATION).call(data).await()
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  /** Send a meeting cancellation in the case of distance cancellation or time cancellation */
  private fun sendMeetingCancellation() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingCancellationState.targetToken,
              "senderUID" to senderUID,
              "senderName" to meetingCancellationState.nameTargetUser,
              "message" to meetingCancellationState.message,
          )
      try {
        val result = functions.getHttpsCallable(SEND_MEETING_CANCELLATION).call(data).await()
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  /**
   * Send an engagement notification to make the use more engaged in the app and get news about the
   * people around him
   */
  fun engagementNotification(targetToken: String, tag: String) {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to targetToken,
              "senderUID" to senderUID,
              "senderName" to senderName,
              "message" to tag,
          )
      try {
        val result = functions.getHttpsCallable(SEND_ENGAGEMENT_NOTIFICATION).call(data).await()
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  /**
   * Adds to the meetingRequestSent list of our profile, the uid of the meeting request target
   * profile
   *
   * @param receiverUID: the uid of the target user
   */
  fun addToMeetingRequestSent(receiverUID: String) {
    val currentMeetingRequestSent =
        profilesViewModel.selfProfile.value?.meetingRequestSent ?: listOf()
    if (!currentMeetingRequestSent.contains(receiverUID)) {
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(
              meetingRequestSent = currentMeetingRequestSent + receiverUID)
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile) {}
      } else {
        Log.e("SENT MEETING REQUEST", "Adding the new meeting request to our sent list failed")
      }
    }
  }
  /**
   * Remove from the meetingRequestSent list of our profile, the uid of the meeting request target
   * profile
   *
   * @param receiverUID: the uid of the target user
   */
  fun removeFromMeetingRequestSent(receiverUID: String) {
    val currentMeetingRequestSent =
        profilesViewModel.selfProfile.value?.meetingRequestSent ?: listOf()
    if (currentMeetingRequestSent.contains(receiverUID)) {
      val updatedMeetingRequestSend = currentMeetingRequestSent.filter { it != receiverUID }
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(meetingRequestSent = updatedMeetingRequestSend)
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile) {}
      } else {
        Log.e("SENT MEETING REQUEST", "Removing the meeting request of our sent list failed")
      }
    }
  }

  /**
   * Adds to our inbox the sender uid and the message the sender sent to us
   *
   * @param senderUID: the uid of the sender
   * @param message: the received message
   */
  fun addToMeetingRequestInbox(senderUID: String, message: String, onComplete: () -> Unit) {
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    if (!currentMeetingRequestInbox.keys.contains(senderUID)) {
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(
              meetingRequestInbox = currentMeetingRequestInbox + (senderUID to message))
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile) { onComplete() }
      } else {
        Log.e("INBOX MEETING REQUEST", "Adding the new meeting request to our inbox list failed")
      }
    }
  }

  /**
   * Remove the message from the Inbox
   *
   * @param senderUID: the uid of the sender
   */
  fun removeFromMeetingRequestInbox(senderUID: String) {
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    if (currentMeetingRequestInbox.keys.contains(senderUID)) {
      val updatedMeetingRequestInbox = currentMeetingRequestInbox.filterKeys { it != senderUID }
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(
              meetingRequestInbox = updatedMeetingRequestInbox)
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile) {}
      } else {
        Log.e("INBOX MEETING REQUEST", "Removing the meeting request in our inbox list failed")
      }
    }
  }

  /** Refreshes the content of the inbox to have it available locally */
  fun updateInboxOfMessages(onComplete: () -> Unit) {
    profilesViewModel.getSelfProfile() {
      profilesViewModel.getInboxOfSelfProfile() {
        profilesViewModel.getMessageCancellationUsers() { onComplete() }
      }
    }
  }

  fun updateChosenLocalisations() {
    profilesViewModel.getSelfProfile() { profilesViewModel.getChosenLocations() }
  }

  fun addPendingLocation(newUid: String, onComplete: () -> Unit) {
    profilesViewModel.addPendingLocation(newUid) { onComplete() }
  }

  fun confirmMeetingLocation(uid: String, loc: Pair<Double, Double>) {
    profilesViewModel.confirmMeetingLocation(uid, loc)
  }

  fun removeChosenLocalisation(uid: String) {
    profilesViewModel.removeChosenLocalisation(uid)
  }

  /**
   * Computes the distance between the user and all his contacts and cancels the meeting requests if
   * the contact is too far away
   */
  fun meetingDistanceCancellation() {
    val selfProfile = profilesViewModel.selfProfile.value
    val originPoint = selfProfile?.location ?: GeoPoint(0.0, 0.0)
    updateInboxOfMessages() {
      val contactUsers = profilesViewModel.getCancellationMessageProfile()
      val distances =
          contactUsers.map {
            distanceBetweenGeoPoints(it.location ?: GeoPoint(0.0, 0.0), originPoint)
          }
      val mapUserDistance = contactUsers.zip(distances).toMap()
      mapUserDistance.forEach {
        if (it.value >= FIVE_HUNDRED_METERS_IN_KM) {
          removeFromMeetingRequestInbox(it.key.uid)
          removeFromMeetingRequestSent(it.key.uid)
          val targetToken = it.key.fcmToken ?: "null"
          val targetName = it.key.name
          setMeetingCancellation(
              targetToken, CancellationType.DISTANCE, targetName) // targeted to other user
          sendMeetingCancellation()
          setMeetingCancellation(senderToken, CancellationType.DISTANCE, it.key.name)
          sendMeetingCancellation()
        }
      }
    }
  }

  /**
   * Function computing the euclidean distance between two points
   *
   * @param point1: the first point
   * @param point2: the second point
   */
  private fun distanceBetweenGeoPoints(point1: GeoPoint, point2: GeoPoint): Double {
    val earthRadius = EARTH_RADIUS_IN_KM

    val lat1 = point1.latitude
    val lon1 = point1.longitude
    val lat2 = point2.latitude
    val lon2 = point2.longitude

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
  }
}
