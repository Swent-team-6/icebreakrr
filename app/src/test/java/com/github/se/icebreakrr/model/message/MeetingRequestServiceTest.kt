package com.github.se.icebreakrr.model.message

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import androidx.core.app.NotificationCompat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MeetingRequestServiceTest {

  private lateinit var meetingRequestService: MeetingRequestService
  private lateinit var mockNotificationManager: NotificationManager
  private lateinit var mockMeetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockContext: Context
  private lateinit var mockResources: Resources
  lateinit var mockBuilder: NotificationCompat.Builder

  @Before
  fun setup() {
    meetingRequestService = spy(MeetingRequestService())
    mockNotificationManager = mock(NotificationManager::class.java)
    mockMeetingRequestViewModel = mock(MeetingRequestViewModel::class.java)
    mockContext = mock(Context::class.java)
    mockResources = mock(Resources::class.java)
    mockBuilder = mock(NotificationCompat.Builder::class.java)
    doReturn(mockNotificationManager)
        .`when`(meetingRequestService)
        .getSystemService(Context.NOTIFICATION_SERVICE)
    doReturn(mockBuilder)
        .`when`(meetingRequestService)
        .createNotificationBuilder(anyString(), anyString())

    // Inject the mock ViewModel
    MeetingRequestManager.meetingRequestViewModel = mockMeetingRequestViewModel
  }

  @Test
  fun onMessageReceivedMeetingRequestTest() {
    //    // Create mock RemoteMessage
    //    val mockRemoteMessage = mock(RemoteMessage::class.java)
    //    val data: Map<String, String> =
    //        mapOf("title" to "MEETING REQUEST", "senderUID" to "1", "message" to "hello")
    //
    //    `when`(mockRemoteMessage.data).thenReturn(data)
    //
    //    // Simulate onMessageReceived
    //    meetingRequestService.onMessageReceived(mockRemoteMessage)
    //
    //    // Verify that ViewModel methods were called
    //    verify(mockMeetingRequestViewModel)?.addToMeetingRequestInbox("1", "hello")
    //    verify(mockMeetingRequestViewModel)?.updateInboxOfMessages()
  }

  @Test
  fun showNotificationTest() {
    // Arrange
    val title = "Test Title"
    val message = "Test Message"

    // Act
    meetingRequestService.showNotification(title, message)

    // Assert that the NotificationManager is called with correct parameters
    verify(mockNotificationManager).notify(eq(0), any())
  }
}
