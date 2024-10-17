package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ProfileEditingScreenTest {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock()
  }

  @Test
  fun testProfileEditingScreenUI() {

    composeTestRule.setContent { ProfileEditingScreen(navigationActions) }

    // Check if topAppBar is displayed
    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()

    // Check if profileEditScreenContent is displayed
    composeTestRule.onNodeWithTag("profileEditScreenContent").assertIsDisplayed()

    // Check if profile picture is displayed
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()

    // Check if name and age are displayed correctly
    composeTestRule.onNodeWithTag("nameAndAge").assertIsDisplayed()

    // Test catchphrase input
    composeTestRule.onNodeWithTag("catchphrase").performTextInput("New Catchphrase")
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains("New Catchphrase")

    // Test description input
    composeTestRule.onNodeWithTag("description").performTextInput("New Description")
    composeTestRule.onNodeWithTag("description").assertTextContains("New Description")

    // Test back button functionality
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("alertDialog").assertIsDisplayed()

    // Test dialog buttons
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag("alertDialog").assertIsNotDisplayed()

    // Test check button
    composeTestRule.onNodeWithTag("checkButton").assertIsDisplayed()
  }

  @Test
  fun testEdgeCases() {

    composeTestRule.setContent { ProfileEditingScreen(navigationActions) }

    // Test empty catchphrase
    composeTestRule.onNodeWithTag("description").performTextInput("New catchphrase")
    composeTestRule.onNodeWithTag("catchphrase").performTextClearance()
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains("")

    // Test empty description
    composeTestRule.onNodeWithTag("description").performTextInput("New Description")
    composeTestRule.onNodeWithTag("description").performTextClearance()
    composeTestRule.onNodeWithTag("description").assertTextContains("")

    // Test long text input
    val longText = "A".repeat(1000)
    composeTestRule.onNodeWithTag("description").performTextInput(longText)
    composeTestRule.onNodeWithTag("description").assertTextContains(longText.take(1000))
  }
}
