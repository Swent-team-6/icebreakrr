package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import java.util.Calendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class NotificationTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockPPRepository: ProfilePicRepository
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var functions: FirebaseFunctions
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockUser: FirebaseUser
  private lateinit var auth: FirebaseAuth

  @get:Rule val composeTestRule = createComposeRule()

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
          geohash = "LocationProfile1",
          meetingRequestSent = listOf(),
          meetingRequestInbox = mapOf(),
          meetingRequestChosenLocalisation = mapOf())

  private val profile2 =
      Profile(
          uid = "2",
          name = "Jean D'eau",
          gender = Gender.MEN,
          birthDate = birthDate2005,
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"),
          profilePictureUrl = "http://example.com/profile.jpg",
          fcmToken = "TokenUser2",
          geohash = "LocationProfile2",
          meetingRequestSent = listOf(),
          meetingRequestInbox = mapOf())

  private val profile3 =
      Profile(
          uid = "3",
          name = "Clint O'Neil",
          gender = Gender.MEN,
          birthDate = birthDate2005,
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"),
          profilePictureUrl = "http://example.com/profile.jpg",
          fcmToken = "TokenUser3",
          geohash = "LocationProfile3",
          meetingRequestSent = listOf(),
          meetingRequestInbox = mapOf())

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockPPRepository = mock(ProfilePicRepository::class.java)
    mockProfilesRepository = mock(ProfilesRepository::class.java)
    mockUser = mock(FirebaseUser::class.java)
    auth = mock(FirebaseAuth::class.java)
    profilesViewModel = ProfilesViewModel(mockProfilesRepository, mockPPRepository, auth)
    functions = mock(FirebaseFunctions::class.java)
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, functions)

    `when`(auth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("1")
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
    composeTestRule.onAllNodesWithText("Inbox").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
  }

  @Test
  fun InboxListIsEmpty() {
    // Set the list of profiles to an empty list
    `when`(mockProfilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(profile1)
    }
    `when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Profile>) -> Unit>(1)
      onSuccess(listOf())
    }
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
  }

  @Test
  fun InboxListOneElement() {
    val updatedProfile =
        profile1.copy(
            meetingRequestInbox =
                mapOf(
                    Pair(profile2.uid, Pair(Pair("hello", "am under the bridge"), Pair(1.0, 2.0)))))
    `when`(mockProfilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(updatedProfile)
    }
    `when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Profile>) -> Unit>(1)
      onSuccess(listOf(profile2))
    }
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").performClick()
  }

  @Test
  fun InboxListTwoElement() {
    val updatedProfile =
        profile1.copy(
            meetingRequestInbox =
                mapOf(
                    (profile2.uid to Pair(Pair("hello", "am under the bridge"), Pair(1.0, 2.0))),
                    (profile3.uid to
                        Pair(Pair("hey there !", "am under the Eiffel tower"), Pair(50.0, 65.0)))))
    `when`(mockProfilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(updatedProfile)
    }
    `when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Profile>) -> Unit>(1)
      onSuccess(listOf(profile2, profile3))
    }
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").assertCountEquals(2)
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()
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

  @Test
  fun badgeDisplayWhen1PendingAnd1Inbox() {
    val updatedProfile =
        profile1.copy(
            meetingRequestInbox =
                mapOf(profile2.uid to Pair(Pair("hello", "am under the bridge"), Pair(1.0, 2.0))))
    `when`(mockProfilesRepository.getProfileByUid(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(Profile) -> Unit>(1)
      onSuccess(updatedProfile)
    }
    `when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Profile>) -> Unit>(1)
      onSuccess(listOf(profile2))
    }
    composeTestRule.setContent {
      NotificationScreen(navigationActions, profilesViewModel, meetingRequestViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inboxButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sentButton").assertIsDisplayed().performClick()
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
