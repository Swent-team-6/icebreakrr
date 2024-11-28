package com.github.se.icebreakrr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue

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
    selectedItem: String,
    notificationCount: Int
) {
  NavigationBar(
      modifier = Modifier.fillMaxWidth().height(60.dp).testTag("bottomNavigationMenu"),
      containerColor = IceBreakrrBlue,
      contentColor = Color.Gray) {
        tabList.forEach { tab ->
          NavigationBarItem(
              icon = {
                Box {
                  Icon(tab.icon, contentDescription = stringResource(id = tab.textId))
                  if (tab.route == Route.NOTIFICATIONS && notificationCount > 0) {
                    Badge(count = notificationCount)
                  }
                }
              },
              label = { Text(stringResource(id = tab.textId)) },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              colors =
                  NavigationBarItemDefaults.colors(
                      selectedIconColor = Color.Gray,
                      unselectedIconColor = Color.White,
                      selectedTextColor = Color.White,
                      unselectedTextColor = Color.White,
                  ),
              modifier = Modifier.testTag("navItem_${tab.textId}"))
        }
      }
}

@Composable
fun Badge(count: Int) {
  Box(
      modifier =
          Modifier.offset(x = 10.dp, y = (-5).dp) // Manually position the badge
              .size(17.dp) // Badge size
              .background(Color.Red, shape = CircleShape),
      contentAlignment = Alignment.Center // Center the text within the badge
      ) {
        Text(
            text = count.toString(),
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
      }
}
