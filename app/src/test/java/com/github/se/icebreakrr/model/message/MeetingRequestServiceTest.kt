package com.github.se.icebreakrr.model.message

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
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

  @Test
  fun testOnMessageReceived_withoutNotification() {
    // Arrange
    val remoteMessage = RemoteMessage.Builder("1").build() // No notification data

    // Act
    meetingRequestService.onMessageReceived(remoteMessage)

    // Assert that showNotification was never called
    verify(meetingRequestService, never()).showNotification(anyString(), anyString())
  }

  @Test
  fun testOnMessageReceived_withNotification() {
    // Arrange
    val remoteMessage = RemoteMessage.Builder("1").setMessageType("Notification").build()

    // Act
    meetingRequestService.onMessageReceived(remoteMessage)

    // Assert that showNotification was never called
    verify(meetingRequestService, never()).showNotification(anyString(), anyString())
  }
}
