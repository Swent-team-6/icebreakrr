package com.github.se.icebreakrr.ui.sections

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.action.ViewActions
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.profile.UnblockProfileScreen
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.internal.wait
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.not
import org.mockito.kotlin.verify
import java.util.Calendar

class UnblockProfileTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var profilesViewModel: ProfilesViewModel
    private lateinit var fakeProfilesViewModel: MockProfileViewModel
    private lateinit var mockProfilesRepository: ProfilesRepository
    private lateinit var blockedProfiles : List<Profile>
    private lateinit var myProfile: Profile

    @Before
    fun setUp() {
        // Set up mocks
        navigationActions = mock()
        mockProfilesRepository =  Mockito.mock(ProfilesRepository::class.java)
        profilesViewModel = ProfilesViewModel(
            mockProfilesRepository,
            ProfilePicRepositoryStorage(mock())
        )
        fakeProfilesViewModel = MockProfileViewModel()

        myProfile = Profile.getMockedProfiles()[0]
        fakeProfilesViewModel.setSelfProfile(myProfile)

        `when`(mockProfilesRepository.isWaiting).thenReturn(MutableStateFlow(false))
        `when`(mockProfilesRepository.waitingDone).thenReturn(MutableStateFlow(false))

        myProfile = fakeProfilesViewModel.profiles.value[0]
        blockedProfiles = fakeProfilesViewModel.profiles.value.filter{ it.uid in myProfile.hasBlocked }

        // Simulates the ViewModel fetching blocked profiles through the repository
        `when`(mockProfilesRepository.getBlockedProfiles(any(),any(),any())).thenAnswer {
                invocation ->
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
                isTestMode = true
            )
        }
        composeTestRule.onNodeWithTag("unblockScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("unblockTopBar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("UnblockTopBarTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    }

    @Test
    fun testUnblockScreenDisplaysCorrectlyWithProfiles() = runTest {
        composeTestRule.setContent {
            UnblockProfileScreen(
                navigationActions = navigationActions,
                profilesViewModel = profilesViewModel,
                isTestMode = true
            )
        }
        profilesViewModel.getBlockedUsers()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
        blockedProfiles.forEach {
            composeTestRule.onNodeWithText(it.name).assertIsDisplayed()
        }

    }

    @Test
    fun testUnblockScreenDisplaysCorrectlyWithNoProfiles() = runTest {
        composeTestRule.setContent {
            UnblockProfileScreen(
                navigationActions = navigationActions,
                profilesViewModel = profilesViewModel,
                isTestMode = true
            )
        }
        `when`(mockProfilesRepository.getBlockedProfiles(any(),any(),any())).thenAnswer {
                invocation ->
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
    fun testUnblockButNo() = runTest {
        composeTestRule.setContent {
            UnblockProfileScreen(
                navigationActions = navigationActions,
                profilesViewModel = profilesViewModel,
                isTestMode = true
            )
        }
        profilesViewModel.getBlockedUsers()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(blockedProfiles[0].name).performClick()
        composeTestRule.onNodeWithTag("unblockDialog").assertIsDisplayed()

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
                isTestMode = true
            )
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
                isTestMode = true
            )
        }

        // Verify refresh indicator exists
        composeTestRule.onNodeWithTag("refreshIndicator").assertExists()
    }

}
