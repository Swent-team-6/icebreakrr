package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ProfileEditingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var fakeProfilesViewModel: MockProfileViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockTagsRepository: TagsRepository

  @Before
  fun setUp() {
    navigationActions = Mockito.mock(NavigationActions::class.java)
    mockProfilesRepository = Mockito.mock(ProfilesRepository::class.java)
    mockTagsRepository = Mockito.mock(TagsRepository::class.java)

    tagsViewModel = TagsViewModel(mockTagsRepository)
    profilesViewModel =
        ProfilesViewModel(mockProfilesRepository, ProfilePicRepositoryStorage(Firebase.storage))
    fakeProfilesViewModel = MockProfileViewModel()

    var mockProfile = Profile.getMockedProfiles()[0]
    fakeProfilesViewModel.setSelectedProfile(mockProfile)
    fakeProfilesViewModel.setLoading(false)
  }

  @Test
  fun testLoadingState() {
    fakeProfilesViewModel.setLoading(true)

    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }

    composeTestRule.onNodeWithTag("loadingBox").assertIsDisplayed()
    composeTestRule.onNodeWithText("Loading profile...").assertIsDisplayed()
  }

  @Test
  fun testTopBarElements() {
    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }

    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkButton").assertIsDisplayed()
  }

  @Test
  fun testProfileEditingScreenUI() {

    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }
    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileEditScreenContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nameAndAge").assertIsDisplayed()

    composeTestRule.onNodeWithTag("catchphrase").performTextClearance()
    composeTestRule.onNodeWithTag("catchphrase").performTextInput("New Catchphrase")
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains("New Catchphrase")
    composeTestRule.onNodeWithTag("description").performTextClearance()
    composeTestRule.onNodeWithTag("description").performTextInput("New Description")
    composeTestRule.onNodeWithTag("description").assertTextContains("New Description")
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("alertDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag("alertDialog").assertIsNotDisplayed()
  }

  @Test
  fun testNoSaveOfChangesWithoutChanges() {
    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testSaveOfChanges() {
    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }

    composeTestRule.onNodeWithTag("checkButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testProfileEditingScreenContent() {

    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }
    val profile = Profile.getMockedProfiles()[0]

    composeTestRule
        .onNodeWithTag("nameAndAge")
        .assertTextEquals(profile.name)
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains(profile.catchPhrase)
    composeTestRule.onNodeWithTag("description").assertTextContains(profile.description)
  }

  @Test
  fun testEdgeCases() {

    composeTestRule.setContent {
      ProfileEditingScreen(navigationActions, tagsViewModel, fakeProfilesViewModel)
    }

    composeTestRule.onNodeWithTag("description").performTextInput("New catchphrase")
    composeTestRule.onNodeWithTag("catchphrase").performTextClearance()
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains("")

    composeTestRule.onNodeWithTag("description").performTextInput("New Description")
    composeTestRule.onNodeWithTag("description").performTextClearance()
    composeTestRule.onNodeWithTag("description").assertTextContains("")

    val longText = "A".repeat(100)
    composeTestRule.onNodeWithTag("description").performTextInput(longText)
    composeTestRule.onNodeWithTag("description").assertTextContains(longText.take(100))
    composeTestRule.onNodeWithTag("catchphrase").performTextInput(longText)
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains(longText.take(100))
    composeTestRule.onNodeWithTag("checkButton").performClick()
    verify(navigationActions).goBack()
  }
}
