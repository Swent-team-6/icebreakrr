package com.github.se.icebreakrr.model.profile

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class ProfilesViewModelTest {
  private lateinit var profilesRepository: ProfilesRepository
  private lateinit var ppRepository: ProfilePicRepository
  private lateinit var profilesViewModel: ProfilesViewModel
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
          profilePictureUrl = "http://example.com/profile.jpg")

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

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    profilesRepository = mock(ProfilesRepository::class.java)
    ppRepository = mock(ProfilePicRepository::class.java)
    profilesViewModel = ProfilesViewModel(profilesRepository, ppRepository)
    Dispatchers.setMain(testDispatcher) // Set main dispatcher first
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun getFilteredProfilesInRadiusCallsRepositoryWithFilters() = runBlocking {
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000.0

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
    val radiusInMeters = 1000.0

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
    val radiusInMeters = 1000.0
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
  fun addNewProfileCallsRepository() = runBlocking {
    profilesViewModel.addNewProfile(profile1)

    verify(profilesRepository).addNewProfile(eq(profile1), any(), any())
  }

  @Test
  fun updateProfileCallsRepository() = runBlocking {
    profilesViewModel.updateProfile(profile1)

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

    profilesViewModel.updateProfile(profile1)

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

  @Test
  fun uploadCurrentUserProfilePictureSucceeds() = runBlocking {
    val imageData = ByteArray(4) { 0xFF.toByte() }

    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }

    whenever(ppRepository.uploadProfilePicture(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(String?) -> Unit>(2)
      onSuccess("http://example.com/profile.jpg")
    }

    profilesViewModel.getProfileByUid("1")
    profilesViewModel.uploadCurrentUserProfilePicture(imageData)

    verify(ppRepository).uploadProfilePicture(eq("1"), eq(imageData), any(), any())
    assertThat(
        profilesViewModel.selectedProfile.value?.profilePictureUrl,
        `is`("http://example.com/profile.jpg"))
  }

  @Test
  fun uploadCurrentUserProfilePictureFails() = runBlocking {
    val imageData = ByteArray(4) { 0xFF.toByte() }
    val exception = Exception("Failed to upload profile picture")

    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }

    whenever(ppRepository.uploadProfilePicture(any(), any(), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(3)
      onFailure(exception)
    }

    profilesViewModel.getProfileByUid("1")
    profilesViewModel.uploadCurrentUserProfilePicture(imageData)

    verify(ppRepository).uploadProfilePicture(eq("1"), eq(imageData), any(), any())
    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  @Test
  fun uploadCurrentUserProfilePictureThrowsExceptionIfUserNotLoggedIn() {
    val imageData = ByteArray(4) { 0xFF.toByte() }

    val exception =
        assertThrows(IllegalStateException::class.java) {
          profilesViewModel.uploadCurrentUserProfilePicture(imageData)
        }

    assertThat(exception.message, `is`("User not logged in"))
  }

  @Test
  fun deleteCurrentUserProfilePictureSucceeds() = runBlocking {
    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }

    whenever(ppRepository.deleteProfilePicture(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    profilesViewModel.getProfileByUid("1")
    profilesViewModel.deleteCurrentUserProfilePicture()

    verify(ppRepository).deleteProfilePicture(eq("1"), any(), any())
    assertThat(profilesViewModel.selectedProfile.value?.profilePictureUrl, `is`(nullValue()))
  }

  @Test
  fun deleteCurrentUserProfilePictureFails() = runBlocking {
    val exception = Exception("Failed to delete profile picture")

    whenever(profilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }

    whenever(ppRepository.deleteProfilePicture(any(), any(), any())).thenAnswer {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(exception)
    }

    profilesViewModel.getProfileByUid("1")
    profilesViewModel.deleteCurrentUserProfilePicture()

    verify(ppRepository).deleteProfilePicture(eq("1"), any(), any())
    assertThat(profilesViewModel.error.value, `is`(exception))
  }

  // Generated with the help of CursorAI
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun showsOfflineWhenTimerCompletesAndStillFailing() = runTest {
    // Initial state
    assertThat(profilesViewModel.isConnected.value, `is`(true))
    assertThat(profilesViewModel.waitingDone.value, `is`(false))
    assertThat(profilesViewModel.isWaiting.value, `is`(false))

    // Simulate a failed request that starts the timer
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000.0
    val exception =
        (com.google.firebase.firestore.FirebaseFirestoreException(
            "Permission denied",
            com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED))

    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
        }

    // First failed request starts the timer
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)
    assertThat(profilesViewModel.isWaiting.value, `is`(true))

    // Advance time to complete the timer
    advanceTimeBy(16_000)
    assertThat(profilesViewModel.waitingDone.value, `is`(true))

    // Another failed request after timer completes
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    // Should now be offline
    assertThat(profilesViewModel.isConnected.value, `is`(false))
  }

  // Generated with the help of CursorAI
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun showsOnlineWhenDifferentError() = runTest {
    // Initial state
    assertThat(profilesViewModel.isConnected.value, `is`(true))
    assertThat(profilesViewModel.waitingDone.value, `is`(false))
    assertThat(profilesViewModel.isWaiting.value, `is`(false))

    // Simulate a failed request that doesn't start the timer
    val center = GeoPoint(0.0, 0.0)
    val radiusInMeters = 1000.0
    val exception = Exception("Test exception")

    whenever(profilesRepository.getProfilesInRadius(eq(center), eq(radiusInMeters), any(), any()))
        .thenAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
        }

    // First failed request doesn't start the timer
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)
    assertThat(profilesViewModel.isWaiting.value, `is`(false))

    // Advance time to complete the timer
    advanceTimeBy(16_000)
    assertThat(profilesViewModel.waitingDone.value, `is`(false))

    // Another failed request after timer completes
    profilesViewModel.getFilteredProfilesInRadius(center, radiusInMeters)

    // Should still be online because it wasn't the correct error
    assertThat(profilesViewModel.isConnected.value, `is`(true))
  }
}
