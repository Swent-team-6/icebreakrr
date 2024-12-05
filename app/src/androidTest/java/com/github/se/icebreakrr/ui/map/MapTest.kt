package com.github.se.icebreakrr.ui.map

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.model.location.ILocationService
import com.github.se.icebreakrr.model.location.LocationRepository
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.profile.MapScreen
import com.github.se.icebreakrr.utils.IPermissionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class MapTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockLocationService: ILocationService
  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var mockPermissionManager: IPermissionManager

  @Before
  fun setUp() {
    // Initialize mocks
    navigationActions = mock(NavigationActions::class.java)
    mockProfilesRepository = mock(ProfilesRepository::class.java)
    mockLocationService = mock(ILocationService::class.java)
    mockLocationRepository = mock(LocationRepository::class.java)
    mockPermissionManager = mock(IPermissionManager::class.java)

    // Initialize ViewModels
    profilesViewModel =
        ProfilesViewModel(
            mockProfilesRepository,
            ProfilePicRepositoryStorage(mock(FirebaseStorage::class.java)),
            mock(FirebaseAuth::class.java))

    locationViewModel =
        LocationViewModel(mockLocationService, mockLocationRepository, mockPermissionManager)
  }

  @Test
  fun testMapScreenDisplaysCorrectly() {
    composeTestRule.setContent {
      MapScreen(navigationActions, profilesViewModel, locationViewModel)
    }

    // Verify basic UI elements are displayed
    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
  }

  @Test
  fun testBottomNavigationDisplaysCorrectly() {
    composeTestRule.setContent {
      MapScreen(navigationActions, profilesViewModel, locationViewModel)
    }

    // Verify bottom navigation is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
