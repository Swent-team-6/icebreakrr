package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class TopBarTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testTopBarBasicDisplay() {
    composeTestRule.setContent { TopBar("Test Title") }

    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertDoesNotExist()
  }

  @Test
  fun testTopBarWithBackButton() {
    val onBackClick = mock<() -> Unit>()

    composeTestRule.setContent {
      TopBar("Test Title", needBackButton = true, backButtonOnClick = onBackClick)
    }

    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
  }

  @Test
  fun testBackButtonClick() {
    val onBackClick = mock<() -> Unit>()

    composeTestRule.setContent {
      TopBar("Test Title", needBackButton = true, backButtonOnClick = onBackClick)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(onBackClick).invoke()
  }

  @Test
  fun testTopBarTextAlignment() {
    composeTestRule.setContent { TopBar("Test Title", needBackButton = true) }

    composeTestRule.onNodeWithText("Test Title").assertIsDisplayed().assertHasNoClickAction()
  }

  @Test
  fun testBackButtonContentDescription() {
    composeTestRule.setContent { TopBar("Test Title", needBackButton = true) }

    composeTestRule.onNode(hasContentDescription("Go back")).assertIsDisplayed()
  }
}
