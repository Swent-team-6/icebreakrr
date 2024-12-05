package com.github.se.icebreakrr.model.message

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
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

  @Before
  fun setup() {
    meetingRequestService = spy(MeetingRequestService())
    mockNotificationManager = mock(NotificationManager::class.java)
    mockMeetingRequestViewModel = mock(MeetingRequestViewModel::class.java)
    mockContext = mock(Context::class.java)
    mockResources = mock(Resources::class.java)

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
}
