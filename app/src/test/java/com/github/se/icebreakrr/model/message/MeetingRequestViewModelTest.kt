package com.github.se.icebreakrr.model.message

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingRequestViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var functions: FirebaseFunctions
  private lateinit var profilesRepository: ProfilesRepository
  private lateinit var ppRepository: ProfilePicRepository
  private lateinit var mockUser: FirebaseUser
  private lateinit var auth: FirebaseAuth
  private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
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
          fcmToken = "TokenUser1",
          geohash = "LocationProfile1")

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
          targetToken = profile2.fcmToken ?: "null",
          message = "Hello!",
      )

  private val meetingResponseState =
      MeetingResponse(
          targetToken = profile1.fcmToken ?: "null", message = "Hey dude", accepted = true)
  private val meetingConfirmationState =
      MeetingConfirmation(
          targetToken = profile1.fcmToken ?: "null",
          message = "Meet me here !",
          location = profile1.geohash ?: "null")

  private val meetingCancellationState =
      MeetingCancellation(
          targetToken = profile1.fcmToken ?: "null",
          message = MeetingRequestViewModel.CancellationType.DISTANCE.toString(),
          nameTargetUser = profile1.name)

  @Before
  fun setUp() {
    // Set up main dispatcher for coroutine testing
    Dispatchers.setMain(testDispatcher)

    // Mock the dependencies
    profilesRepository = mock(ProfilesRepository::class.java)
    ppRepository = mock(ProfilePicRepository::class.java)
    functions = mock(FirebaseFunctions::class.java)
    mockUser = mock(FirebaseUser::class.java)
    auth = mock(FirebaseAuth::class.java)

    // Initialize the ViewModels with mocks
    profilesViewModel = ProfilesViewModel(profilesRepository, ppRepository, auth)
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, functions)
    `when`(functions.getHttpsCallable("sendMeetingRequest")).thenReturn(callableReference)
    `when`(functions.getHttpsCallable("sendMeetingResponse")).thenReturn(callableReference)
    `when`(functions.getHttpsCallable("sendMeetingConfirmation")).thenReturn(callableReference)
    `when`(functions.getHttpsCallable("sendMeetingCancellation")).thenReturn(callableReference)
    `when`(functions.getHttpsCallable("sendEngagementNotification")).thenReturn(callableReference)
    `when`(auth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("1")
  }

  @Test
  fun setInitialValuesTest() = runBlocking {
    val ourUid = profile1.uid
    val ourToken = profile1.fcmToken ?: "null"
    val ourName = profile1.name

    meetingRequestViewModel.setInitialValues(
        senderToken = ourToken, senderUID = ourUid, senderName = ourName)

    // Assert that the message was updated in the ViewModel state
    assert(meetingRequestViewModel.senderName == ourName)
    assert(meetingRequestViewModel.senderToken == ourToken)
    assert(meetingRequestViewModel.senderUID == ourUid)
  }

  @Test
  fun onMeetingRequestChangeUpdatesMessageTest() = runBlocking {
    val message = "New Message"
    meetingRequestViewModel.onMeetingRequestChange(message)

    // Assert that the message was updated in the ViewModel state
    assert(meetingRequestViewModel.meetingRequestState.message == message)
  }

  @Test
  fun onMeetingResponseSetTest() = runBlocking {
    val message = "New Message"
    meetingRequestViewModel.setMeetingResponse(profile2.fcmToken ?: "null", message, true)
    assert(meetingRequestViewModel.meetingResponseState.message == message)
    assert(meetingRequestViewModel.meetingResponseState.accepted)
    assert(
        (meetingRequestViewModel.meetingResponseState.targetToken) == (profile2.fcmToken ?: "null"))
  }

  @Test
  fun onMeetingConfirmationSetTest() = runBlocking {
    val message = "New Message"
    val newLocation = "6.35838, 46.28495"
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }
    profilesViewModel.getSelfProfile {}
    meetingRequestViewModel.setMeetingConfirmation(
        targetToken = profile2.fcmToken ?: "null", newLocation = newLocation, newMessage = message)
    assert(meetingRequestViewModel.meetingConfirmationState.message == message)
    assert(
        (meetingRequestViewModel.meetingConfirmationState.targetToken) ==
            (profile2.fcmToken ?: "null"))
    assert(newLocation == meetingRequestViewModel.meetingConfirmationState.location)
  }

  @Test
  fun testOnRemoteTokenChange() = runBlocking {
    val newToken = "TokenUser3"
    val existingProfile = profile1
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }
    profilesViewModel.getSelfProfile {}

    meetingRequestViewModel.onRemoteTokenChange(newToken)

    assert(meetingRequestViewModel.meetingRequestState.targetToken == newToken)

    val updatedProfile = existingProfile.copy(fcmToken = newToken)
    verify(profilesRepository).updateProfile(eq(updatedProfile), any(), any())
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
    meetingRequestViewModel.setInitialValues(
        senderToken = profile1.fcmToken ?: "null",
        senderUID = profile1.uid,
        senderName = profile1.name)

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
    assert(capturedData["senderUID"] == meetingRequestViewModel.senderUID)
    assert(capturedData["message"] == meetingRequestState.message)
  }

  @Test
  fun testSendMeetingResponseCorrectData() = runBlocking {
    // Mock the callable function
    val callableMock: HttpsCallableResult = mock(HttpsCallableResult::class.java)
    val callableTask: Task<HttpsCallableResult> = Tasks.forResult(callableMock)
    val callable = mock<HttpsCallableReference>()
    meetingRequestViewModel.setInitialValues(
        senderToken = profile1.fcmToken ?: "null",
        senderUID = profile1.uid,
        senderName = profile1.name)

    `when`(callable.call(any())).thenReturn(callableTask) // This makes it chainable

    meetingRequestViewModel.meetingResponseState = meetingResponseState

    // Act: Call the method under test
    meetingRequestViewModel.sendMeetingResponse()
    // Assert: Verify that the cloud function was called with the correct data
    verify(functions).getHttpsCallable("sendMeetingResponse")

    // Capture the argument passed to `call`
    val argumentCaptor =
        ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Any>>
    verify(callableReference).call(argumentCaptor.capture())

    val capturedData = argumentCaptor.value as Map<String, Any>

    assert(capturedData["targetToken"] == meetingResponseState.targetToken)
    assert(capturedData["senderToken"] == profile1.fcmToken)
    assert(capturedData["senderUID"] == meetingRequestViewModel.senderUID)
    assert(capturedData["senderName"] == meetingRequestViewModel.senderName)
    assert(capturedData["accepted"] == "true")
    assert(capturedData["message"] == meetingResponseState.message)
  }

  @Test
  fun testSendMeetingConfirmationCorrectData() = runBlocking {
    // Mock the callable function
    val callableMock: HttpsCallableResult = mock(HttpsCallableResult::class.java)
    val callableTask: Task<HttpsCallableResult> = Tasks.forResult(callableMock)
    val callable = mock<HttpsCallableReference>()
    meetingRequestViewModel.setInitialValues(
        senderToken = profile1.fcmToken ?: "null",
        senderUID = profile1.uid,
        senderName = profile1.name)

    `when`(callable.call(any())).thenReturn(callableTask) // This makes it chainable

    meetingRequestViewModel.meetingConfirmationState = meetingConfirmationState

    // Act: Call the method under test
    meetingRequestViewModel.sendMeetingConfirmation {}
    // Assert: Verify that the cloud function was called with the correct data
    verify(functions).getHttpsCallable("sendMeetingConfirmation")

    // Capture the argument passed to `call`
    val argumentCaptor =
        ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Any>>
    verify(callableReference).call(argumentCaptor.capture())

    val capturedData = argumentCaptor.value as Map<String, Any>

    assert(capturedData["targetToken"] == meetingConfirmationState.targetToken)
    assert(capturedData["senderUID"] == meetingRequestViewModel.senderUID)
    assert(capturedData["senderName"] == meetingRequestViewModel.senderName)
    assert(capturedData["location"] == meetingConfirmationState.location)
    assert(capturedData["message"] == meetingConfirmationState.message)
  }

  @Test
  fun testSendMeetingCancellationCorrectData() = runBlocking {
    // Mock the callable function
    val callableMock: HttpsCallableResult = mock(HttpsCallableResult::class.java)
    val callableTask: Task<HttpsCallableResult> = Tasks.forResult(callableMock)
    val callable = mock<HttpsCallableReference>()
    meetingRequestViewModel.setInitialValues(
        senderToken = profile1.fcmToken ?: "null",
        senderUID = profile1.uid,
        senderName = profile1.name)

    `when`(callable.call(any())).thenReturn(callableTask) // This makes it chainable

    meetingRequestViewModel.meetingCancellationState = meetingCancellationState

    // Act: Call the method under test
    meetingRequestViewModel.sendMeetingCancellation(meetingCancellationState.targetToken, MeetingRequestViewModel.CancellationType.TIME, meetingCancellationState.senderUID, meetingCancellationState.nameTargetUser)
    // Assert: Verify that the cloud function was called with the correct data
    verify(functions).getHttpsCallable("sendMeetingCancellation")

    // Capture the argument passed to `call`
    val argumentCaptor =
        ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Any>>
    verify(callableReference).call(argumentCaptor.capture())

    val capturedData = argumentCaptor.value as Map<String, Any>

    assert(capturedData["targetToken"] == meetingCancellationState.targetToken)
    assert(capturedData["senderUID"] == profile1.uid)
    assert(capturedData["senderName"] == meetingCancellationState.nameTargetUser)
    assert(capturedData["message"] == MeetingRequestViewModel.CancellationType.TIME)
  }

  @Test
  fun testEngagementNotificationCorrectData() = runBlocking {
    // Mock the callable function
    val callableMock: HttpsCallableResult = mock(HttpsCallableResult::class.java)
    val callableTask: Task<HttpsCallableResult> = Tasks.forResult(callableMock)
    val callable = mock<HttpsCallableReference>()
    meetingRequestViewModel.setInitialValues(
        senderToken = profile1.fcmToken ?: "null",
        senderUID = profile1.uid,
        senderName = profile1.name)

    `when`(callable.call(any())).thenReturn(callableTask) // This makes it chainable

    // Act: Call the method under test
    val tag = "Football"
    meetingRequestViewModel.engagementNotification(profile2.fcmToken ?: "null", tag)
    // Assert: Verify that the cloud function was called with the correct data
    verify(functions).getHttpsCallable("sendEngagementNotification")

    // Capture the argument passed to `call`
    val argumentCaptor =
        ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Any>>
    verify(callableReference).call(argumentCaptor.capture())

    val capturedData = argumentCaptor.value as Map<String, Any>

    assert(capturedData["targetToken"] == profile2.fcmToken)
    assert(capturedData["senderUID"] == meetingRequestViewModel.senderUID)
    assert(capturedData["senderName"] == meetingRequestViewModel.senderName)
    assert(capturedData["message"] == tag)
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

  @Test
  fun addMeetingRequestSentTest(): Unit = runBlocking {
    val updatedProfile1 = profile1.copy(meetingRequestSent = listOf("2"))
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }
    profilesViewModel.getSelfProfile {}
    meetingRequestViewModel.addToMeetingRequestSent(profile2.uid)
    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
    verify(profilesRepository).updateProfile(eq(updatedProfile1), any(), any())
  }

  @Test
  fun removeMeetingRequestSentTest(): Unit = runBlocking {
    val updatedProfile1 = profile1.copy(meetingRequestSent = listOf("2"))
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(updatedProfile1)
    }
    profilesViewModel.getSelfProfile {}
    meetingRequestViewModel.removeFromMeetingRequestSent("2") {}
    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
    verify(profilesRepository).updateProfile(eq(profile1), any(), any())
  }

  @Test
  fun addMeetingRequestInboxTest(): Unit = runBlocking {
    val updatedProfile1 = profile1.copy(meetingRequestInbox = mapOf("2" to "hello"))
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }
    profilesViewModel.getSelfProfile {}
    meetingRequestViewModel.addToMeetingRequestInbox("2", "hello") {}
    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
    verify(profilesRepository).updateProfile(eq(updatedProfile1), any(), any())
  }

  @Test
  fun removeMeetingRequestInboxTest(): Unit = runBlocking {
    val updatedProfile1 = profile1.copy(meetingRequestInbox = mapOf("2" to "hello"))
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(updatedProfile1)
    }
    profilesViewModel.getSelfProfile {}
    meetingRequestViewModel.removeFromMeetingRequestInbox("2")
    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
    verify(profilesRepository).updateProfile(eq(profile1), any(), any())
  }

  @Test
  fun updateInboxOfMessagesTest() {
    `when`(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }
    meetingRequestViewModel.updateInboxOfMessages {}
    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
  }
}
