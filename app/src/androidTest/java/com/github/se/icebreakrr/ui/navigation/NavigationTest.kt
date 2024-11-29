package com.github.se.icebreakrr.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.IcebreakrrNavHost
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.location.ILocationService
import com.github.se.icebreakrr.model.location.LocationRepository
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.sort.SortViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.utils.IPermissionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NavigationTest {
  private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val tempFolder = TemporaryFolder()

  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfileViewModel: MockProfileViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var sortViewModel: SortViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockTagsRepository: TagsRepository
  private lateinit var mockFirebaseStorage: FirebaseStorage
  private lateinit var mockMeetingRequestViewModel: MeetingRequestViewModel
  private lateinit var mockFunction: FirebaseFunctions
  private lateinit var mockFilterViewModel: FilterViewModel
  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var appDataStore: AppDataStore

  private lateinit var mockLocationService: ILocationService
  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var mockPermissionManager: IPermissionManager

  private lateinit var locationViewModel: LocationViewModel

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
    mockFirebaseStorage = mock(FirebaseStorage::class.java)
    mockFunction = mock(FirebaseFunctions::class.java)
    mockMeetingRequestViewModel = MeetingRequestViewModel(mockProfileViewModel, mockFunction)
    mockFilterViewModel = FilterViewModel()
    tagsViewModel =
        TagsViewModel(
            TagsRepository(mock(FirebaseFirestore::class.java), mock(FirebaseAuth::class.java)))
    profilesViewModel =
        ProfilesViewModel(
            mockProfilesRepository,
            ProfilePicRepositoryStorage(mockFirebaseStorage),
            mock(FirebaseAuth::class.java))

    sortViewModel = SortViewModel(profilesViewModel)
    mockLocationService = mock(ILocationService::class.java)
    mockLocationRepository = mock(LocationRepository::class.java)
    mockPermissionManager = mock(IPermissionManager::class.java)

    locationViewModel =
        LocationViewModel(mockLocationService, mockLocationRepository, mockPermissionManager)

    // mock state flow
    `when`(mockPermissionManager.permissionStatuses)
        .thenReturn(
            MutableStateFlow(
                mapOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION to
                        android.content.pm.PackageManager.PERMISSION_GRANTED)))
  }

  @Test
  fun testNavigationLogin() = runTest {
    composeTestRule.setContent {
      IcebreakrrNavHost(
          mockProfileViewModel,
          tagsViewModel,
          mockFilterViewModel,
          sortViewModel,
          mockMeetingRequestViewModel,
          appDataStore,
          locationViewModel,
          Route.AUTH,
          mock(FirebaseAuth::class.java),
          mock(IPermissionManager::class.java),
          true)
    }

    // Assert that the login screen is shown on launch
    composeTestRule.onNodeWithTag("loginScreen").assertExists()
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
          locationViewModel,
          Route.AROUND_YOU,
          FirebaseAuth.getInstance(),
          mockPermissionManager,
          true)
    }

    /**
     * @Test fun testBottomNavigationBar() = runTest { composeTestRule.setContent {
     *   IcebreakrrNavHost( mockProfileViewModel, tagsViewModel, mockFilterViewModel,
     *   mockMeetingRequestViewModel, appDataStore, locationViewModel, Route.AROUND_YOU,
     *   FirebaseAuth.getInstance(), true) }
     *
     * // Check that the "Around You" screen is displayed after login
     * composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
     *
     * // Test navigation to the Settings screen
     * composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
     * composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
     *
     * // Test navigation to the AroundYou screen
     * composeTestRule.onNodeWithTag("navItem_${R.string.heatmap}").performClick()
     * composeTestRule.onNodeWithTag("heatMapScreen").assertIsDisplayed()
     *
     * // Test navigation to the Notifications screen
     * composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()
     * composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
     *
     * // Test navigation to the AroundYou screen
     * composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()
     * composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed() } }
     */
