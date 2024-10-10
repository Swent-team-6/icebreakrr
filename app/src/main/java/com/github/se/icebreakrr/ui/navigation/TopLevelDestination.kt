package com.github.se.icebreakrr.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.se.icebreakrr.R

/** Data class representing a top-level destination in the navigation bar. */
data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    @StringRes val textId: Int
)

object Route {
  const val AUTH = "Auth"
  const val AROUND_YOU = "AroundYou"
  const val PROFILE = "Profile"
  const val NOTIFICATIONS = "Notifications"
}

object Screen {
  const val AUTH = "Auth Screen"
  const val AROUND_YOU = "Around You Screen"
  const val PROFILE = "Profile Screen"
  const val NOTIFICATIONS = "Notifications Screen"
}

object TopLevelDestinations {
  val AROUND_YOU =
      TopLevelDestination(
          route = Route.AROUND_YOU, icon = Icons.Outlined.Place, textId = R.string.around_you)
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE, icon = Icons.Outlined.Person, textId = R.string.profile)
  val NOTIFICATIONS =
      TopLevelDestination(
          route = Route.NOTIFICATIONS,
          icon = Icons.Outlined.Notifications,
          textId = R.string.notifications)
}

val LIST_TOP_LEVEL_DESTINATIONS =
    listOf(
        TopLevelDestinations.PROFILE,
        TopLevelDestinations.AROUND_YOU,
        TopLevelDestinations.NOTIFICATIONS)
