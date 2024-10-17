package com.github.se.icebreakrr.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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

  @Before fun setUp() {}

  @Test
  fun aroundYouScreenIsDisplayedOnLaunch() {
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationMenuIsDisplayed() {
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun navigationWorks() {
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("editButton").performClick()
    composeTestRule.onNodeWithTag("profileEditScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("discardChangesOption").performClick()

    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
  }
}
