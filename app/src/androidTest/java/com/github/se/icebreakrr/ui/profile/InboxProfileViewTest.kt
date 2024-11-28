package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onIdle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.functions.FirebaseFunctions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class InboxProfileViewTest {
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var profileViewModel: ProfilesViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var mockProfileRepository: ProfilesRepository
  private lateinit var firebaseFunctions: FirebaseFunctions
  private lateinit var mockTagsRepository: TagsRepository
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    mockProfileRepository = mock(ProfilesRepository::class.java)
    mockNavigationActions = mock(NavigationActions::class.java)
    tagsViewModel =
        TagsViewModel(
            TagsRepository(mock(FirebaseFirestore::class.java), mock(FirebaseAuth::class.java)))

    val testProfile =
        Profile(
            uid = "testUid",
            name = "Test User",
            gender = Gender.OTHER,
            birthDate = Timestamp.now(),
            catchPhrase = "Hello world!",
            description = "Test description",
            tags = listOf("tag1", "tag2"),
            profilePictureUrl = null,
            fcmToken = "testFcmToken",
            location = GeoPoint(0.0, 0.0),
            geohash = "u0kbb57",
            hasBlocked = emptyList(),
            meetingRequestSent = emptyList(),
            meetingRequestInbox = mapOf(Pair("12345", "Hey, do you want to meat?")))

    profileViewModel =
        ProfilesViewModel(
            mockProfileRepository,
            mock(ProfilePicRepository::class.java),
            mock(FirebaseAuth::class.java))
    meetingRequestViewModel =
        MeetingRequestViewModel(profileViewModel, mock(FirebaseFunctions::class.java))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Profile) -> Unit>(1)
          onSuccess(testProfile)
          null
        }
        .`when`(mockProfileRepository)
        .getProfileByUid(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Profile>) -> Unit>(1)
          onSuccess(listOf(testProfile))
          null
        }
        .`when`(mockProfileRepository)
        .getMultipleProfiles(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun InboxProfileCheckDisplayedTest() {
    composeTestRule.setContent {
      InboxProfileViewScreen(
          profileViewModel,
          null,
          mockNavigationActions,
          tagsViewModel,
          meetingRequestViewModel,
          true)
    }
    onIdle()
    composeTestRule.onNodeWithTag("NotificationProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("flagButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("requestButton").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("Accept/DeclineBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RequestMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("declineButton").assertIsDisplayed().assertHasClickAction()

    composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello world!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertIsDisplayed()
  }

  @Test
  fun InboxProfileAcceptRequestTest() {
    composeTestRule.setContent {
      InboxProfileViewScreen(
          profileViewModel,
          null,
          mockNavigationActions,
          tagsViewModel,
          meetingRequestViewModel,
          true)
    }
    onIdle()
    composeTestRule.onNodeWithTag("acceptButton").performClick()
    verify(mockNavigationActions).goBack()
  }

  @Test
  fun InboxProfileDeclineRequestTest() {
    composeTestRule.setContent {
      InboxProfileViewScreen(
          profileViewModel,
          null,
          mockNavigationActions,
          tagsViewModel,
          meetingRequestViewModel,
          true)
    }
    onIdle()
    composeTestRule.onNodeWithTag("declineButton").performClick()
    verify(mockNavigationActions).goBack()
  }
}
