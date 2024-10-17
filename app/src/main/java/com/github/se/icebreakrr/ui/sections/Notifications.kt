package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions

/**
 * Composable function for displaying the notification screen.
 *
 * It includes a bottom navigation bar and displays the main content of the notification screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Factory)
) {
  Scaffold(
      modifier = Modifier.testTag("notificationScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = navigationActions.currentRoute())
      },
      content = { innerPadding ->
        // Use the innerPadding to apply padding around your content
        Text(
            text = "Notification screen", // TODO PLaceholder
            modifier = Modifier.padding(innerPadding) // Applying the padding to the content
            )
      })
}
