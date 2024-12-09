package com.github.se.icebreakrr.model.profile

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import java.io.ByteArrayInputStream
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class ProfilesViewModelTest {
  private lateinit var context: Context
  private lateinit var profilesRepository: ProfilesRepository
  private lateinit var ppRepository: ProfilePicRepository
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfileViewModel: ProfilesViewModel
  private lateinit var mockAndThen: () -> Unit
  private lateinit var mockAuth: FirebaseAuth
  @OptIn(ExperimentalCoroutinesApi::class)
  private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

  private val birthDate2002 =
      Timestamp(
          Calendar.getInstance()
              .apply {
                set(2002, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
              }
              .time)

  private val profile1 =
      Profile(
          uid = "1",
          name = "John Doe",
          gender = Gender.MEN,
          birthDate = birthDate2002, // 22 years old
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"),
          profilePictureUrl = "http://example.com/profile.jpg",
          meetingRequestInbox = mapOf(),
          meetingRequestChosenLocalisation = mapOf(),
          meetingRequestSent = listOf())

  private val profile2 =
      Profile(
          uid = "2",
          name = "Jane Smith",
          gender = Gender.WOMEN,
          birthDate = birthDate2002,
          catchPhrase = "Adventure awaits!",
          description = "Always looking for new experiences.",
          tags = listOf("adventurous", "outgoing"),
          profilePictureUrl = null)

  private lateinit var bitmapFactoryMock: MockedStatic<BitmapFactory>
  private lateinit var bitmapMock: MockedStatic<Bitmap>

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    context = mock(Context::class.java)
    profilesRepository = mock(ProfilesRepository::class.java)
    ppRepository = mock(ProfilePicRepository::class.java)
    mockAuth = mock(FirebaseAuth::class.java)
    profilesViewModel = ProfilesViewModel(profilesRepository, ppRepository, mockAuth)
    mockProfileViewModel = mock(ProfilesViewModel::class.java)
    mockAndThen = mock()

    bitmapFactoryMock = mockStatic(BitmapFactory::class.java)
    bitmapMock = mockStatic(Bitmap::class.java)

    whenever(profilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    whenever(profilesRepository.waitingDone).thenReturn(MutableStateFlow(false))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
    bitmapFactoryMock.close()
    bitmapMock.close()
  }

  @Test
  fun getFilteredProfilesInRadiusCallsRepositoryWithFilters() = runBlocking {
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000

    val profilesList = listOf(profile1, profile2)
    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onSuccess = it.getArgument<(List<Profile>) -> Unit>(2)
          onSuccess(profilesList)
        }

    // Test filtering by a list of genders and tags
    profilesViewModel.getFilteredProfilesInRadius(
        center, radiusInMeters, listOf(Gender.MEN), 20..30, listOf("friendly"))

    verify(profilesRepository).getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any())
    assertThat(
        profilesViewModel.filteredProfiles.value.size, `is`(1)) // Should return only profile1
    assertThat(
        profilesViewModel.filteredProfiles.value[0].uid, `is`("1")) // profile1 should be returned
  }

  @Test
  fun getFilteredProfilesInRadiusWithMultipleGenders() = runBlocking {
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000

    val profilesList = listOf(profile1, profile2)
    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onSuccess = it.getArgument<(List<Profile>) -> Unit>(2)
          onSuccess(profilesList)
        }

    // Test filtering by a list of genders (both MEN and WOMEN)
    profilesViewModel.getFilteredProfilesInRadius(
        center, radiusInMeters, listOf(Gender.MEN, Gender.WOMEN), 20..30, null)

    verify(profilesRepository).getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any())
    assertThat(profilesViewModel.profiles.value.size, `is`(2)) // Should return both profiles
  }

  @Test
  fun getFilteredProfilesInRadiusHandlesError() = runBlocking {
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000
    val exception = Exception("Test exception")

    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
        }

    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  @Test
  fun getBlockedUsersInRadiusHandlesError() = runBlocking {
    val exception = Exception("Test exception")

    whenever(profilesRepository.getMultipleProfiles(eq(emptyList()), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    profilesViewModel.getBlockedUsers()

    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  @Test
  fun addNewProfileCallsRepository() = runBlocking {
    profilesViewModel.addNewProfile(profile1)

    verify(profilesRepository).addNewProfile(eq(profile1), any(), any())
  }

  @Test
  fun updateProfileCallsRepository() = runBlocking {
    profilesViewModel.updateProfile(profile1, {}) {}

    verify(profilesRepository).updateProfile(eq(profile1), any(), any())
  }

  @Test
  fun getProfileByUidCallsRepository() = runBlocking {
    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }

    profilesViewModel.getProfileByUid("1")

    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
    assertThat(profilesViewModel.selectedProfile.value?.uid, `is`("1"))
  }

  @Test
  fun getProfileByUidAndThenCallsRepository() = runBlocking {
    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }

    profilesViewModel.getProfileByUidAndThen("1", mockAndThen)

    verify(profilesRepository).getProfileByUid(eq("1"), any(), any())
    verify(mockAndThen).invoke()
    assertThat(profilesViewModel.selectedProfile.value?.uid, `is`("1"))
  }

  @Test
  fun deleteProfileByUidCallsRepository() = runBlocking {
    profilesViewModel.deleteProfileByUid("1")

    verify(profilesRepository).deleteProfileByUid(eq("1"), any(), any())
  }

  @Test
  fun addNewProfileHandlesError() = runBlocking {
    val exception = Exception("Failed to add profile")

    whenever(profilesRepository.addNewProfile(eq(profile1), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    profilesViewModel.addNewProfile(profile1)

    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  @Test
  fun updateProfileHandlesError() = runBlocking {
    val exception = Exception("Failed to update profile")

    whenever(profilesRepository.updateProfile(eq(profile1), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    profilesViewModel.updateProfile(profile1, {}) {}

    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  @Test
  fun getProfileByUidHandlesError() = runBlocking {
    val exception = Exception("Profile not found")

    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    profilesViewModel.getProfileByUid("1")

    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  @Test
  fun deleteProfileByUidHandlesError() = runBlocking {
    val exception = Exception("Failed to delete profile")

    whenever(profilesRepository.deleteProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    profilesViewModel.deleteProfileByUid("1")

    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  // Generated with the help of CursorAI
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun showsOfflineWhenTimerCompletesAndStillFailing() = runTest {

    // Mock repository states
    whenever(profilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    whenever(profilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

    // Initial state
    assertTrue(profilesViewModel.isConnected.value)
    assertFalse(profilesRepository.waitingDone.value)
    assertFalse(profilesRepository.isWaiting.value)

    // Simulate a failed request that starts the timer
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 300
    val exception =
        com.google.firebase.firestore.FirebaseFirestoreException(
            "Unavailable",
            com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE)

    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
        }

    // First failed request starts the timer
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    // Advance time to complete the timer
    advanceTimeBy(15_001)

    // Mock repository states
    whenever(profilesRepository.isWaiting).thenReturn(MutableStateFlow(true))
    whenever(profilesRepository.waitingDone).thenReturn(MutableStateFlow(true))

    // Another failed request after timer completes
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    // Should now be offline
    assertFalse(profilesViewModel.isConnected.value)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun showsOnlineWhenDifferentError() = runTest {
    whenever(profilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    whenever(profilesRepository.waitingDone).thenReturn(MutableStateFlow(false))
    // Initial state
    assertTrue(profilesViewModel.isConnected.value)
    assertFalse(profilesRepository.waitingDone.value)
    assertFalse(profilesRepository.isWaiting.value)

    // Simulate a failed request that doesn't start the timer
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000
    val exception = Exception("Test exception")

    // Mock repository states
    whenever(profilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    whenever(profilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
        }

    // First failed request doesn't start the timer
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    // Advance time
    advanceTimeBy(16_000)

    // Another failed request after timer completes
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    // Should still be online because it wasn't the correct error
    assertTrue(profilesViewModel.isConnected.value)
  }

  @Test
  fun clearTempProfilePictureBitmapSucceeds() {
    val mockBitmap = mock(Bitmap::class.java)
    val field = ProfilesViewModel::class.java.getDeclaredField("_tempProfilePictureBitmap")
    field.isAccessible = true
    val tempProfilePictureBitmap = field.get(profilesViewModel) as MutableStateFlow<Bitmap?>
    tempProfilePictureBitmap.value = mockBitmap

    profilesViewModel.clearTempProfilePictureBitmap()

    assertThat(profilesViewModel.tempProfilePictureBitmap.value, `is`(nullValue()))
  }

  @Test
  fun generateTempProfilePictureBitmapSucceeds() = runBlocking {
    // Mock ContentResolver and Uri
    val contentResolver = mock(ContentResolver::class.java)
    val uri = mock(Uri::class.java)
    whenever(context.contentResolver).thenReturn(contentResolver)

    // Create a test image as InputStream
    val testImageBytes = ByteArray(100) { it.toByte() }
    val inputStream = ByteArrayInputStream(testImageBytes)
    whenever(contentResolver.openInputStream(uri)).thenReturn(inputStream)

    // Create a mock bitmap that will be "decoded" from the input stream
    val mockBitmap = mock(Bitmap::class.java)
    whenever(mockBitmap.width).thenReturn(100)
    whenever(mockBitmap.height).thenReturn(100)

    // Mock BitmapFactory.decodeStream to return our mock bitmap
    bitmapFactoryMock.`when`<Bitmap> { BitmapFactory.decodeStream(any()) }.thenReturn(mockBitmap)

    // Mock Bitmap.createBitmap to return the same mock bitmap
    bitmapMock
        .`when`<Bitmap> { Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any()) }
        .thenReturn(mockBitmap)

    profilesViewModel.generateTempProfilePictureBitmap(context, uri)

    assertThat(profilesViewModel.tempProfilePictureBitmap.value, `is`(mockBitmap))
  }

  @Test
  fun validateAndUploadProfilePictureWithNoTempBitmapDoesNothing() = runBlocking {
    profilesViewModel.clearTempProfilePictureBitmap() // Ensure no temp bitmap exists
    profilesViewModel.validateAndUploadProfilePicture(context) { /*pass*/}

    // Verify no interactions with upload functionality
    verify(profilesRepository, never()).updateProfile(any(), any(), any())
  }

  @Test
  fun setPictureChangeStateUpdatesState() {
    profilesViewModel.setPictureChangeState(ProfilesViewModel.ProfilePictureState.TO_UPLOAD)
    assertThat(
        profilesViewModel.pictureChangeState.value,
        `is`(ProfilesViewModel.ProfilePictureState.TO_UPLOAD))
  }

  @Test
  fun saveEditedProfileUpdatesState() {
    profilesViewModel.saveEditedProfile(profile1)
    assertThat(profilesViewModel.editedCurrentProfile.value, `is`(profile1))
  }

  @Test
  fun resetProfileEditionStateClearsStates() {
    profilesViewModel.saveEditedProfile(profile1)
    profilesViewModel.setPictureChangeState(ProfilesViewModel.ProfilePictureState.TO_UPLOAD)

    profilesViewModel.resetProfileEditionState()

    assertThat(profilesViewModel.editedCurrentProfile.value, `is`(nullValue()))
    assertThat(
        profilesViewModel.pictureChangeState.value,
        `is`(ProfilesViewModel.ProfilePictureState.UNCHANGED))
    assertThat(profilesViewModel.tempProfilePictureBitmap.value, `is`(nullValue()))
  }

  @Test
  fun processCroppedImageUpdatesState() {
    val mockBitmap = mock(Bitmap::class.java)
    whenever(mockBitmap.width).thenReturn(100)
    whenever(mockBitmap.height).thenReturn(100)

    profilesViewModel.processCroppedImage(mockBitmap)

    assertThat(profilesViewModel.tempProfilePictureBitmap.value, `is`(mockBitmap))
    assertThat(
        profilesViewModel.pictureChangeState.value,
        `is`(ProfilesViewModel.ProfilePictureState.TO_UPLOAD))
  }

  @Test
  fun confirmMeetingLocationTest() {
    val updatedProfile = profile1.copy()
    val finalProfile =
        profile1.copy(
            meetingRequestChosenLocalisation =
                mapOf("2" to Pair("we can meat here", Pair(5.0, 6.0))))
    profilesViewModel.updateProfile(updatedProfile, {}) {}
    profilesViewModel.confirmMeetingLocation("2", Pair("we can meat here", Pair(5.0, 6.0)), {}) {}
    verify(profilesRepository).updateProfile(eq(finalProfile), any(), any())
  }

  @Test
  fun removeChosenLocationTest() {
    val updatedProfile =
        profile1.copy(
            meetingRequestChosenLocalisation =
                mapOf("2" to Pair("we can meat here", Pair(5.0, 6.0))))
    val finalProfile = profile1.copy(meetingRequestChosenLocalisation = mapOf())
    profilesViewModel.updateProfile(updatedProfile, {}) {}
    profilesViewModel.removeChosenLocalisation("2")
    verify(profilesRepository).updateProfile(eq(finalProfile), any(), any())
  }

  @Test
  fun getChosenLocationTest() {
    val updatedProfile =
        profile1.copy(
            meetingRequestChosenLocalisation =
                mapOf("2" to Pair("we can meat here", Pair(5.0, 6.0))))
    `when`(profilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Profile>) -> Unit>(1)
      onSuccess(listOf(profile2))
    }
    profilesViewModel.updateProfile(updatedProfile, {}) {}
    profilesViewModel.getChosenLocationsUsers()
    assertEquals(
        profilesViewModel.chosenLocalisations.value,
        mapOf(profile2 to Pair("we can meat here", Pair(5.0, 6.0))))
  }
}
