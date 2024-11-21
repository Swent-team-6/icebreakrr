package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

// This File was written with the help of Cursor
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsTest {
  private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val tempFolder = TemporaryFolder()

  private lateinit var navigationActionsMock: NavigationActions
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var appDataStore: AppDataStore

  @Before
  fun setUp() {
    // Set up real DataStore with test scope
    testDataStore =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempFolder.newFolder(), "test_preferences.preferences_pb") })
    appDataStore = AppDataStore(testDataStore)

    // Set up other mocks
    navigationActionsMock = mock()
    mockProfilesRepository = Mockito.mock(ProfilesRepository::class.java)

    // Create a mock storage reference
    val mockStorage = mock<FirebaseStorage>()
    val mockStorageRef = mock<StorageReference>()
    `when`(mockStorage.reference).thenReturn(mockStorageRef)

    // Initialize profilesViewModel with proper mocks
    profilesViewModel =
        ProfilesViewModel(
            mockProfilesRepository, ProfilePicRepositoryStorage(mockStorage), mock<FirebaseAuth>())

    // Mock necessary repository Flow returns
    `when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    `when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

    `when`(navigationActionsMock.currentRoute()).thenReturn(Route.SETTINGS)
  }

  @Test
  fun testProfileSettingsScreenDisplaysCorrectly() = runTest {
    composeTestRule.setContent {
      SettingsScreen(profilesViewModel, navigationActionsMock, appDataStore, mock<FirebaseAuth>())
    }

    // Assert that the top bar is displayed
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()

    // Assert that the profile card is displayed
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()

    // Assert that the toggle options are displayed and clickable
    composeTestRule.onNodeWithTag("Toggle Discoverability").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Toggle Discoverability").performClick()

    // Assert that the Log Out button is displayed and clickable
    composeTestRule.onNodeWithTag("logOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logOutButton").performClick()

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun testNavigationActionsOnProfileCardClick() = runTest {
    composeTestRule.setContent {
      SettingsScreen(profilesViewModel, navigationActionsMock, appDataStore, mock<FirebaseAuth>())
    }

    composeTestRule.onNodeWithTag("profileCard").performClick()
    verify(navigationActionsMock).navigateTo(Screen.PROFILE)
  }

  @Test
  fun testToggleSwitchStateChange() = runTest {
    // Set initial state
    appDataStore.saveDiscoverableStatus(false)

    composeTestRule.setContent {
      SettingsScreen(profilesViewModel, navigationActionsMock, appDataStore, mock<FirebaseAuth>())
    }

    // Initial state check
    composeTestRule.onNodeWithTag("switchToggle Discoverability").assertIsOff()

    // Toggle the switch
    composeTestRule.onNodeWithTag("switchToggle Discoverability").performClick()

    // Verify new state
    composeTestRule.onNodeWithTag("switchToggle Discoverability").assertIsOn()
    assertEquals(true, appDataStore.isDiscoverable.first())
  }
}
