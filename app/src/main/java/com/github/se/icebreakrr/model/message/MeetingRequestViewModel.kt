package com.github.se.icebreakrr.model.message

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class MeetingRequestViewModel : ViewModel() {
  var state by mutableStateOf(MeetingRequest())
    private set

  private val api: FcmApi =
      Retrofit.Builder()
          .baseUrl("http://10.0.2.2:8080/") // Todo: use an other URL later
          .addConverterFactory(MoshiConverterFactory.create())
          .build()
          .create()

  fun onRemoteTokenChange(newToken: String) {
    // ToDo : add the system to change the fcm token stored in the profile of the user
  }
}
