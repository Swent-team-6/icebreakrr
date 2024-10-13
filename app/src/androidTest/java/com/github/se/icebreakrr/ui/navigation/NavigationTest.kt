package com.github.se.icebreakrr.ui.navigation

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.MainActivity
import com.github.se.icebreakrr.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setup() {

    // Creating a custom flag to disable the sign in to correctly test navigation
    val intent =
        Intent(composeTestRule.activity, MainActivity::class.java).apply {
          putExtra("IS_TESTING", true) // Pass the testing flag via intent
        }

    ActivityScenario.launch<MainActivity>(intent)
  }

  @Test
  fun loginScreenIsDisplayedOnLaunch() {
    // Assert that the login screen is shown on launch
    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
  }

  @Test
  fun testNavigationAfterLogin() {
    // Simulate clicking the login button
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // Check that the "Around You" screen is displayed after login
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()

    // Test navigation to the Settings screen
    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

    // Test navigation to the Notifications screen
    composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
  }
}
