package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NotificationTest {
  private lateinit var navigationActionsMock: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActionsMock = mock()
  }

  @Test
  fun notificationIsDisplayedOnLaunch() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock) }
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
  }

  @Test
  fun allIsDisplayed() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock) }
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationSecondText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationTopAppBar").assertIsDisplayed()
  }

  @Test
  fun navigationToFilterWorks() {
    composeTestRule.setContent { NotificationScreen(navigationActionsMock) }
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").performClick()
    verify(navigationActionsMock).navigateTo(screen = Screen.FILTER)
  }
}
