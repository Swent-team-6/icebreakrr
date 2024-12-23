package com.github.se.icebreakrr.ui.navigation

import androidx.navigation.NavHostController

open class NavigationActions(
    private val navController: NavHostController,
) {
  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to Clear the back stack when
   *   navigating to a new destination This is useful when navigating to a new screen from the
   *   bottom navigation bar as we don't want to keep the previous screen in the back stack
   */
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      popUpTo(0) {
        saveState = true
        inclusive = true
      }
      launchSingleTop = true
      if (destination.route != Route.AUTH) {
        restoreState = true
      }
    }
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    val currentRoute = navController.currentDestination?.route
    return if (currentRoute == null) {
      // Redirect to the authentication screen if the current route is null
      navController.navigate(Route.AUTH)
      Route.AUTH
    } else {
      currentRoute
    }
  }
}
