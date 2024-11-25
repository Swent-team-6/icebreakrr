package com.github.se.icebreakrr.model.message

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

  private val SEND_MEETING_REQUEST = "sendMeetingRequest"
  private val SEND_MEETING_RESPONSE = "sendMeetingResponse"
  private val SEND_MEETING_CONFIRMATION = "sendMeetingConfirmation"

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

  init {
    viewModelScope.launch {}
  }

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
      profilesViewModel.updateProfile(updatedProfile)
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

  fun setMeetingResponse(targetToken: String, newMessage: String, accepted: Boolean) {
    meetingResponseState =
        meetingResponseState.copy(
            targetToken = targetToken, message = newMessage, accepted = accepted)
  }

  fun setMeetingConfirmation(targetToken: String, newMessage: String) {
    val location = profilesViewModel.getSelfGeoHash()
    if (location != null) {
      meetingConfirmationState =
          meetingConfirmationState.copy(
              targetToken = targetToken, message = newMessage, location = location)
    } else {
      Log.e("MEETING CONFIRMATION", "geohash null for sender profile")
    }
  }

  fun sendMeetingRequest() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to senderUID,
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

  fun addToMeetingRequestSent(receiverUID: String) {
    val currentMeetingRequestSent =
        profilesViewModel.selfProfile.value?.meetingRequestSent ?: listOf()
    val updatedProfile =
        profilesViewModel.selfProfile.value?.copy(
            meetingRequestSent = currentMeetingRequestSent + receiverUID)
    if (updatedProfile != null) {
      profilesViewModel.updateProfile(updatedProfile)
    } else {
      Log.e("SENT MEETING REQUEST", "Adding the new meeting request to our sent list failed")
    }
  }

  fun removeFromMeetingRequestSent(receiverUID: String) {
    val currentMeetingRequestSent =
        profilesViewModel.selfProfile.value?.meetingRequestSent ?: listOf()
    val updatedMeetingRequestSend = currentMeetingRequestSent.filter { it != receiverUID }
    val updatedProfile =
        profilesViewModel.selfProfile.value?.copy(meetingRequestSent = updatedMeetingRequestSend)
    if (updatedProfile != null) {
      profilesViewModel.updateProfile(updatedProfile)
    } else {
      Log.e("SENT MEETING REQUEST", "Removing the meeting request of our sent list failed")
    }
  }

  fun addToMeetingRequestInbox(senderUID: String, message: String) {
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    val updatedProfile =
        profilesViewModel.selfProfile.value?.copy(
            meetingRequestInbox = currentMeetingRequestInbox + (senderUID to message))
    if (updatedProfile != null) {
      profilesViewModel.updateProfile(updatedProfile)
    } else {
      Log.e("INBOX MEETING REQUEST", "Adding the new meeting request to our inbox list failed")
    }
  }

  fun removeFromMeetingRequestInbox(senderUID: String) {
    val currentMeetingRequestInbox =
        profilesViewModel.selfProfile.value?.meetingRequestInbox ?: mapOf()
    val updatedMeetingRequestInbox = currentMeetingRequestInbox.filterKeys { it != senderUID }
    val updatedProfile =
        profilesViewModel.selfProfile.value?.copy(meetingRequestInbox = updatedMeetingRequestInbox)
    if (updatedProfile != null) {
      profilesViewModel.updateProfile(updatedProfile)
    } else {
      Log.e("INBOX MEETING REQUEST", "Removing the meeting request in our inbox list failed")
    }
  }
}
