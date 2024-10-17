package com.github.se.endToEnd

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.MainActivity
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M1_Test {

  private lateinit var navigationActions: NavigationActions

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
  fun end_to_end_test_1() {
    // Screen 1 : Login Screen
    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("IceBreakrr")
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // Screen 2 : Around You Screen
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").performClick()

    // Screen 3 : Filter Screen and Click on Men
    composeTestRule.onNodeWithTag("FilterTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AgeRangeTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AgeFromTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AgeToTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TagsTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FilterButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    composeTestRule.onNodeWithTag("FilterTopBar").onChildAt(0).performClick()

    // Screen 4 : Go back to Around You
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()

    // Screen 5 : Notification Screen
    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationSecondText").performScrollTo()
    composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()

    // Screen 6 : Go back to Around You and click on card
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()

    // Screen 7 : Profile Screen
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertTextEquals("John Do")
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Go to the settings from Around You
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()

    // Screen 8 : Setting Screen
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Toggle Location").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Toggle Location").performClick()
    composeTestRule.onNodeWithTag("Option 1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Option 1").performClick()
    composeTestRule.onNodeWithTag("logOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logOutButton").performClick()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
