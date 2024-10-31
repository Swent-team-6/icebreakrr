package com.github.se.icebreakrr.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AroundYouProfileTest {
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock()
  }

  @Test
  fun AroundYouProfileDisplayTagTest() {
    composeTestRule.setContent { AroundYouProfile(navigationActions) }
    composeTestRule.onNodeWithTag("aroundYouProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("catchPhrase").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tagSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  }

  @Test
  fun AroundYouProfileGoBackButtonMessage() {
    composeTestRule.setContent { AroundYouProfile(navigationActions) }
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun AroundYouProfileMessageTest() {
    composeTestRule.setContent { AroundYouProfile(navigationActions) }
    composeTestRule.onNodeWithTag("requestButton").performClick()

    composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sendButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("messageTextField").assertIsDisplayed()

    composeTestRule.onNodeWithTag("messageTextField").performTextInput("New message")
    composeTestRule.onNodeWithTag("messageTextField").assertTextContains("New message")

    composeTestRule.onNodeWithTag("sendButton").performClick()
    composeTestRule.onNodeWithTag("bluredBackground").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("requestButton").performClick()

    composeTestRule.onNodeWithTag("bluredBackground").performClick()
    composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()

    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.onNodeWithTag("bluredBackground").assertIsNotDisplayed()
  }
}
