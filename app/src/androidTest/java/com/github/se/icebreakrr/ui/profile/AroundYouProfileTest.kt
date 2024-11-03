package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.model.profile.MockProfileViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.getMockedProfiles
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AroundYouProfileTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var fakeProfilesViewModel: MockProfileViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var mockTagsRepository: TagsRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = Mockito.mock(NavigationActions::class.java)
    mockTagsRepository = Mockito.mock(TagsRepository::class.java)
    mockTagsRepository = Mockito.mock(TagsRepository::class.java)

    tagsViewModel = TagsViewModel(mockTagsRepository)

    fakeProfilesViewModel = MockProfileViewModel()
  }

  @Test
  fun AroundYouProfileDisplayTagTest() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions, null)
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
  fun AroundYouProfileGoBackButtonMessage() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions, null)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun AroundYouProfileMessageTest() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelectedProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      OtherProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions, null)
    }
    composeTestRule.onNodeWithTag("requestButton").performClick()

    composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sendButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("messageTextField").assertIsDisplayed()

    composeTestRule.onNodeWithTag("messageTextField").performTextInput("New message")
    composeTestRule.onNodeWithTag("messageTextField").assertTextContains("New message")

    composeTestRule.onNodeWithTag("sendButton").performClick()
    composeTestRule.onNodeWithTag("bluredBackground").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("requestButton").performClick()

    composeTestRule.onNodeWithTag("bluredBackground").performClick()
    composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()

    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.onNodeWithTag("bluredBackground").assertIsNotDisplayed()
  }
}
