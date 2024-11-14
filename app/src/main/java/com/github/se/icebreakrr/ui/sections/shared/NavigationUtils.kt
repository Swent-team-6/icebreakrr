package com.github.se.icebreakrr.ui.sections.shared

import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions

/**
 * Handles the back navigation action. If there are unsaved changes, it shows a confirmation dialog.
 * Otherwise, it navigates back to the previous screen.
 *
 * @param isModified A boolean indicating if there are unsaved changes.
 * @param setShowDialog A lambda function to update the showDialog state.
 * @param tagsViewModel The ViewModel for managing tags.
 * @param navigationActions The navigation actions for navigating between screens.
 */
fun handleSafeBackNavigation(
    isModified: Boolean,
    setShowDialog: (Boolean) -> Unit,
    tagsViewModel: TagsViewModel,
    navigationActions: NavigationActions
) {
  if (isModified) {
    setShowDialog(true)
  } else {
    tagsViewModel.leaveUI()
    navigationActions.goBack()
  }
}
