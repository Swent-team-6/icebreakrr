package com.github.se.icebreakrr.model.message

import android.app.NotificationManager
import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MeetingRequestServiceTest {

  private lateinit var meetingRequestService: MeetingRequestService

  @Mock private lateinit var mockNotificationManager: NotificationManager

  @Mock private lateinit var mockContext: Context

  @Before
  fun setup() {
    // Initialize MeetingRequestService with mocked NotificationManager
    meetingRequestService = MeetingRequestService()
    meetingRequestService = spy(meetingRequestService)
  }

  @Test fun testOnMessageReceived_withoutNotification() {}
}
