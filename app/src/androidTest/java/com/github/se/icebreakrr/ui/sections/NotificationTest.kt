package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class NotificationTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockPPRepository: ProfilePicRepository
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var functions: FirebaseFunctions
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockPPRepository = mock(ProfilePicRepository::class.java)
    mockProfilesRepository = mock(ProfilesRepository::class.java)
    mockAuth = mock(FirebaseAuth::class.java)
    profilesViewModel = ProfilesViewModel(mockProfilesRepository, mockPPRepository, mockAuth)
    functions = mock(FirebaseFunctions::class.java)
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, functions)

    // Mock repository state flows
    `when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    `when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

    // Mock successful connection check
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(emptyList())
      null
    }

    val profile = mockProfile()
    `when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(1)
      onSuccessCallback(listOf(profile))
    }

    `when`(navigationActions.currentRoute()).thenReturn(Route.NOTIFICATIONS)
  }

  @Test
  fun notificationIsDisplayedOnLaunch() {
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
  }

  @Test
  fun allIsDisplayed() {
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
  }

  @Test
  fun profilesListIsEmpty() {
    // Set the list of profiles to an empty list
    // TODO: add test for profile cards in the Notification Screen when having no meeting request

    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
  }

  @Test
  fun profilesListNotEmpty() {
    // TODO: add test for profile cards in the Notification Screen when having meeting request
  }

  @Test
  fun bottomNavigationBarTest() {
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
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

  private fun mockProfile2() =
      Profile(
          uid = "2",
          name = "Jan Smith",
          gender = Gender.MEN,
          birthDate = birthDate2002, // 22 years old
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"))
}
