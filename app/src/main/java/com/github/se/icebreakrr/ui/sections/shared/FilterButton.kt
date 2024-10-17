package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.theme.FilterIcon
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue

@Composable
fun FilterFloatingActionButton(navigationActions: NavigationActions) {
  FloatingActionButton(
      onClick = { navigationActions.navigateTo(Screen.FILTER) },
      modifier = Modifier.testTag("filterButton"),
      containerColor = IceBreakrrBlue,
      contentColor = Color.White) {
        Icon(imageVector = FilterIcon, contentDescription = "Filter")
      }
}
