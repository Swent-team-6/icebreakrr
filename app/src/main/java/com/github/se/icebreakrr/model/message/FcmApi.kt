package com.github.se.icebreakrr.model.message

import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
  @POST("/send") suspend fun sendMessage(@Body body: MeetingRequest)

  @POST("/broadcast") suspend fun broadcast(@Body body: MeetingRequest)
}
