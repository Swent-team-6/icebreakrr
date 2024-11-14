package com.github.se.icebreakrr.model.message

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*
   Class that manages the interaction between messages, the Profile backend and the user of the app
*/
class MeetingRequestViewModel(
    private val profilesViewModel: ProfilesViewModel,
    private val functions: FirebaseFunctions,
    private val ourUserId: String?,
    private val ourName: String?
) : ViewModel() {

  var meetingRequestState by mutableStateOf(MeetingRequest())
  private val SEND_MESSAGE_FUNCTION_NAME = "sendMessage"

  private var name: String? = null
  private var userUID: String? = null

  companion object {
    class Factory(
        private val profilesViewModel: ProfilesViewModel,
        private val functions: FirebaseFunctions,
        private val ourUserId: String?,
        private val ourName: String?
    ) : ViewModelProvider.Factory {

      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingRequestViewModel::class.java)) {
          return MeetingRequestViewModel(profilesViewModel, functions, ourUserId, ourName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
      }
    }
  }

  init {
    viewModelScope.launch {
      meetingRequestState = meetingRequestState.copy(senderUID = ourUserId ?: "null")
    }
  }

  /**
   * Sets the good token for our message, locally and in the profile's backend
   *
   * @param newToken the current token of the user to which we want to send the message
   */
  fun onRemoteTokenChange(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
    profilesViewModel.getProfileByUid(ourUserId ?: "null")
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
  /** Sets the status of the message, shows that it is ready to be sent */
  fun onSubmitMeetingRequest() {
    meetingRequestState = meetingRequestState.copy(isEnteringMessage = false)
  }

  /** Sends the message from our user to the target user */
  fun sendMessage() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to meetingRequestState.senderUID,
              "body" to MeetingRequestManager.ourName + " : " + meetingRequestState.message,
              "picture" to meetingRequestState.picture,
              "location" to meetingRequestState.location)
      try {
        val result =
            functions
                .getHttpsCallable(SEND_MESSAGE_FUNCTION_NAME) // Cloud Function name
                .call(data)
                .await()

        meetingRequestState = meetingRequestState.copy(message = "", picture = null)
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }
}
