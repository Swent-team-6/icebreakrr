package com.github.se.icebreakrr.model.message

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MeetingRequestViewModel(
    private val profilesViewModel: ProfilesViewModel,
    private val ourUserId: String?
) : ViewModel() {

  var meetingRequestState by mutableStateOf(MeetingRequest())
  var meetingResponseState by mutableStateOf(MeetingResponse())
  private val functions = FirebaseFunctions.getInstance()

  companion object {
    class Factory(
        private val profilesViewModel: ProfilesViewModel,
        private val ourUserId: String?
    ) : ViewModelProvider.Factory {

      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingRequestViewModel::class.java)) {
          return MeetingRequestViewModel(profilesViewModel, ourUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
      }
    }
  }

  init {
    viewModelScope.launch {
      // Subscribe to a topic, or retrieve FCM token if needed
      Firebase.messaging.subscribeToTopic("allUsers").await()
    }
  }

  fun onRemoteTokenChange(newToken: String) {
    meetingRequestState = meetingRequestState.copy(targetToken = newToken)
  }

  fun onMeetingRequestChange(newMessage: String) {
    meetingRequestState = meetingRequestState.copy(message = newMessage)
  }

  fun onSubmitMeetingRequest() {
    meetingRequestState = meetingRequestState.copy(isEnteringMessage = false)
    Log.d("PRINT SEND TOKEN", meetingRequestState.targetToken)
  }

  fun onMeetingResponseChange(newMessage: String, newAnswer: Boolean) {
    meetingResponseState = meetingResponseState.copy(message = newMessage, accept = newAnswer)
  }

  fun onSubmitMeetingResponse() {
    meetingResponseState = meetingResponseState.copy(isEnteringMessage = false)
  }

  // Send a message by calling the Firebase Function to send FCM message

  fun sendMessage(isBroadcast: Boolean) {
    viewModelScope.launch {
      val data =
          hashMapOf(
              "targetToken" to meetingRequestState.targetToken,
              "senderUID" to meetingRequestState.senderUID,
              "body" to meetingRequestState.message,
              "picture" to meetingRequestState.picture,
              "location" to meetingRequestState.location)
      try {
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
