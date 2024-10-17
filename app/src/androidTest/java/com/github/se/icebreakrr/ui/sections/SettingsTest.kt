package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

// This File was written with the help of Cursor
class SettingsTest {
  private lateinit var navigationActionsMock: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActionsMock = mock()

    `when`(navigationActionsMock.currentRoute()).thenReturn(Route.SETTINGS)
  }

  @Test
  fun testProfileSettingsScreenDisplaysCorrectly() {

    // Set the content to the ProfileSettingsScreen
    composeTestRule.setContent { SettingsScreen(navigationActionsMock) }

    // Assert that the top bar is displayed
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()

    // Assert that the profile card is displayed
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()

    // Assert that the toggle options are displayed and clickable
    composeTestRule.onNodeWithTag("Toggle Location").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Toggle Location").performClick()
    composeTestRule.onNodeWithTag("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Option 1").performClick()

    // Assert that the Log Out button is displayed and clickable
    composeTestRule.onNodeWithTag("logOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logOutButton").performClick()

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun testNavigationActionsOnProfileCardClick() {
    composeTestRule.setContent { SettingsScreen(navigationActionsMock) }

    // Click the profile card
    composeTestRule.onNodeWithTag("profileCard").performClick()

    // Verify that the correct navigation action is triggered
    verify(navigationActionsMock).navigateTo(Screen.PROFILE)
  }

  @Test
  fun testToggleSwitchStateChange() {
    composeTestRule.setContent { SettingsScreen(navigationActionsMock) }

    // Initial state: Verify that the switch is initially off (unchecked)
    composeTestRule.onNodeWithTag("switchToggle Location").assertIsOff()

    // Perform a click to toggle the switch
    composeTestRule.onNodeWithTag("switchToggle Location").performClick()

    // After clicking, verify that the switch is now on (checked)
    composeTestRule.onNodeWithTag("switchToggle Location").assertIsOn()
  }
}
