package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

// These tests were generated with the help of CursorAI
class UnsavedChangesDialogTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testDialogVisibility() {
    var showDialog by mutableStateOf(false)
    val onDismiss = mock<() -> Unit>()
    val onConfirm = mock<() -> Unit>()

    composeTestRule.setContent {
      UnsavedChangesDialog(showDialog = showDialog, onDismiss = onDismiss, onConfirm = onConfirm)
    }

    // Test dialog not shown initially
    composeTestRule.onNodeWithTag("alertDialog").assertDoesNotExist()

    // Test dialog shown after changing state
    showDialog = true
    composeTestRule.onNodeWithTag("alertDialog").assertIsDisplayed()
  }

  @Test
  fun testDialogContent() {
    composeTestRule.setContent {
      UnsavedChangesDialog(showDialog = true, onDismiss = {}, onConfirm = {})
    }

    // Verify dialog text content
    composeTestRule.onNodeWithText("You are about to leave this page").assertIsDisplayed()
    composeTestRule.onNodeWithText("Your changes will not be saved.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    composeTestRule.onNodeWithText("Discard changes").assertIsDisplayed()
  }

  @Test
  fun testDialogButtons() {
    val onDismiss = mock<() -> Unit>()
    val onConfirm = mock<() -> Unit>()

    composeTestRule.setContent {
      UnsavedChangesDialog(showDialog = true, onDismiss = onDismiss, onConfirm = onConfirm)
    }

    // Test Cancel button
    composeTestRule.onNodeWithText("Cancel").performClick()
    verify(onDismiss).invoke()
    verifyNoInteractions(onConfirm)

    // Test Discard button
    composeTestRule.onNodeWithText("Discard changes").performClick()
    verify(onConfirm).invoke()
  }
}
