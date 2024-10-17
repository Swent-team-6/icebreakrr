package com.github.se.icebreakrr.model.profile

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class ProfilesViewModelTest {
  private lateinit var profilesRepository: ProfilesRepository
  private lateinit var profilesViewModel: ProfilesViewModel

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
          tags = listOf("friendly", "outgoing"))

  private val profile2 =
      Profile(
          uid = "2",
          name = "Jane Smith",
          gender = Gender.WOMEN,
          birthDate = birthDate2002,
          catchPhrase = "Adventure awaits!",
          description = "Always looking for new experiences.",
          tags = listOf("adventurous", "outgoing"))

  @Before
  fun setUp() {
    profilesRepository = mock(ProfilesRepository::class.java)
    profilesViewModel = ProfilesViewModel(profilesRepository)
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
    assertThat(profilesViewModel.profiles.value.size, `is`(1)) // Should return only profile1
    assertThat(profilesViewModel.profiles.value[0].uid, `is`("1")) // profile1 should be returned
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
}
