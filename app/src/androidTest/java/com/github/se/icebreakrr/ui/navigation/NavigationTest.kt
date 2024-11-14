package com.github.se.icebreakrr.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.IcebreakrrNavHost
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.utils.PermissionManager
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NavigationTest {
  private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val tempFolder = TemporaryFolder()

  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfileViewModel: MockProfileViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockTagsRepository: TagsRepository
  private lateinit var mockFirebaseStorage: FirebaseStorage
  private lateinit var mockMeetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockFunction: FirebaseFunctions
  private lateinit var mockFilterViewModel: FilterViewModel
  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var appDataStore: AppDataStore
  private lateinit var mockLocationViewModel: LocationViewModel
  private lateinit var mockPermissionManager: PermissionManager

  @Before
  fun setup() {
    // Set up real DataStore with test scope
    testDataStore =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempFolder.newFolder(), "test_preferences.preferences_pb") })
    appDataStore = AppDataStore(testDataStore)

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
    mockLocationViewModel = mock(LocationViewModel::class.java)
    mockPermissionManager = mock(PermissionManager::class.java)
  }

  @Test
  fun testNavigationLogin() = runTest {
    composeTestRule.setContent {
      IcebreakrrNavHost(
          mockProfileViewModel,
          tagsViewModel,
          mockFilterViewModel,
          mockMeetingRequestViewModel,
          appDataStore,
          Route.AUTH,
          mockLocationViewModel,
          mockPermissionManager)
    }

    // Assert that the login screen is shown on launch
    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
  }

  @Test
  fun testBottomNavigationBar() = runTest {
    composeTestRule.setContent {
      IcebreakrrNavHost(
          mockProfileViewModel,
          tagsViewModel,
          mockFilterViewModel,
          mockMeetingRequestViewModel,
          appDataStore,
          Route.AROUND_YOU,
          mockLocationViewModel,
          mockPermissionManager)
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
  }
}
