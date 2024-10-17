package com.github.se.icebreakrr.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Composable function that creates a bottom navigation menu.
 *
 * This menu is used for navigating between the top-level destinations in the app. It displays icons
 * and labels for each tab, and highlights the currently selected tab.
 *
 * @param onTabSelect A lambda function that is called when a tab is selected, receiving the
 *   selected [TopLevelDestination] as a parameter.
 * @param tabList A list of [TopLevelDestination] objects that define the available tabs.
 * @param selectedItem The route of the currently selected tab, used to highlight the active tab.
 *
 * The menu's style adapts to the app's theme, and includes a tag for UI testing.
 */
@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  NavigationBar(
      modifier = Modifier.fillMaxWidth().height(60.dp).testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.surface) {
        tabList.forEach { tab ->
          NavigationBarItem(
              icon = { Icon(tab.icon, contentDescription = stringResource(id = tab.textId)) },
              label = { Text(stringResource(id = tab.textId)) },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              colors =
                  NavigationBarItemDefaults.colors(
                      selectedIconColor = MaterialTheme.colorScheme.primary,
                      unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      selectedTextColor = MaterialTheme.colorScheme.primary,
                      unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
              modifier = Modifier.testTag("navItem_${tab.textId}"))
        }
      }
}
