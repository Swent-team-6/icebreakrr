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
import com.google.firebase.messaging.messaging
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class MeetingRequestViewModel(
    private val profilesViewModel: ProfilesViewModel,
    private val ourUserId: String?
) : ViewModel() {

  var meetingRequestState by mutableStateOf(MeetingRequest())
  var meetingResponseState by mutableStateOf(MeetingResponse())

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

  private val api: FcmApi =
      Retrofit.Builder()
          .baseUrl(
              "http://10.0.2.2:8080/") // Todo: use an other URL later, using dependency injection ?
          .addConverterFactory(MoshiConverterFactory.create())
          .build()
          .create()

  init {
    viewModelScope.launch { Firebase.messaging.subscribeToTopic("allUsers").await() }
  }

  fun onRemoteTokenChange(newToken: String) {
//    if (ourUserId != null) {
//      profilesViewModel.getProfileByUid(ourUserId)
//      val currentProfile = profilesViewModel.selectedProfile.value
//      if (currentProfile != null) {
//        val updatedProfile = currentProfile.copy(fcmToken = newToken)
//        profilesViewModel.updateProfile(updatedProfile)
//      }
//    }
      meetingRequestState = meetingRequestState.copy(targetToken = newToken)
  }

  fun onMeetingRequestChange(newMessage: String) {
    meetingRequestState =
        meetingRequestState.copy(
            message = newMessage,
        )
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

  fun sendMessage(isBroadcast: Boolean) {
    Log.d("SENDING MESSAGE", meetingRequestState.targetToken)
//      profilesViewModel.getProfileByUid(ourUserId)
//      val userName = profilesViewModel.selectedProfile.value?.name
      viewModelScope.launch {
        val messageDto =
            SendMessageDto(
                to = if (isBroadcast) null else meetingRequestState.targetToken,
                notification =
                    NotificationBody(
                        title = "Jan", body = "bien jouée mon sucre sucre en gimauve ! Reste plus que 53h de debug ;) "))
        try {
          if (isBroadcast) {
              Log.d("MESSAGE", "BROADCAST SENT")
            api.broadcast(messageDto)
              Log.d("MESSAGE", "BROADCAST RETURNED")
          } else {
              Log.d("MESSAGE", "NOTIFICATION SENT")
            api.sendMessage(messageDto)
              Log.d("MESSAGE", "NOTIFICATION RETURNED")
          }
          meetingRequestState = meetingRequestState.copy(message = "", picture = null)
        } catch (e: HttpException) {
            Log.e("API ERROR", "HTTP exception: ${e.code()}", e)
        } catch (e: IOException) {
            Log.e("API ERROR", "Network error", e)
        } catch (e: Exception) {
            Log.e("API ERROR", "Unexpected error", e)
        }
      }
  }
}
