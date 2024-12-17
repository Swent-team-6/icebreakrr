package com.github.se.icebreakrr.ui.map

import android.content.Context
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.model.location.ILocationService
import com.github.se.icebreakrr.model.location.LocationRepository
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.utils.IPermissionManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class LocationMapSelectorTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfileRepository: ProfilesRepository
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var mockUser: FirebaseUser
  private lateinit var mockFunctions: FirebaseFunctions
  private lateinit var mockContext: Context

  private val profile1 =
      Profile(
          uid = "1",
          name = "John Doe",
          gender = Gender.MEN,
          birthDate = Timestamp.now(), // 22 years old
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"),
          profilePictureUrl = "http://example.com/profile.jpg",
          meetingRequestInbox = mapOf(),
          meetingRequestChosenLocalisation = mapOf(),
          meetingRequestSent = listOf(),
          fcmToken = "11")

  private val profile2 =
      Profile(
          uid = "2",
          name = "Jane Smith",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Adventure awaits!",
          description = "Always looking for new experiences.",
          tags = listOf("adventurous", "outgoing"),
          profilePictureUrl = null,
          fcmToken = "22")

  private var profile1Changeable =
      Profile(
          uid = "1",
          name = "John Doe",
          gender = Gender.MEN,
          birthDate = Timestamp.now(), // 22 years old
          catchPhrase = "Just a friendly guy",
          description = "I love meeting new people.",
          tags = listOf("friendly", "outgoing"),
          profilePictureUrl = "http://example.com/profile.jpg",
          meetingRequestInbox = mapOf(),
          meetingRequestChosenLocalisation = mapOf(),
          meetingRequestSent = listOf(),
          fcmToken = "11")

  @Before
  fun setup() {
    mockProfileRepository = mock(ProfilesRepository::class.java)
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)
    mockFunctions = mock(FirebaseFunctions::class.java)
    mockLocationRepository = mock(LocationRepository::class.java)

    `when`(mockProfileRepository.getProfileByUid(anyOrNull(), anyOrNull(), anyOrNull()))
        .thenAnswer {
          val uid = it.getArgument<(String)>(0)
          val onSuccess = it.getArgument<(Profile) -> Unit>(1)
          if (uid == "1") {
            onSuccess(profile1Changeable)
          } else if (uid == "2") {
            onSuccess(profile2)
          }
        }
    `when`(mockProfileRepository.updateProfile(anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      val newProfile = it.getArgument<Profile>(0)
      profile1Changeable = newProfile
      onSuccess()
    }
    `when`(mockProfileRepository.getMultipleProfiles(anyOrNull(), anyOrNull(), anyOrNull()))
        .thenAnswer {
          val onSuccess = it.getArgument<(List<Profile>) -> Unit>(1)
          val uidList = it.getArgument<List<String>>(0)
          if (uidList == listOf("2")) {
            onSuccess(listOf(profile2))
          }
        }
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("1")

    profilesViewModel =
        ProfilesViewModel(mockProfileRepository, mock(ProfilePicRepository::class.java), mockAuth)
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, mockFunctions)

    mockNavigationActions = mock(NavigationActions::class.java)
    mockContext = mock(Context::class.java)

    locationViewModel =
        LocationViewModel(
            mock(ILocationService::class.java),
            mockLocationRepository,
            mock(IPermissionManager::class.java),
            mockContext)
    composeTestRule.setContent {
      LocationSelectorMapScreen(
          profilesViewModel,
          mockNavigationActions,
          meetingRequestViewModel,
          null,
          locationViewModel,
          true)
    }
  }

  @Test
  fun locationSelectorTest() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("LocationSelectorMapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addTextAndSendLocationBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("addDetailsTextField")
        .assertIsDisplayed()
        .performTextInput("Let's meet on the second floor")
    composeTestRule.onNodeWithText("Let's meet on the second floor").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("buttonSendMessageLocation")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    assertEquals(
        mapOf<Profile, Pair<String, Pair<Double, Double>>>(),
        profilesViewModel.chosenLocalisations.value)
    verify(mockFunctions).getHttpsCallable("sendMeetingRequest")
    verify(mockNavigationActions).navigateTo(Route.MAP)
  }
}
