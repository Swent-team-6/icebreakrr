package com.github.se.icebreakrr.model.message

import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull

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
    // Create mock RemoteMessage
    val mockRemoteMessage = mock(RemoteMessage::class.java)
    val data: Map<String, String> =
        mapOf(
            "title" to "MEETING REQUEST",
            "senderUID" to "1",
            "message1" to "hello",
            "message2" to "am under the Rolex",
            "location" to "1.0, 2.0")
    `when`(mockRemoteMessage.data).thenReturn(data)
    assertNotNull(MeetingRequestManager.meetingRequestViewModel)
    doAnswer { invocation ->
          val onComplete: () -> Unit = invocation.getArgument(4)
          onComplete() // Trigger the completion manually
          null
        }
        .`when`(mockMeetingRequestViewModel)
        .addToMeetingRequestInbox(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

    `when`(meetingRequestService.isAppInForeground()).thenReturn(false)

    // Simulate onMessageReceived
    meetingRequestService.onMessageReceived(mockRemoteMessage)
    // Verify that ViewModel methods were called
    verify(mockMeetingRequestViewModel)
        ?.addToMeetingRequestInbox(
            senderUID = anyOrNull(),
            message1 = anyOrNull(),
            message2 = anyOrNull(),
            location = anyOrNull(),
            onComplete = anyOrNull())
    verify(mockMeetingRequestViewModel)?.updateInboxOfMessages(anyOrNull())
    verify(meetingRequestService).showNotification(anyOrNull(), anyOrNull())
  }

  @Test
  fun onMessageReceivedMeetingResponseTest() {
    // Create mock RemoteMessage
    val mockRemoteMessage = mock(RemoteMessage::class.java)
    val data: Map<String, String> =
        mapOf(
            "title" to "MEETING RESPONSE",
            "senderUID" to "1",
            "message" to "hello",
            "accepted" to "true",
            "location" to "1.0, 2.0")
    `when`(mockRemoteMessage.data).thenReturn(data)
    assertNotNull(MeetingRequestManager.meetingRequestViewModel)
    doAnswer { invocation ->
          val onComplete: () -> Unit = invocation.getArgument(1)
          onComplete() // Trigger the completion manually
          null
        }
        .`when`(mockMeetingRequestViewModel)
        .removeFromMeetingRequestSent(anyOrNull(), anyOrNull())

    `when`(meetingRequestService.isAppInForeground()).thenReturn(false)

    // Simulate onMessageReceived
    meetingRequestService.onMessageReceived(mockRemoteMessage)
    // Verify that ViewModel methods were called
    verify(mockMeetingRequestViewModel)?.removeFromMeetingRequestSent(anyOrNull(), anyOrNull())
    verify(mockMeetingRequestViewModel)?.updateInboxOfMessages(anyOrNull())
    verify(meetingRequestService).showNotification(anyOrNull(), anyOrNull())
  }

  @Test
  fun onMessageReceivedMeetingCancellationTest() {
    // Create mock RemoteMessage
    val mockRemoteMessage = mock(RemoteMessage::class.java)
    val data: Map<String, String> =
        mapOf(
            "title" to "MEETING CANCELLATION",
            "senderUID" to "1",
            "message" to MeetingRequestViewModel.CancellationType.CANCELLED.toString(),
            "senderName" to "John Doe")
    `when`(mockRemoteMessage.data).thenReturn(data)
    assertNotNull(MeetingRequestManager.meetingRequestViewModel)

    // Simulate onMessageReceived
    meetingRequestService.onMessageReceived(mockRemoteMessage)
    // Verify that ViewModel methods were called
    verify(mockMeetingRequestViewModel)?.removeFromMeetingRequestSent(anyOrNull(), anyOrNull())
  }

  @Test
  fun onMessageReceivedEngagementNotificationTest() {
    // Create mock RemoteMessage
    val mockRemoteMessage = mock(RemoteMessage::class.java)
    val data: Map<String, String> =
        mapOf(
            "title" to "ENGAGEMENT NOTIFICATION",
            "senderUID" to "1",
            "message" to "hello",
            "senderName" to "John Doe")
    `when`(mockRemoteMessage.data).thenReturn(data)
    assertNotNull(MeetingRequestManager.meetingRequestViewModel)

    // Simulate onMessageReceived
    meetingRequestService.onMessageReceived(mockRemoteMessage)
    // Verify that ViewModel methods were called
    verify(meetingRequestService).showNotification(anyOrNull(), anyOrNull())
  }

  @Test
  fun showNotificationTest() {
    // Arrange
    val title = "Test Title"
    val message = "Test Message"

    // Act
    meetingRequestService.showNotification(title, message)

    // Assert that the NotificationManager is called with correct parameters
    // In the eq() one has to put the fallback notification ID
    verify(mockNotificationManager).notify(eq(2), any())
  }

  @Test
  fun onNewTokenTest() {
    val newToken = "newToken"
    meetingRequestService.onNewToken(newToken)
    verify(mockMeetingRequestViewModel).onRemoteTokenChange(anyOrNull())
  }
}
