package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.profile.reportType
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.google.firebase.functions.FirebaseFunctions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class OtherProfileViewTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var fakeProfilesViewModel: MockProfileViewModel
  private lateinit var tagsViewModel: TagsViewModel

  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var profilesRepository: ProfilesRepository
  private lateinit var ppRepository: ProfilePicRepository
  private lateinit var ourUserId: String
  private lateinit var functions: FirebaseFunctions

  private lateinit var mockTagsRepository: TagsRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockTagsRepository = mock(TagsRepository::class.java)
    ourUserId = "UserId1"
    profilesRepository = mock(ProfilesRepository::class.java)
    ppRepository = mock(ProfilePicRepository::class.java)
    profilesViewModel = ProfilesViewModel(profilesRepository, ppRepository)

    functions = mock(FirebaseFunctions::class.java)
    meetingRequestViewModel =
        MeetingRequestViewModel(profilesViewModel, functions, ourUserId, "My Name")

    tagsViewModel = TagsViewModel(mockTagsRepository)

    fakeProfilesViewModel = MockProfileViewModel()
  }

  @Test
  fun OtherProfileDisplayTagTest() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("aroundYouProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("catchPhrase").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tagSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  }

  @Test
  fun OtherProfileGoBackButtonMessage() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationActions).goBack()
  }

  // @Test
  // fun OtherProfileMessageTest() {
  //  fakeProfilesViewModel.setLoading(false)
  //  fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

  //  composeTestRule.setContent {
  //    OtherProfileView(
  //        fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
  //  }
  //  composeTestRule.onNodeWithTag("requestButton").performClick()

  // composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()
  // composeTestRule.onNodeWithTag("sendButton").assertIsDisplayed()
  // composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
  // composeTestRule.onNodeWithTag("messageTextField").assertIsDisplayed()

  // composeTestRule.onNodeWithTag("messageTextField").performTextInput("New message")
  // composeTestRule.onNodeWithTag("messageTextField").assertTextContains("New message")

  // composeTestRule.onNodeWithTag("sendButton").performClick()
  //    composeTestRule.onNodeWithTag("bluredBackground").assertIsNotDisplayed()

  // composeTestRule.onNodeWithTag("requestButton").performClick()

  // composeTestRule.onNodeWithTag("bluredBackground").performClick()
  // composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()

  // composeTestRule.onNodeWithTag("cancelButton").performClick()
  // composeTestRule.onNodeWithTag("bluredBackground").assertIsNotDisplayed()
  // }

  @Test
  fun testFlagButtonDisplaysReportBlockDialog() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("flagButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("flagButton").performClick()

    composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsDisplayed()
    composeTestRule.onNodeWithText("Block").assertIsDisplayed()
    composeTestRule.onNodeWithText("Report").assertIsDisplayed()
  }

  @Test
  fun testBlockUserFlowWithCancel() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("flagButton").performClick()
    composeTestRule.onNodeWithText("Block").performClick()

    composeTestRule.onNodeWithText("Do you really want to block this user?").assertIsDisplayed()

    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsNotDisplayed()
  }

  @Test
  fun testBlockUserFlowWithConfirm() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])
    fakeProfilesViewModel.setSelfProfile(Profile.getMockedProfiles()[1])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("flagButton").performClick()
    composeTestRule.onNodeWithText("Block").performClick()

    composeTestRule.onNodeWithText("Do you really want to block this user?").assertIsDisplayed()

    composeTestRule.onNodeWithText("Block").performClick()
    composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsNotDisplayed()
  }

  @Test
  fun testReportUserUI() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[1])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("flagButton").performClick()
    composeTestRule.onNodeWithText("Report").performClick()

    composeTestRule.onNodeWithText("Select a reason for reporting:").assertIsDisplayed()

    reportType.values().forEach { reportType ->
      composeTestRule.onNodeWithText(reportType.displayName).assertIsDisplayed()
    }
  }

  @Test
  fun testReportSubmissionWithCancel() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[1])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("flagButton").performClick()
    composeTestRule.onNodeWithText("Report").performClick()

    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("flagButton").performClick()
    composeTestRule.onNodeWithText("Report").performClick()

    composeTestRule.onNodeWithText(reportType.values()[0].displayName).performClick()

    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsNotDisplayed()
  }

  @Test
  fun testReportSubmissionWithConfirm() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[1])

    composeTestRule.setContent {
      OtherProfileView(
          fakeProfilesViewModel, tagsViewModel, meetingRequestViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("flagButton").performClick()
    composeTestRule.onNodeWithText("Report").performClick()

    composeTestRule.onNodeWithText(reportType.values()[1].displayName).performClick()
    composeTestRule.onNodeWithText("Report").performClick()

    composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsNotDisplayed()
  }
}
