package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class AlreadyMetTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var profilesViewModel: ProfilesViewModel
  private val profiles = MutableStateFlow<List<Profile>>(emptyList())

  @Before
  fun setup() {
    navigationActions = mock(NavigationActions::class.java)
    profilesViewModel = mock(ProfilesViewModel::class.java)
    `when`(profilesViewModel.profiles).thenReturn(profiles)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { AlreadyMetScreen(navigationActions, profilesViewModel) }

    composeTestRule.onNodeWithTag("alreadyMetScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  }

  @Test
  fun testGoBackButtonFunctionality() {
    composeTestRule.setContent { AlreadyMetScreen(navigationActions, profilesViewModel) }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun displayProfileCardsWhenNotEmpty() {
    // Create mock profiles
    val mockProfiles = listOf(mockProfile(), mockProfile().copy(uid = "2"))
    profiles.value = mockProfiles

    composeTestRule.setContent { AlreadyMetScreen(navigationActions, profilesViewModel) }

    // Verify profile cards are displayed
    composeTestRule.onAllNodesWithTag("profileCard").assertCountEquals(2)
  }

  @Test
  fun testProfileCardNavigation() {
    val mockProfile = mockProfile()
    profiles.value = listOf(mockProfile)

    composeTestRule.setContent { AlreadyMetScreen(navigationActions, profilesViewModel) }

    composeTestRule.onNodeWithTag("profileCard").performClick()
    verify(navigationActions).navigateTo(Screen.OTHER_PROFILE_VIEW + "?userId=${mockProfile.uid}")
  }

  // Helper function to create a mock profile
  private val birthDate2002 =
      Timestamp(
          Calendar.getInstance()
              .apply {
                set(2002, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
              }
              .time)

  private fun mockProfile() =
      Profile(
          uid = "1",
          name = "John Doe",
          gender = Gender.MEN,
          birthDate = birthDate2002,
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"))
}
