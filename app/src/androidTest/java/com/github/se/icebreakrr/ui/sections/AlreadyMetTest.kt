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
import org.mockito.kotlin.verify

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

    // Get Alice's profile and her already met profiles
    myProfile = Profile.getMockedProfiles()[0]
    alreadyMetProfiles = Profile.getMockedProfiles().filter { it.uid in myProfile.hasAlreadyMet }

    // Set up fake view model
    fakeProfilesViewModel.setSelfProfile(myProfile)
    fakeProfilesViewModel.setLoading(false)

    // Mock repository state flows
    Mockito.`when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
    Mockito.`when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

    // Mock repository to return already met profiles
    Mockito.`when`(mockProfilesRepository.getMultipleProfiles(any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccessCallback = invocation.getArgument<(List<Profile>) -> Unit>(1)
      onSuccessCallback(alreadyMetProfiles)
      null
    }
  }

  @Test
  fun testAlreadyMetScreenDisplaysCorrectly() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    composeTestRule.onNodeWithTag("profileListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
  }

  @Test
  fun testAlreadyMetScreenDisplaysCorrectlyWithProfiles() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }
    profilesViewModel.getAlreadyMetUsers()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    alreadyMetProfiles.forEach { composeTestRule.onNodeWithText(it.name).assertExists() }
  }

  @Test
  fun testAlreadyMetScreenDisplaysCorrectlyWithNoProfiles() = runTest {
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

    composeTestRule
        .onNodeWithText(context.getString(R.string.no_already_met_users))
        .assertIsDisplayed()
  }

  @Test
  fun testUnmeetUserDialogDisplayed() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }

    profilesViewModel.getAlreadyMetUsers()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(alreadyMetProfiles[0].name).performClick()
    composeTestRule.onNodeWithTag("confirmDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_yes)).assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_no)).assertIsDisplayed()
  }

  @Test
  fun testUnmeetUserCancellation() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = profilesViewModel,
          isTestMode = true)
    }

    profilesViewModel.getAlreadyMetUsers()
    composeTestRule.waitForIdle()

    val firstProfile = alreadyMetProfiles[0]
    composeTestRule.onNodeWithText(firstProfile.name).performClick()
    composeTestRule.onNodeWithText(context.getString(R.string.unmeet_no)).performClick()
    composeTestRule.onNodeWithTag("confirmDialog").assertDoesNotExist()
    composeTestRule.onNodeWithText(firstProfile.name).assertExists()
  }

  @Test
  fun testNavigationBackButton() = runTest {
    composeTestRule.setContent {
      AlreadyMetScreen(
          navigationActions = navigationActions,
          profilesViewModel = fakeProfilesViewModel,
          isTestMode = true)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationActions).goBack()
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

    composeTestRule.onNodeWithTag("refreshIndicator").assertExists()
  }
}
