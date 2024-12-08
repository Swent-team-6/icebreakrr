package com.github.se.icebreakrr.model.message

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.database.core.Context
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val SEND_MEETING_REQUEST = "sendMeetingRequest"
private const val SEND_MEETING_RESPONSE = "sendMeetingResponse"
private const val SEND_MEETING_CONFIRMATION = "sendMeetingConfirmation"
private const val SEND_MEETING_CANCELLATION = "sendMeetingCancellation"
private const val SEND_ENGAGEMENT_NOTIFICATION = "sendEngagementNotification"
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
  val uidTimerMap : MutableMap<String, UUID> = mutableMapOf()

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
      profilesViewModel.updateProfile(updatedProfile, {}, {})
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
   * Sets the message of the meeting confirmation
   *
   * @param targetToken: the FCM token of the target user
   * @param newLocation: the chosen location to send in the form "latitude, longitude"
   * @param newMessage : message sent when choosing location
   */
  fun setMeetingConfirmation(targetToken: String, newLocation: String, newMessage: String) {
    meetingConfirmationState =
        meetingConfirmationState.copy(
            targetToken = targetToken, message = newMessage, location = newLocation)
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

  /**
   * Send a meeting confirmation to the target user, by calling a Firebase Cloud Function
   *
   * @param onFailure: callback to propagate errors
   */
  fun sendMeetingConfirmation(onFailure: (Exception) -> Unit) {
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
        onFailure(e)
      }
    }
  }

  /** Send a meeting cancellation in the case of distance cancellation or time cancellation */
  fun sendMeetingCancellation(
      targetToken: String,
      cancellationReason: String,
      senderUID: String,
      senderName: String
  ) {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to targetToken,
              "senderUID" to senderUID,
              "senderName" to senderName,
              "message" to cancellationReason,
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
        profilesViewModel.updateProfile(updatedProfile, {}, {})
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
  fun removeFromMeetingRequestSent(receiverUID: String, onComplete: () -> Unit) {
    val currentMeetingRequestSent =
        profilesViewModel.selfProfile.value?.meetingRequestSent ?: listOf()
    if (currentMeetingRequestSent.contains(receiverUID)) {
      val updatedMeetingRequestSend = currentMeetingRequestSent.filter { it != receiverUID }
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(meetingRequestSent = updatedMeetingRequestSend)
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile, { onComplete() }, {})
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
    println("addToMeetingRequestInbox() called with senderUid: $senderUID and message: $message")
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    if (!currentMeetingRequestInbox.keys.contains(senderUID)) {
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(
              meetingRequestInbox = currentMeetingRequestInbox + (senderUID to message))
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile, { onComplete() }, {})
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
        profilesViewModel.updateProfile(updatedProfile, {}, {})
      } else {
        Log.e("INBOX MEETING REQUEST", "Removing the meeting request in our inbox list failed")
      }
    }
  }

  /**
   * function used to add a pending location. Called when someone accepts your meeting request
   *
   * @param newUid: uid of the user that accepted your request
   * @param onComplete: callback function to avoid race conditions
   */
  fun addPendingLocation(newUid: String, onComplete: () -> Unit) {
    profilesViewModel.addPendingLocation(newUid) { onComplete() }
  }

  /**
   * function that we need to call when two people have met
   *
   * @param uid : uid of the user you have met
   */
  fun removeChosenLocalisation(uid: String) {
    profilesViewModel.removeChosenLocalisation(uid)
  }

  /**
   * private functions called when we have set the meeting confirmation It fetches the chosen
   * locations from the database
   */
  private fun getChosenLocalisations() {
    profilesViewModel.getSelfProfile { profilesViewModel.getChosenLocationsUsers() }
  }

  /**
   * method called when you receive a meeting confirmation
   *
   * @param uid : uid of the user with whom you want to have a meeting
   * @param locAndMessage : variable that contains the uid of the other user, the message he sent
   *   you and the location he has chosen,
   * @param onFailure : callback to propagate errors
   */
  fun confirmMeetingLocation(
      uid: String,
      locAndMessage: Pair<String, Pair<Double, Double>>,
      onFailure: (Exception) -> Unit
  ) {
    profilesViewModel.confirmMeetingLocation(
        uid, locAndMessage, { getChosenLocalisations() }, { onFailure(it) })
  }

  /**
   * Refreshes the content of the inbox to have it available locally
   *
   * @param onComplete : callback function to remove racing conditions
   */
  fun updateInboxOfMessages(onComplete: () -> Unit) {
    profilesViewModel.getSelfProfile {
      profilesViewModel.getInboxOfSelfProfile {
        profilesViewModel.getMessageCancellationUsers { onComplete() }
      }
    }
  }

  /**
   * Computes the distance between the user and all his contacts and cancels the meeting requests if
   * the contact is too far away
   */
  fun meetingDistanceCancellation() {
    updateInboxOfMessages {
      val contactUsers = profilesViewModel.getCancellationMessageProfile()
      val usersInMessagingRange = profilesViewModel.messagingProfiles.value
      val contactUsersUid = contactUsers.map { it.uid }
      val usersInMessagingRangeUid = usersInMessagingRange.map { it.uid }
      val contactUserNotInRangeUid = contactUsersUid.filter { !usersInMessagingRangeUid.contains(it) }
      val contactUserNotInRange = contactUsers.filter { contactUserNotInRangeUid.contains(it.uid) }
      Log.d("USERS NOT IN RANGE", contactUserNotInRange.toString())
        contactUserNotInRange.forEach {
          removeFromMeetingRequestInbox(it.uid)
          removeFromMeetingRequestSent(it.uid) {}
          sendCancellationToBothUsers(targetUID = it.uid, targetToken = it.fcmToken ?: "", targetName = it.name, reason = CancellationType.DISTANCE)
      }
    }
  }

  fun startMeetingRequestTimer(uid: String, token: String, name: String, context: android.content.Context){
      val workManager = WorkManager.getInstance(context)
      val inputData = Data.Builder()
          .putString("TARGET_UID", uid)
          .putString("TARGET_TOKEN", token)
          .putString("TARGET_NAME", name)
          .build()

      val workRequest = OneTimeWorkRequestBuilder<MessagingTimeoutWorker>()
          .setInputData(inputData)
          .setInitialDelay(30, TimeUnit.SECONDS)
          .build()

      Log.d("START TIMER", workRequest.id.toString())
      uidTimerMap[uid] = workRequest.id
      workManager.enqueue(workRequest)
  }

  fun stopMeetingRequestTimer(uid: String, context: android.content.Context){
      val workManager = WorkManager.getInstance(context)
      val workId = uidTimerMap[uid]
      if (workId != null) {
          Log.d("STOP TIMER", workId.toString())
          workManager.cancelWorkById(workId)
      }
  }

  fun sendCancellationToBothUsers(targetUID : String, targetToken : String, targetName : String, reason: CancellationType){
      sendMeetingCancellation(targetToken, reason.toString(), senderUID, senderName)
      sendMeetingCancellation(senderToken, reason.toString(), targetUID , targetName)
  }
}
