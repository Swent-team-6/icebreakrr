package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class NotificationTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockProfilesRepository: ProfilesRepository
    private lateinit var mockPPRepository: ProfilePicRepository
  private lateinit var profilesViewModel: ProfilesViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = Mockito.mock(NavigationActions::class.java)
    mockProfilesRepository = Mockito.mock(ProfilesRepository::class.java)

    profilesViewModel = ProfilesViewModel(mockProfilesRepository, mockPPRepository)
  }

  @Test
  fun notificationIsDisplayedOnLaunch() {
    composeTestRule.setContent { NotificationScreen(navigationActions, profilesViewModel) }
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
  }

  @Test
  fun allIsDisplayed() {
    composeTestRule.setContent { NotificationScreen(navigationActions, profilesViewModel) }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationSecondText").performScrollTo()
  }

  @Test
  fun profilesListIsEmpty() {
    // Set the list of profiles to an empty list
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(emptyList())
      null
    }
    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300.0)

    composeTestRule.setContent { NotificationScreen(navigationActions, profilesViewModel) }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("notificationSecondText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
  }

  @Test
  fun profilesListNotEmpty() {
    val profile = mockProfile()

    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(listOf(profile))
      null
    }

    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300.0)
    composeTestRule.setContent { NotificationScreen(navigationActions, profilesViewModel) }
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").assertCountEquals(1)
  }

  @Test
  fun bottomNavigationBarTest() {
    composeTestRule.setContent { NotificationScreen(navigationActions, profilesViewModel) }
    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
    verify(navigationActions).navigateTo(TopLevelDestinations.SETTINGS)

    composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()
    verify(navigationActions).navigateTo(TopLevelDestinations.AROUND_YOU)
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
          birthDate = birthDate2002, // 22 years old
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"))
}
