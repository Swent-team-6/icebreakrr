package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.profile.UnblockProfileScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.not
import org.mockito.kotlin.verify

class UnblockProfileTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var fakeProfilesViewModel: MockProfileViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var blockedProfiles: List<Profile>
  private lateinit var myProfile: Profile

  @Before
  fun setUp() {
    // Set up mocks
    navigationActions = mock()
    mockProfilesRepository = Mockito.mock(ProfilesRepository::class.java)
    profilesViewModel =
        ProfilesViewModel(mockProfilesRepository, ProfilePicRepositoryStorage(mock()), mock())
    fakeProfilesViewModel = MockProfileViewModel()

    myProfile = Profile.getMockedProfiles()[0]
    fakeProfilesViewModel.setSelfProfile(myProfile)

    `when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    `when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

    myProfile = fakeProfilesViewModel.profiles.value[0]
    blockedProfiles = fakeProfilesViewModel.profiles.value.filter { it.uid in myProfile.hasBlocked }

    // Simulates the ViewModel fetching blocked profiles through the repository
    `when`(mockProfilesRepository.getBlockedProfiles(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(1)
      onSuccessCallback(blockedProfiles)
      null
    }
  }

  @Test
  fun testUnblockScreenDisplaysCorrectly() = runTest {
    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    composeTestRule.onNodeWithTag("unblockScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
  }

  @Test
  fun testUnblockScreenDisplaysCorrectlyWithProfiles() = runTest {
    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    profilesViewModel.getBlockedUsers()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    blockedProfiles.forEach { composeTestRule.onNodeWithText(it.name).assertIsDisplayed() }
  }

  @Test
  fun testUnblockScreenDisplaysCorrectlyWithNoProfiles() = runTest {
    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    `when`(mockProfilesRepository.getBlockedProfiles(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(1)
      onSuccessCallback(emptyList())
      null
    }

    profilesViewModel.getBlockedUsers()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("You have not blocked any users!").assertIsDisplayed()
  }

  @Test
  fun testUnblockFlow() = runTest {
    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    profilesViewModel.getBlockedUsers()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(blockedProfiles[0].name).performClick()
    composeTestRule.onNodeWithTag("unblockDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Yes").assertIsDisplayed()
    composeTestRule.onNodeWithText("No").assertIsDisplayed()
  }

  @Test
  fun testUnblockFlowButNo() = runTest {
    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    profilesViewModel.getBlockedUsers()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(blockedProfiles[0].name).performClick()
    composeTestRule.onNodeWithText("No").performClick()
    composeTestRule.onNodeWithTag("unblockDialog").assertIsNotDisplayed()
    composeTestRule.onNodeWithText(blockedProfiles[0].name).assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() = runTest {
    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    // Click back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Verify navigation action was called
    verify(navigationActions).goBack()
  }

  @Test
  fun testPullToRefresh() = runTest {
    fakeProfilesViewModel.updateIsConnected(true)

    composeTestRule.setContent {
      UnblockProfileScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    // Verify refresh indicator exists
    composeTestRule.onNodeWithTag("refreshIndicator").assertExists()
  }
}
