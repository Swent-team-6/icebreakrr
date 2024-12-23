package com.github.se.icebreakrr.ui.sections

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.espresso.action.ViewActions.swipeDown
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.location.ILocationService
import com.github.se.icebreakrr.model.location.LocationRepository
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.notification.EngagementNotificationManager
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.sort.SortViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.utils.IPermissionManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.functions.FirebaseFunctions
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class)
class AroundYouScreenTest {
  private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockPPRepository: ProfilePicRepository
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var engagementNotificationManager: EngagementNotificationManager
  private lateinit var sortViewModel: SortViewModel
  private lateinit var mockLocationService: ILocationService
  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var mockPermissionManager: IPermissionManager
  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var appDataStore: AppDataStore
  private lateinit var mockContext: Context
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var functions: FirebaseFunctions
  private lateinit var filterViewModel: FilterViewModel
  private lateinit var tagsViewModel: TagsViewModel

  private lateinit var locationViewModel: LocationViewModel

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val tempFolder = TemporaryFolder()

  @Before
  fun setUp() = runTest {
    // Set up real DataStore with test scope
    testDataStore =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempFolder.newFolder(), "test_preferences.preferences_pb") })
    appDataStore = AppDataStore(testDataStore)
    appDataStore.saveDiscoverableStatus(true)

    // Set up mocks
    navigationActions = mock(NavigationActions::class.java)
    mockProfilesRepository = mock(ProfilesRepository::class.java)
    mockPPRepository = mock(ProfilePicRepository::class.java)
    profilesViewModel =
        ProfilesViewModel(mockProfilesRepository, mockPPRepository, mock(FirebaseAuth::class.java))

    sortViewModel = SortViewModel(profilesViewModel)
    mockLocationService = mock(ILocationService::class.java)
    mockLocationRepository = mock(LocationRepository::class.java)
    mockPermissionManager = mock(IPermissionManager::class.java)
    mockContext = mock(Context::class.java)
    locationViewModel =
        LocationViewModel(
            mockLocationService, mockLocationRepository, mockPermissionManager, mockContext)

    // Mock state flows
    `when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    `when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))
    `when`(mockPermissionManager.permissionStatuses)
        .thenReturn(
            MutableStateFlow(
                mapOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION to
                        android.content.pm.PackageManager.PERMISSION_GRANTED)))
    // Mock successful connection check
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(emptyList())
      null
    }
    tagsViewModel =
        TagsViewModel(
            TagsRepository(mock(FirebaseFirestore::class.java), mock(FirebaseAuth::class.java)))
    filterViewModel = FilterViewModel()
    functions = mock(FirebaseFunctions::class.java)
    meetingRequestViewModel = MeetingRequestViewModel(profilesViewModel, functions)
    engagementNotificationManager =
        EngagementNotificationManager(
            profilesViewModel,
            meetingRequestViewModel,
            appDataStore,
            filterViewModel,
            tagsViewModel,
            permissionManager = mockPermissionManager)

    // Mock initial behavior of repository
    `when`(navigationActions.currentRoute()).thenReturn(Route.AROUND_YOU)

    composeTestRule.setContent {
      AroundYouScreen(
          navigationActions,
          profilesViewModel,
          viewModel(
              factory =
                  TagsViewModel.Companion.Factory(
                      mock(FirebaseAuth::class.java), mock(FirebaseFirestore::class.java))),
          viewModel(factory = FilterViewModel.Factory),
          locationViewModel,
          sortViewModel,
          mockPermissionManager,
          appDataStore,
          isTestMode = true,
          engagementManager = engagementNotificationManager)
    }

    // Trigger initial connection check
    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300)
  }

  @Test
  fun displayTextWhenEmpty() = runTest {
    // Mock the repository behavior
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(emptyList())
      null
    }

    // Trigger the fetch
    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert the empty state text is displayed
    composeTestRule.onNodeWithTag("emptyProfilePrompt").assertExists().assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
  }

  @Test
  fun navigationOnCardClick() {
    // Create a mock profile
    val profile = mockProfile()

    assertTrue(profilesViewModel.isConnected.value)

    // Mock the repository behavior
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(listOf(profile))
      null
    }

    // Trigger the fetch
    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300)

    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()
    verify(navigationActions).navigateTo(screen = Screen.OTHER_PROFILE_VIEW + "?userId=1")
  }

  @Test
  fun testRefreshMechanism() {
    // Step 1: Set up initial state with one profile
    val profile = mockProfile()
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(listOf(profile))
      null
    }

    // Step 2: Fetch profiles initially and verify profile card is displayed
    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300)
    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()

    // Step 3: Simulate empty list response from repository
    `when`(mockProfilesRepository.getProfilesInRadius(any(), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(2)
      onSuccessCallback(emptyList())
      null
    }

    // Step 4: Perform swipe down to trigger refresh
    composeTestRule.onNodeWithTag("aroundYouScreen").performTouchInput { swipeDown() }

    // Step 5: Wait and check if refresh indicator is displayed
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("refreshIndicator").assertIsDisplayed()

    // Step 6: Verify empty profile prompt after refresh completes
    profilesViewModel.getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("emptyProfilePrompt").assertIsDisplayed()
  }

  @Test
  fun navigationOnFabClick() {
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").performClick()
    verify(navigationActions).navigateTo(screen = Screen.FILTER)
  }

  @Test
  fun sortDropdownDisplaysCorrectly() {
    // Check initial display
    composeTestRule
        .onNodeWithTag("SortOptionsDropdown_Selected")
        .assertExists()
        .assertIsDisplayed()
        .assertTextContains("Sort by: Distance")

    // Click to expand the dropdown
    composeTestRule.onNodeWithTag("SortOptionsDropdown_Selected").performClick()

    // Check that the options are displayed
    composeTestRule
        .onNodeWithTag("SortOptionsDropdown_Option_AGE")
        .assertExists()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("SortOptionsDropdown_Option_COMMON_TAGS")
        .assertExists()
        .assertIsDisplayed()

    // Select an option
    composeTestRule.onNodeWithTag("SortOptionsDropdown_Option_AGE").performClick()

    // Check that the selected option is updated
    composeTestRule.onNodeWithTag("SortOptionsDropdown_Selected").assertTextContains("Sort by: Age")
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
}
