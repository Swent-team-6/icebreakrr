package com.github.se.icebreakrr.model.message

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingRequestViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var functions: FirebaseFunctions

  private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockMeetingRequestManager: MeetingRequestManager
  private val callableReference: HttpsCallableReference = mock()

  private val birthDate2005 =
      Timestamp(
          Calendar.getInstance()
              .apply {
                set(2005, Calendar.JANUARY, 6, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
              }
              .time)

  private val profile1 =
      Profile(
          uid = "1",
          name = "John Doe",
          gender = Gender.MEN,
          birthDate = birthDate2005,
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"),
          profilePictureUrl = "http://example.com/profile.jpg",
          fcmToken = "TokenUser1")

  private val profile2 =
      Profile(
          uid = "2",
          name = "Jane Smith",
          gender = Gender.WOMEN,
          birthDate = birthDate2005,
          catchPhrase = "Adventure awaits!",
          description = "Always looking for new experiences.",
          tags = listOf("adventurous", "outgoing"),
          profilePictureUrl = null,
          fcmToken = "TokenUser2")

  private val selectedProfileOurOwn = MutableStateFlow(profile1)

  private val meetingRequestState =
      MeetingRequest(
          targetToken = "TokenUser1",
          senderUID = "senderUid",
          message = "Hello!",
      )

  @Before
  fun setUp() {
    // Set up main dispatcher for coroutine testing
    Dispatchers.setMain(testDispatcher)

    // Mock the dependencies
    profilesViewModel = mock(ProfilesViewModel::class.java)
    functions = mock(FirebaseFunctions::class.java)
    mockMeetingRequestManager = mock(MeetingRequestManager::class.java)

    // Initialize the ViewModel with mocks
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, functions)
    `when`(functions.getHttpsCallable("sendMessage")).thenReturn(callableReference)
    `when`(functions.getHttpsCallable("sendMeetingRequest")).thenReturn(callableReference)
    `when`(mockMeetingRequestManager.ourName).thenReturn("John Doe")
    `when`(mockMeetingRequestManager.ourUid).thenReturn("1")
  }

  @Test
  fun onMeetingRequestChangeUpdatesMessage() = runBlocking {
    val message = "New Message"
    meetingRequestViewModel.onMeetingRequestChange(message)

    // Assert that the message was updated in the ViewModel state
    assert(meetingRequestViewModel.meetingRequestState.message == message)
  }

  @Test
  fun testOnRemoteTokenChange() = runBlocking {
    val newToken = "TokenUser3"
    val existingProfile = profile1
    `when`(profilesViewModel.selectedProfile).thenReturn(selectedProfileOurOwn)

    // Act: Call the method under test
    meetingRequestViewModel.onRemoteTokenChange(newToken)

    // Assert: Verify that meetingRequestState was updated
    assert(meetingRequestViewModel.meetingRequestState.targetToken == newToken)

    // Assert: Verify that getProfileByUid was called
    verify(profilesViewModel).getProfileByUid(any())

    // Assert: Verify that updateProfile was called with the updated profile
    val updatedProfile = existingProfile.copy(fcmToken = newToken)
    verify(profilesViewModel).updateProfile(updatedProfile)
  }

  @Test
  fun testOnLocalTokenChange() = runBlocking {
    val newToken = "TokenUser3"

    meetingRequestViewModel.onLocalTokenChange(newToken)

    // Assert: Verify that meetingRequestState was updated
    assert(meetingRequestViewModel.meetingRequestState.targetToken == newToken)
  }

  @Test
  fun testSendMeetingRequestCorrectData() = runBlocking {
    // Mock the callable function
    val callableMock: HttpsCallableResult = mock(HttpsCallableResult::class.java)
    val callableTask: Task<HttpsCallableResult> = Tasks.forResult(callableMock)
    val callable = mock<HttpsCallableReference>()
    val mockMeetingRequestManager = mock(MeetingRequestManager::class.java)

    // Setup the function mock to return the task when called
    `when`(callable.call(any())).thenReturn(callableTask) // This makes it chainable

    meetingRequestViewModel.meetingRequestState = meetingRequestState

    // Act: Call the method under test
    meetingRequestViewModel.sendMeetingRequest()

    // Assert: Verify that the cloud function was called with the correct data
    verify(functions).getHttpsCallable("sendMeetingRequest")

    // Capture the argument passed to `call`
    val argumentCaptor =
        ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Any>>
    verify(callableReference).call(argumentCaptor.capture())

    val capturedData = argumentCaptor.value as Map<String, Any>

    assert(capturedData["targetToken"] == meetingRequestState.targetToken)
    assert(capturedData["senderUID"] == meetingRequestState.senderUID)
    assert(
        capturedData["message"] ==
            mockMeetingRequestManager.ourName + " : " + meetingRequestState.message)
  }

  @Test
  fun testSendMeetingRequestErrorFound(): Unit = runBlocking {
    // Arrange: Create a task that throws an exception
    val callableTask: Task<HttpsCallableResult> =
        Tasks.forException(RuntimeException("Firebase Error"))

    // Mock the callable to return this task when `call` is invoked
    `when`(callableReference.call(any())).thenReturn(callableTask)

    // Act
    meetingRequestViewModel.sendMeetingRequest()

    // Verify: Ensure no unhandled exceptions and log output if needed
    verify(functions).getHttpsCallable("sendMeetingRequest")
    verify(callableReference).call(any())
  }

  @Test
  fun constructWrongMeetingRequestVM(): Unit = runBlocking {
    val meetingRequestViewModelFactory =
        MeetingRequestViewModel.Companion.Factory(profilesViewModel, functions)
    var exception: IllegalArgumentException = IllegalArgumentException("No message")
    try {
      meetingRequestViewModelFactory.create(ProfilesViewModel::class.java)
    } catch (e: IllegalArgumentException) {
      exception = e
    }

    assertEquals("Unknown ViewModel class", exception.message)
  }
}
