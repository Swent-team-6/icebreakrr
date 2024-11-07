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
          picture = null,
          location = null)

  @Before
  fun setUp() {
    // Set up main dispatcher for coroutine testing
    Dispatchers.setMain(testDispatcher)

    // Mock the dependencies
    profilesViewModel = mock(ProfilesViewModel::class.java)
    functions = mock(FirebaseFunctions::class.java)

    // Initialize the ViewModel with mocks
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, functions, "1", "John Doe")
    `when`(functions.getHttpsCallable("sendMessage")).thenReturn(callableReference)
  }

  @Test
  fun onMeetingRequestChangeUpdatesMessage() = runBlocking {
    val message = "New Message"
    meetingRequestViewModel.onMeetingRequestChange(message)

    // Assert that the message was updated in the ViewModel state
    assert(meetingRequestViewModel.meetingRequestState.message == message)
  }

  @Test
  fun testOnSubmitMeetingRequestUpdatesIsEnteringMessage() = runBlocking {
    meetingRequestViewModel.onSubmitMeetingRequest()

    // Assert that isEnteringMessage is false after submitting
    assert(!meetingRequestViewModel.meetingRequestState.isEnteringMessage)
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
    verify(profilesViewModel).getProfileByUid(profile1.uid)

    // Assert: Verify that updateProfile was called with the updated profile
    val updatedProfile = existingProfile.copy(fcmToken = newToken)
    verify(profilesViewModel).updateProfile(updatedProfile)
  }

  @Test
  fun testSendMessageCorrectData() = runBlocking {
    // Mock the callable function
    val callableMock: HttpsCallableResult = mock(HttpsCallableResult::class.java)
    val callableTask: Task<HttpsCallableResult> = Tasks.forResult(callableMock)
    val callable = mock<HttpsCallableReference>()

    // Setup the function mock to return the task when called
    `when`(callable.call(any())).thenReturn(callableTask) // This makes it chainable

    meetingRequestViewModel.meetingRequestState = meetingRequestState

    // Act: Call the method under test
    meetingRequestViewModel.sendMessage()

    // Assert: Verify that the cloud function was called with the correct data
    verify(functions).getHttpsCallable("sendMessage")

    // Capture the argument passed to `call`
    val argumentCaptor =
        ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Any>>
    verify(callableReference).call(argumentCaptor.capture())

    val capturedData = argumentCaptor.value as Map<String, Any>
    assert(capturedData["targetToken"] == meetingRequestState.targetToken)
    assert(capturedData["senderUID"] == meetingRequestState.senderUID)
    assert(capturedData["body"] == "John Doe : " + meetingRequestState.message)
    assert(capturedData["picture"] == meetingRequestState.picture)
    assert(capturedData["location"] == meetingRequestState.location)
  }

  @Test
  fun testSendMessageErrorFound(): Unit = runBlocking {
    // Arrange: Create a task that throws an exception
    val callableTask: Task<HttpsCallableResult> =
        Tasks.forException(RuntimeException("Firebase Error"))

    // Mock the callable to return this task when `call` is invoked
    `when`(callableReference.call(any())).thenReturn(callableTask)

    // Act
    meetingRequestViewModel.sendMessage()

    // Verify: Ensure no unhandled exceptions and log output if needed
    verify(functions).getHttpsCallable("sendMessage")
    verify(callableReference).call(any())
  }
}