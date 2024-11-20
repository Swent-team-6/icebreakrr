package com.github.se.icebreakrr.model.message

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

  private val SEND_MESSAGE_FUNCTION_NAME = "sendMessage"
  private val SEND_MEETING_REQUEST = "sendMeetingRequest"

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

  /**
   * Sets the good token for our message, locally and in the profile's backend
   *
   * @param newToken the current token of the user to which we want to send the message
   */
  fun onRemoteTokenChange(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
    profilesViewModel.getProfileByUid(MeetingRequestManager.ourUid ?: "null")
    val currentProfile = profilesViewModel.selectedProfile.value
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


  /** Sends the message from our user to the target user */
  fun sendMessage() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to meetingRequestState.senderUID,
              "body" to MeetingRequestManager.ourName + " : " + meetingRequestState.message,
              )
      try {
        val result =
            functions
                .getHttpsCallable(SEND_MESSAGE_FUNCTION_NAME) // Cloud Function name
                .call(data)
                .await()

        meetingRequestState = meetingRequestState.copy(message = "")
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  fun sendMeetingRequest() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to MeetingRequestManager.ourUid,
              "message" to MeetingRequestManager.ourName + " : " + meetingRequestState.message,
          )
      try {
        Log.d("SENDING MEETING REQUEST : ", data.toString())
        val result =
            functions
                .getHttpsCallable(SEND_MEETING_REQUEST) // Cloud Function name
                .call(data)
                .await()

        addToMeetingRequestSent(meetingRequestState)
        meetingRequestState = meetingRequestState.copy(message = "")
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }

  private fun addToMeetingRequestSent(m : MeetingRequest){
      val ourUid = MeetingRequestManager.ourUid ?: "null"
      profilesViewModel.getProfileByUidAndThen(ourUid){
          val currentMeetingRequestSent = profilesViewModel.selectedProfile.value?.meetingRequestSent ?: mapOf()
          val updatedProfile = profilesViewModel.selectedProfile.value?.copy(meetingRequestSent = currentMeetingRequestSent + (m.senderUID to m.message))
          if(updatedProfile != null) {
              profilesViewModel.updateProfile(updatedProfile)
          } else {
              Log.e("SENT MEETING REQUEST", "Adding the new meeting request to our sent list failed")
          }
      }
  }

   fun addToMeetingRequestInbox(m : MeetingRequest){
        val ourUid = MeetingRequestManager.ourUid ?: "null"
        profilesViewModel.getProfileByUidAndThen(ourUid){
            val currentMeetingRequestSent = profilesViewModel.selectedProfile.value?.meetingRequestInbox ?: mapOf()
            val updatedProfile = profilesViewModel.selectedProfile.value?.copy(meetingRequestInbox = currentMeetingRequestSent + (m.senderUID to m.message))
            if(updatedProfile != null) {
                profilesViewModel.updateProfile(updatedProfile)
            } else {
                Log.e("INBOX MEETING REQUEST", "Adding the new meeting request to our inbox list failed")
            }
        }
    }

}
