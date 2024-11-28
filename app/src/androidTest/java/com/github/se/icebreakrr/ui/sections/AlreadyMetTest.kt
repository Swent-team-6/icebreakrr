package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

// The file was written with the help of CursorAI
class AlreadyMetScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private lateinit var navigationActions: NavigationActions
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var fakeProfilesViewModel: MockProfileViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var alreadyMetProfiles: List<Profile>
  private lateinit var myProfile: Profile

  @Before
  fun setUp() {
    navigationActions = mock()
    mockProfilesRepository = Mockito.mock(ProfilesRepository::class.java)
    profilesViewModel =
        ProfilesViewModel(mockProfilesRepository, ProfilePicRepositoryStorage(mock()), mock())
    fakeProfilesViewModel = MockProfileViewModel()

    // Get Alice's profile (first mock profile)
    myProfile = Profile.getMockedProfiles()[0]

    // Get profiles that Alice has already met
    alreadyMetProfiles = Profile.getMockedProfiles().filter { it.uid in myProfile.hasAlreadyMet }

    // Set up fake view model with the already met profiles
    fakeProfilesViewModel.setSelfProfile(myProfile)
    fakeProfilesViewModel.setLoading(false)

    // Mock repository state flows
    Mockito.`when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    Mockito.`when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))
  }

  @Test
  fun testAlreadyMetScreenBasicLayout() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }

    // Verify basic layout components
    composeTestRule.onNodeWithTag("profileListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
  }

  @Test
  fun testAlreadyMetScreenWithProfiles() = runTest {
    // Set up the fake view model with the already met profiles
    fakeProfilesViewModel.setLoading(false)

    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    // Wait for UI to update
    composeTestRule.waitForIdle()

    // Verify profiles are displayed
    alreadyMetProfiles.forEach { profile ->
      composeTestRule.onNodeWithText(profile.name).assertExists()
      composeTestRule.onNodeWithText(profile.name).assertIsDisplayed()
    }
  }

  @Test
  fun testAlreadyMetScreenEmptyState() = runTest {
    // Mock repository to return empty list
    Mockito.`when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(1)
      onSuccessCallback(emptyList())
      null
    }

    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }

    profilesViewModel.getAlreadyMetUsers()
    composeTestRule.waitForIdle()

    // Verify empty state message
    composeTestRule
        .onNodeWithText(context.getString(R.string.no_already_met_users))
        .assertIsDisplayed()
  }

  @Test
  fun testUnmeetUserFlow() = runTest {
    fakeProfilesViewModel.setLoading(false)

    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    composeTestRule.waitForIdle()

    alreadyMetProfiles.first().let { profile ->
      composeTestRule.onNodeWithText(profile.name).performClick()
    }

    // Verify confirmation dialog
    composeTestRule.onNodeWithTag("confirmDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_confirm)).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(context.getString(R.string.unmeet_confirm_message))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_yes)).assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_no)).assertIsDisplayed()
  }

  @Test
  fun testUnmeetUserConfirmation() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    composeTestRule.waitForIdle()

    // Click on first already met profile
    val firstProfile = alreadyMetProfiles.first()
    composeTestRule.onNodeWithText(firstProfile.name).performClick()

    // Click Yes on confirmation dialog
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_yes)).performClick()

    // Wait for the UI to update after unmeet action
    composeTestRule.waitForIdle()

    // Dialog should be dismissed
    composeTestRule.onNodeWithTag("confirmDialog").assertDoesNotExist()
  }

  @Test
  fun testUnmeetUserCancellation() = runTest {
    fakeProfilesViewModel.setLoading(false)

    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    composeTestRule.waitForIdle()

    // Click on first already met profile
    val firstProfile = alreadyMetProfiles.first()
    composeTestRule.onNodeWithText(firstProfile.name).performClick()

    // Click No on confirmation dialog
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_no)).performClick()

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Dialog should be dismissed
    composeTestRule.onNodeWithTag("confirmDialog").assertDoesNotExist()

    // Profile should still be visible
    composeTestRule.onNodeWithText(firstProfile.name).assertIsDisplayed()
  }

  @Test
  fun testPullToRefresh() = runTest {
    fakeProfilesViewModel.updateIsConnected(true)

    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    // Verify refresh indicator exists
    composeTestRule.onNodeWithTag("refreshIndicator").assertExists()
  }
}
