package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

// This File was generated with the help of Cursor AI

/**
 * A reusable dialog component that prompts users when they attempt to leave a screen with unsaved
 * changes. This dialog gives users two options:
 * 1. Cancel: Stay on the current screen
 * 2. Discard: Leave without saving changes
 *
 * @param showDialog Boolean flag to control the visibility of the dialog
 * @param onDismiss Callback function invoked when the user clicks "Cancel" or dismisses the dialog
 * @param onConfirm Callback function invoked when the user chooses to discard changes
 * @param modifier Optional modifier for customizing the dialog's appearance
 *
 * Example usage:
 * ```
 * UnsavedChangesDialog(
 *     showDialog = dialogState,
 *     onDismiss = { dialogState = false },
 *     onConfirm = {
 *         dialogState = false
 *         // Handle discarding changes
 *         navigationActions.goBack()
 *     }
 * )
 * ```
 */
@Composable
fun UnsavedChangesDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
  if (showDialog) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("You are about to leave this page") },
        text = { Text("Your changes will not be saved.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Discard changes", color=MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color=MaterialTheme.colorScheme.onSecondary) } },
        modifier = modifier.testTag("alertDialog"))
  }
}
