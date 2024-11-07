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

class MeetingRequestViewModel(
    private val profilesViewModel: ProfilesViewModel,
    private val functions: FirebaseFunctions,
    private val ourUserId: String?,
    private val ourName: String?
) : ViewModel() {

  var meetingRequestState by mutableStateOf(MeetingRequest())

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
      // Subscribe to a topic, or retrieve FCM token if needed
      // Firebase.messaging.subscribeToTopic("allUsers").await()
      meetingRequestState = meetingRequestState.copy(senderUID = ourUserId ?: "null")
    }
  }

  fun onRemoteTokenChange(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
    profilesViewModel.getProfileByUid(ourUserId ?: "null")
    val currentProfile = profilesViewModel.selectedProfile.value
    if (currentProfile != null) {
      val updatedProfile = currentProfile.copy(fcmToken = newToken)
      profilesViewModel.updateProfile(updatedProfile)
    }
  }

  fun onMeetingRequestChange(newMessage: String) {
    meetingRequestState = meetingRequestState.copy(message = newMessage)
  }

  fun onSubmitMeetingRequest() {
    meetingRequestState = meetingRequestState.copy(isEnteringMessage = false)
  }

  // Send a message by calling the Firebase Function to send FCM message
  fun sendMessage() {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to meetingRequestState.senderUID,
              "body" to ourName + " : " + meetingRequestState.message,
              "picture" to meetingRequestState.picture,
              "location" to meetingRequestState.location)
      try {
        Log.d("CURRENT MESSAGE : ", ourName + " : " + meetingRequestState.message)
        val result =
            functions
                .getHttpsCallable("sendMessage") // Cloud Function name
                .call(data)
                .await()

        meetingRequestState = meetingRequestState.copy(message = "", picture = null)
      } catch (e: Exception) {
        Log.e("FIREBASE ERROR", "Error sending message", e)
      }
    }
  }
}
