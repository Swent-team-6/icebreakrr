package com.github.se.icebreakrr.model.sort

import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock

class SortViewModelTest {

  @Mock private lateinit var profilesViewModel: ProfilesViewModel

  private lateinit var sortViewModel: SortViewModel

  private val sampleProfile1 =
      Profile(
          uid = "1",
          name = "Alice",
          gender = Gender.WOMEN,
          birthDate = Timestamp(946684800, 0), // Year 2000
          catchPhrase = "Hello",
          description = "Test user",
          tags = listOf("hiking", "reading"),
          distanceToSelfProfile = 10)

  private val sampleProfile2 =
      Profile(
          uid = "2",
          name = "Bob",
          gender = Gender.MEN,
          birthDate = Timestamp(788918400, 0), // Year 1995
          catchPhrase = "Hi there",
          description = "Another test user",
          tags = listOf("cooking", "reading"),
          distanceToSelfProfile = 5)

  private val sampleProfile3 =
      Profile(
          uid = "3",
          name = "Charlie",
          gender = Gender.OTHER,
          birthDate = Timestamp(126230400, 0), // Year 1974
          catchPhrase = "Hey",
          description = "Yet another test user",
          tags = listOf("hiking", "coding"),
          distanceToSelfProfile = 15)

  private val selfProfile =
      Profile(
          uid = "self",
          name = "Self",
          gender = Gender.MEN,
          birthDate = Timestamp(631152000, 0), // Year 1990
          catchPhrase = "It's me",
          description = "Self profile",
          tags = listOf("reading", "hiking"))

  @Before
  fun setUp() {
    profilesViewModel =
        ProfilesViewModel(
            mock(ProfilesRepository::class.java),
            mock(ProfilePicRepository::class.java),
            mock(FirebaseAuth::class.java))
    sortViewModel = SortViewModel(profilesViewModel)

    // Set up selfProfile as a real MutableStateFlow
    profilesViewModel.selfProfile = MutableStateFlow(selfProfile)
  }

  @Test
  fun `test sort by distance`() {
    val profiles = listOf(sampleProfile1, sampleProfile2, sampleProfile3)
    val sortedProfiles = sortViewModel.sortByDistance(profiles)

    assertEquals(listOf(sampleProfile2, sampleProfile1, sampleProfile3), sortedProfiles)
  }

  @Test
  fun `test sort by age`() {
    val profiles = listOf(sampleProfile1, sampleProfile2, sampleProfile3)
    val sortedProfiles = sortViewModel.sortByAge(profiles)

    assertEquals(listOf(sampleProfile1, sampleProfile2, sampleProfile3), sortedProfiles)
  }

  @Test
  fun `test sort by common tags`() {
    val profiles = listOf(sampleProfile1, sampleProfile2, sampleProfile3)
    val sortedProfiles = sortViewModel.sortByCommonTags(profiles)

    assertEquals(listOf(sampleProfile1, sampleProfile2, sampleProfile3), sortedProfiles)
  }

  @Test
  fun `test update sort option`() {
    sortViewModel.updateSortOption(SortOption.AGE)
    assertEquals(SortOption.AGE, sortViewModel.selectedSortOption.value)

    sortViewModel.updateSortOption(SortOption.DISTANCE)
    assertEquals(SortOption.DISTANCE, sortViewModel.selectedSortOption.value)
  }
}
