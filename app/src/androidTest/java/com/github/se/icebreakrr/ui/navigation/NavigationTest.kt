package com.github.se.icebreakrr.ui.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.IcebreakrrNavHost
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class NavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfileViewModel: MockProfileViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockTagsRepository: TagsRepository
  private lateinit var mockFirebaseStorage: FirebaseStorage
  private lateinit var mockMeetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockFunction: FirebaseFunctions
  private lateinit var mockFilterViewModel: FilterViewModel

  @Before
  fun setup() {
    // Initialize mocks and view models
    mockProfileViewModel = MockProfileViewModel()
    mockProfilesRepository = mock(ProfilesRepository::class.java)
    mockTagsRepository = mock(TagsRepository::class.java)
    mockFirebaseStorage = mock(FirebaseStorage::class.java)
    mockFunction = mock(FirebaseFunctions::class.java)
    mockMeetingRequestViewModel =
        MeetingRequestViewModel(mockProfileViewModel, mockFunction, "1", "John Doe")
    mockFilterViewModel = FilterViewModel()

    tagsViewModel = TagsViewModel(mockTagsRepository)
    profilesViewModel =
        ProfilesViewModel(mockProfilesRepository, ProfilePicRepositoryStorage(mockFirebaseStorage))
  }

  @Test
  fun testNavigationLogin() {

    composeTestRule.setContent {
      IcebreakrrNavHost(
          mockProfileViewModel,
          tagsViewModel,
          mockFilterViewModel,
          mockMeetingRequestViewModel,
          Route.AUTH, FirebaseAuth.getInstance())
    }

    // Assert that the login screen is shown on launch
    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
  }

  @Test
  fun testBottomNavigationBar() {
    composeTestRule.setContent {
      IcebreakrrNavHost(
          mockProfileViewModel,
          tagsViewModel,
          mockFilterViewModel,
          mockMeetingRequestViewModel,
          Route.AROUND_YOU, FirebaseAuth.getInstance())
    }
    // Check that the "Around You" screen is displayed after login
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()

    // Test navigation to the Settings screen
    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

    // Test navigation to the Notifications screen
    composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()

    // Test navigation to the AroundYou screen
    composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()

    // Test navigation to the Filter screen
  }
}
