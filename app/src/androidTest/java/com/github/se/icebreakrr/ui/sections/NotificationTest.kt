package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.MockProfileViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NotificationTest {
  private lateinit var navigationActionsMock: NavigationActions
  private lateinit var profileViewModel: MockProfileViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActionsMock = mock()
    profileViewModel = MockProfileViewModel()
  }

  @Test
  fun notificationIsDisplayedOnLaunch() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
  }

  @Test
  fun allIsDisplayed() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationSecondText").performScrollTo()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
  }

  @Test
  fun navigationToFilterWorks() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").performClick()
    verify(navigationActionsMock).navigateTo(screen = Screen.FILTER)
  }

  @Test
  fun profilesListIsEmpty() {
    profileViewModel.clearProfiles()
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("notificationSecondText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
  }

  @Test
  fun profilesListNotEmpty() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").assertCountEquals(24)
  }

  @Test
  fun bottomNavigationBarTest() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
    verify(navigationActionsMock).navigateTo(TopLevelDestinations.SETTINGS)

    composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()
    verify(navigationActionsMock).navigateTo(TopLevelDestinations.AROUND_YOU)
  }

  @Test
  fun clickOnCardEvent() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock, profileViewModel) }
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()
    verify(navigationActionsMock).navigateTo(Screen.PROFILE_EDIT)
  }
}
