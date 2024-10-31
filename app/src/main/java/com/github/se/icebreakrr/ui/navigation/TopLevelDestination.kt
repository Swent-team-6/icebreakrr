package com.github.se.icebreakrr.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.ui.theme.GroupsIcon
import com.github.se.icebreakrr.ui.theme.NotificationsIcon

/** Data class representing a top-level destination in the navigation bar. */
data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    @StringRes val textId: Int
)

object Route {
  const val AUTH = "Auth"
  const val AROUND_YOU = "AroundYou"
  const val SETTINGS = "Settings"
  const val NOTIFICATIONS = "Notifications"
  const val PROFILE_EDIT = "ProfileEdit"
  const val FILTER = "Filter"
}

object Screen {
  const val AUTH = "Auth Screen"
  const val AROUND_YOU = "Around You Screen"
  const val SETTINGS = "Settings Screen"
  const val NOTIFICATIONS = "Notifications Screen"
  const val PROFILE_EDIT = "Profile Edit Screen"
  const val FILTER = "Filter Screen"
  const val PROFILE = "Profile Screen"
  const val AROUND_YOU_PROFILE = "Around You Profile Screen"
}

object TopLevelDestinations {
  val AROUND_YOU =
      TopLevelDestination(route = Route.AROUND_YOU, icon = GroupsIcon, textId = R.string.around_you)
  val SETTINGS =
      TopLevelDestination(
          route = Route.SETTINGS, icon = Icons.TwoTone.AccountCircle, textId = R.string.settings)
  val NOTIFICATIONS =
      TopLevelDestination(
          route = Route.NOTIFICATIONS, icon = NotificationsIcon, textId = R.string.notifications)
}

val LIST_TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestinations.SETTINGS,
        TopLevelDestinations.AROUND_YOU,
        TopLevelDestinations.NOTIFICATIONS)
