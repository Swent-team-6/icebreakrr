package com.github.se.icebreakrr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.icebreakrr.config.LocalIsTesting
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.authentication.SignInScreen
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.profile.OtherProfileView
import com.github.se.icebreakrr.ui.profile.ProfileEditingScreen
import com.github.se.icebreakrr.ui.profile.ProfileView
import com.github.se.icebreakrr.ui.sections.AroundYouScreen
import com.github.se.icebreakrr.ui.sections.FilterScreen
import com.github.se.icebreakrr.ui.sections.NotificationScreen
import com.github.se.icebreakrr.ui.sections.SettingsScreen
import com.github.se.icebreakrr.ui.theme.SampleAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase Auth
    FirebaseApp.initializeApp(this)
    requestNotificationPermission()

    // Retrieve the testing flag from the Intent
    val isTesting = intent?.getBooleanExtra("IS_TESTING", false) ?: false

    setContent {
      // Provide the `isTesting` flag to the entire composable tree
      CompositionLocalProvider(LocalIsTesting provides isTesting) {
        SampleAppTheme { Surface(modifier = Modifier.fillMaxSize()) { IcebreakrrApp() } }
      }
    }
  }

  private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val hasPermission =
          ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
              PackageManager.PERMISSION_GRANTED

      if (!hasPermission) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
      }
    }
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
  val profileViewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Factory)
  val tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Factory)
  val filterViewModel: FilterViewModel = viewModel(factory = FilterViewModel.Factory)
  val ourUserUid = FirebaseAuth.getInstance().currentUser?.uid
  val ourName = FirebaseAuth.getInstance().currentUser?.displayName
  val functions = FirebaseFunctions.getInstance()
  val meetingRequestViewModel: MeetingRequestViewModel =
      viewModel(
          factory =
              MeetingRequestViewModel.Companion.Factory(
                  profileViewModel, functions, ourUserUid, ourName))

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) {
        SignInScreen(profileViewModel, meetingRequestViewModel, navigationActions)
      }
    }

    navigation(
        startDestination = Screen.AROUND_YOU,
        route = Route.AROUND_YOU,
    ) {
      composable(Screen.AROUND_YOU) {
        AroundYouScreen(navigationActions, profileViewModel, tagsViewModel, filterViewModel)
      }
      composable(Screen.OTHER_PROFILE_VIEW + "?userId={userId}") { navBackStackEntry ->
        OtherProfileView(
            profileViewModel,
            tagsViewModel,
            meetingRequestViewModel,
            navigationActions,
            navBackStackEntry)
      }
    }

    navigation(
        startDestination = Screen.SETTINGS,
        route = Route.SETTINGS,
    ) {
      composable(Screen.SETTINGS) { SettingsScreen(profileViewModel, navigationActions) }
      composable(Screen.PROFILE) { ProfileView(profileViewModel, tagsViewModel, navigationActions) }
    }

    navigation(
        startDestination = Screen.NOTIFICATIONS,
        route = Route.NOTIFICATIONS,
    ) {
      composable(Screen.NOTIFICATIONS) { NotificationScreen(navigationActions, profileViewModel) }
    }

    navigation(
        startDestination = Screen.PROFILE_EDIT,
        route = Route.PROFILE_EDIT,
    ) {
      composable(Screen.PROFILE_EDIT) {
        ProfileEditingScreen(navigationActions, tagsViewModel, profileViewModel)
      }
    }

    navigation(
        startDestination = Screen.FILTER,
        route = Route.FILTER,
    ) {
      composable(Screen.FILTER) {
        FilterScreen(navigationActions, tagsViewModel, filterViewModel, profileViewModel)
      }
    }
  }
}
