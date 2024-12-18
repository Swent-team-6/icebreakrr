package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ProfileViewTest {

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
    tagsViewModel =
        TagsViewModel(
            TagsRepository(
                Mockito.mock(FirebaseFirestore::class.java),
                Mockito.mock(FirebaseAuth::class.java)))

    profilesViewModel =
        ProfilesViewModel(
            mockProfilesRepository,
            ProfilePicRepositoryStorage(Firebase.storage),
            FirebaseAuth.getInstance())
    fakeProfilesViewModel = MockProfileViewModel()
  }

  @Test
  fun testProfileHeaderDisplaysCorrectlyAsUser() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelfProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      ProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions)
    }

    // Verify that the profile header and its elements are displayed
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertTextEquals(Profile.getMockedProfiles()[0].name)
  }

  @Test
  fun testProfileHeaderDisplaysCorrectlyAsUserLoading() {
    fakeProfilesViewModel.setLoadingSelf(true)
    composeTestRule.setContent {
      ProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions)
    }

    // Verify that the profile header and its elements are displayed
    composeTestRule.onNodeWithTag("loadingBox").assertIsDisplayed()
  }

  @Test
  fun testGoBackButtonFunctionality() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelfProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      ProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions)
    }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun testEditButtonFunctionality() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelfProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      ProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions)
    }

    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").performClick()
  }

  @Test
  fun testInfoSectionDisplaysCorrectly() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelfProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      ProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions)
    }

    // Verify that the info section is displayed
    composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()

    // Verify that the catchphrase is displayed
    composeTestRule.onNodeWithText("«${Profile.getMockedProfiles()[0].catchPhrase}»").assertIsDisplayed()

    // Verify that all tags are displayed
    composeTestRule.onNodeWithTag("tagSection").assertIsDisplayed()

    // Verify that the description is displayed
    composeTestRule.onNodeWithTag("profileDescription").assertIsDisplayed()
  }

  @Test
  fun testProfileDescriptionDisplaysCorrectly() {
    fakeProfilesViewModel.setLoading(false)
    fakeProfilesViewModel.setSelfProfile(Profile.getMockedProfiles()[0])

    composeTestRule.setContent {
      ProfileView(fakeProfilesViewModel, tagsViewModel, navigationActions)
    }

    // Verify that the profile description is displayed correctly
    composeTestRule
        .onNodeWithTag("profileDescription")
        .assertTextContains(Profile.getMockedProfiles()[0].description)
        .assertIsDisplayed()
  }
}
