package com.github.se.icebreakrr.model.message

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
import com.google.firebase.functions.FirebaseFunctions
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val SEND_MEETING_REQUEST = "sendMeetingRequest"
private const val SEND_MEETING_RESPONSE = "sendMeetingResponse"
private const val SEND_MEETING_CANCELLATION = "sendMeetingCancellation"
private const val SEND_ENGAGEMENT_NOTIFICATION = "sendEngagementNotification"
private const val TIMER_TARGET_UID = "TARGET_UID"
private const val TIMER_TARGET_TOKEN = "TARGET_TOKEN"
private const val TIMER_TARGET_NAME = "TARGET_NAME"
private const val TIMEOUT_DELAY = 20L

/*
   Class that manages the interaction between messages, the Profile backend and the user of the app
*/

class MeetingRequestViewModel(
    private val profilesViewModel: ProfilesViewModel,
    private val functions: FirebaseFunctions,
) : ViewModel() {

  var meetingRequestState by mutableStateOf(MeetingRequest())
  var meetingResponseState by mutableStateOf(MeetingResponse())
  val uidTimerMap: MutableMap<String, UUID> = mutableMapOf()

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
    CLOSED("closed app"),
    CANCELLED("cancel")
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
  fun setTargetToken(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
  }
  /**
   * Sets the message of the meeting request
   *
   * @param newMessage the message we want to send
   */
  fun setMeetingRequestChangeMessage(message: String) {
    meetingRequestState = meetingRequestState.copy(message = message)
  }

  /**
   * Sets the second message of the meeting request
   *
   * @param message2 the message we want to send
   */
  fun setMeetingRequestChangeLocation(locationMessage: String, location: String) {
    meetingRequestState =
        meetingRequestState.copy(locationMessage = locationMessage, location = location)
  }

  /**
   * Sets the message of the meeting response
   *
   * @param targetToken: the FCM token of the target user
   * @param newMessage: the response message we want to send
   * @param accepted: the acceptation status of the meeting request
   * @param location: the location of the meeting request
   */
  fun setMeetingResponse(
      targetToken: String,
      newMessage: String,
      accepted: Boolean,
      location: String
  ) {
    meetingResponseState =
        meetingResponseState.copy(
            targetToken = targetToken,
            message = newMessage,
            accepted = accepted,
            location = location)
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
              "locationMessage" to meetingRequestState.locationMessage,
              "location" to meetingRequestState.location)
      try {
        val result = functions.getHttpsCallable(SEND_MEETING_REQUEST).call(data).await()
        meetingRequestState =
            meetingRequestState.copy(message = "", locationMessage = "", location = "")
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
              "location" to meetingResponseState.location,
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
   * Send a meeting cancellation in the case of distance cancellation or time cancellation
   *
   * @param targetToken: the fcm token of the target user
   * @param cancellationReason: the reason for the cancellation of the meeting request
   * @param senderUID: the uid of the sender
   * @param senderName: the name of the sender
   */
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
    onComplete()
  }

  /**
   * Adds to our inbox the sender uid and the message the sender sent to us
   *
   * @param senderUID: the uid of the sender
   * @param message1: the meeting request message
   * @param message2: the location message
   * @param location: the location of the meeting request
   * @param onComplete: the function called when the task has been completed
   */
  fun addToMeetingRequestInbox(
      senderUID: String,
      message1: String,
      message2: String,
      location: Pair<Double, Double>,
      onComplete: () -> Unit
  ) {
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    if (!currentMeetingRequestInbox.keys.contains(senderUID)) {
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(
              meetingRequestInbox =
                  currentMeetingRequestInbox + (senderUID to ((message1 to message2) to location)))
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
   * @param onComplete: task called after completion of the server side task
   */
  fun removeFromMeetingRequestInbox(senderUID: String, onComplete: () -> Unit) {
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    if (currentMeetingRequestInbox.keys.contains(senderUID)) {
      val updatedMeetingRequestInbox = currentMeetingRequestInbox.filterKeys { it != senderUID }
      val updatedProfile =
          profilesViewModel.selfProfile.value?.copy(
              meetingRequestInbox = updatedMeetingRequestInbox)
      if (updatedProfile != null) {
        profilesViewModel.updateProfile(updatedProfile, { onComplete() }, {})
      } else {
        Log.e("INBOX MEETING REQUEST", "Removing the meeting request in our inbox list failed")
      }
    }
    onComplete()
  }

  /**
   * function that we need to call when two people have met
   *
   * @param uid : uid of the user you have met
   */
  fun removeChosenLocalisation(uid: String, onComplete: () -> Unit) {
    profilesViewModel.removeChosenLocalisation(uid) { onComplete() }
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
  fun confirmMeetingRequest(
      uid: String,
      locAndMessage: Pair<String, Pair<Double, Double>>,
      onFailure: (Exception) -> Unit
  ) {
    profilesViewModel.confirmMeetingRequest(
        uid, locAndMessage, { getChosenLocalisations() }, { onFailure(it) })
  }

  /**
   * Refreshes the content of the inbox to have it available locally
   *
   * @param onComplete : callback function to remove racing conditions
   */
  fun updateInboxOfMessages(onComplete: () -> Unit) {
    Log.d("TESTEST", "[updateInboxOfMessages] --------------")
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
      val usersInMessagingRange = profilesViewModel.getUsersInMessagingRange()
      val contactUsersUid = contactUsers.map { it.uid }
      val usersInMessagingRangeUid = usersInMessagingRange.map { it.uid }
      val contactUserNotInRangeUid =
          contactUsersUid.filter { !usersInMessagingRangeUid.contains(it) }
      val contactUserNotInRange = contactUsers.filter { contactUserNotInRangeUid.contains(it.uid) }
      contactUserNotInRange.forEach {
        removeFromMeetingRequestSent(it.uid) {
          removeFromMeetingRequestInbox(it.uid) {
            sendCancellationToBothUsers(
                targetUID = it.uid,
                targetToken = it.fcmToken ?: "",
                targetName = it.name,
                reason = CancellationType.DISTANCE)
          }
        }
      }
    }
  }

  /**
   * Starts a timer to timeout the meeting request in case it takes too long
   *
   * @param uid: the uid of the user we are sending the meeting request
   * @param token: the fcm token of the target user
   * @param name: the name of the target user
   * @param context: the current context
   */
  fun startMeetingRequestTimer(
      uid: String,
      token: String,
      name: String,
      context: android.content.Context
  ) {
    val workManager = WorkManager.getInstance(context)
    val inputData =
        Data.Builder()
            .putString(TIMER_TARGET_UID, uid)
            .putString(TIMER_TARGET_TOKEN, token)
            .putString(TIMER_TARGET_NAME, name)
            .build()

    val workRequest =
        OneTimeWorkRequestBuilder<MessagingTimeoutWorker>()
            .setInputData(inputData)
            .setInitialDelay(TIMEOUT_DELAY, TimeUnit.MINUTES)
            .build()

    uidTimerMap[uid] = workRequest.id
    workManager.enqueue(workRequest)
  }
  /**
   * Stops the timer to timeout the meeting request
   *
   * @param uid: the uid of the user we sent the meeting request
   * @param context: the current context
   */
  fun stopMeetingRequestTimer(uid: String, context: android.content.Context) {
    val workManager = WorkManager.getInstance(context)
    val workId = uidTimerMap[uid]
    if (workId != null) {
      workManager.cancelWorkById(workId)
    }
  }

  /**
   * Sends the meeting cancellation to both users for the given reason
   *
   * @param targetToken: the fcm token of the target user
   * @param targetUID: the uid of the target user
   * @param targetName: the uid of the target name
   * @param reason: the reason of the meeting cancellation
   */
  fun sendCancellationToBothUsers(
      targetUID: String,
      targetToken: String,
      targetName: String,
      reason: CancellationType
  ) {
    sendMeetingCancellation(targetToken, reason.toString(), senderUID, senderName)
    sendMeetingCancellation(senderToken, reason.toString(), targetUID, targetName)
  }
}
