package com.github.se.icebreakrr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.AroundYouScreen
import com.github.se.icebreakrr.ui.sections.NotificationScreen
import com.github.se.icebreakrr.ui.sections.SettingsScreen
import com.github.se.icebreakrr.ui.theme.SampleAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
        auth.signOut()
    }
    setContent { SampleAppTheme { Surface(modifier = Modifier.fillMaxSize()) { IcebreakrrApp() } } }
  }
}

/**
 * Main composable function for the Icebreakrr app.
 *
 * This function sets up the navigation controller and defines the navigation graph for the app,
 * including screens such as "Around You", "Profile", and "Notifications".
 *
 * @see AroundYouScreen
 * @see SettingsScreen
 * @see NotificationScreen
 */
@Composable
fun IcebreakrrApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Factory)

  // TODO Implement Auth Screen navigation
  NavHost(navController = navController, startDestination = Route.AROUND_YOU) {
    navigation(
        startDestination = Screen.AROUND_YOU,
        route = Route.AROUND_YOU,
    ) {
      composable(Screen.AROUND_YOU) { AroundYouScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.SETTINGS,
        route = Route.SETTINGS,
    ) {
      composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.NOTIFICATIONS,
        route = Route.NOTIFICATIONS,
    ) {
      composable(Screen.NOTIFICATIONS) { NotificationScreen(navigationActions) }
    }
  }
}
