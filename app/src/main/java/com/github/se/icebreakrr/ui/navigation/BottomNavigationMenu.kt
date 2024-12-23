package com.github.se.icebreakrr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private const val BADGE_OFFSET_X = 15
private const val BADGE_OFFSET_Y = -5
private const val BADGE_SIZE = 17
private const val ICON_SIZE = 30

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
 * @param notificationCount : number of notification pending
 *
 * The menu's style adapts to the app's theme, and includes a tag for UI testing.
 */
@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String,
    notificationCount: Int,
    heatMapCount: Int
) {
  NavigationBar(
      modifier = Modifier.fillMaxWidth().height(70.dp).testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.primary) {
        tabList.forEach { tab ->
          NavigationBarItem(
              icon = {
                Box {
                  Icon(
                      tab.icon,
                      contentDescription = stringResource(id = tab.textId),
                      modifier = Modifier.size(ICON_SIZE.dp))
                  if (tab.route == Route.NOTIFICATIONS && notificationCount > 0) {
                    Badge(notificationCount, "badgeNotification")
                  }
                  if (tab.route == Route.MAP && heatMapCount > 0) {
                    Badge(heatMapCount, "badgeHeatmap")
                  }
                }
              },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              colors =
                  NavigationBarItemDefaults.colors(
                      selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                      unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                      selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                      unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                      indicatorColor = Color.Transparent),
              modifier = Modifier.testTag("navItem_${LocalContext.current.getString(tab.textId)}"))
        }
      }
}

/**
 * Badge composable put in the navigation bar item notification
 *
 * @param count : number of pending notifications
 */
@Composable
fun Badge(count: Int, tag: String) {
  Box(
      modifier =
          Modifier.offset(x = BADGE_OFFSET_X.dp, y = (BADGE_OFFSET_Y).dp)
              .size(BADGE_SIZE.dp)
              .background(Color.Red, shape = CircleShape)
              .testTag(tag),
      contentAlignment = Alignment.Center) {
        Text(
            text = count.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
        )
      }
}
