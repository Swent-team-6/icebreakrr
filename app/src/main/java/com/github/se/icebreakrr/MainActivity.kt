package com.github.se.icebreakrr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.github.se.icebreakrr.model.message.ChatScreen
import com.github.se.icebreakrr.model.message.EnterTokenDialog
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
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestNotificationPermission()
    auth = FirebaseAuth.getInstance()
    // Initialize Firebase Auth
    FirebaseApp.initializeApp(this)
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      // This is useful for testing purposes
      auth.signOut()
    }
    FirebaseAuth.getInstance()

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
  val ourUserUid = FirebaseAuth.getInstance().currentUser?.uid
  val meetingRequestViewModel: MeetingRequestViewModel =
      viewModel(factory = MeetingRequestViewModel.Companion.Factory(profileViewModel, ourUserUid))

  // Set up the Firebase Cloud Messaging system
  FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
      val token = task.result
      // Save token to backend for user messaging
      // meetingRequestViewModel.onRemoteTokenChange(token)
      Log.d(
          "FCM Token",
          "Token: $token") // todo : est se que on doit nous meme créer un profile si il existe pas
      // encore pour le user courant ?
    }
  }

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.AROUND_YOU,
        route = Route.AROUND_YOU,
    ) {
      composable(Screen.AROUND_YOU) {
        AroundYouScreen(navigationActions, profileViewModel, tagsViewModel)
      }
      composable(Screen.OTHER_PROFILE_VIEW) { OtherProfileView(navigationActions) }
    }

    navigation(
        startDestination = Screen.SETTINGS,
        route = Route.SETTINGS,
    ) {
      composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
      composable(Screen.PROFILE) { ProfileView(navigationActions) }
    }

    navigation(
        startDestination = Screen.NOTIFICATIONS,
        route = Route.NOTIFICATIONS,
    ) {
      composable(Screen.NOTIFICATIONS) { // Todo : this part is temporary, it ll be used for debugging
        //         NotificationScreen(navigationActions, profileViewModel)
        val state =
            meetingRequestViewModel
                .meetingRequestState
        // purposes
        if (state.isEnteringMessage) {
          EnterTokenDialog(
              token = state.targetToken,
              onTokenChange = meetingRequestViewModel::onRemoteTokenChange,
              onSubmit = meetingRequestViewModel::onSubmitMeetingRequest)
        } else {
          meetingRequestViewModel.sendMessage(false)
          NotificationScreen(navigationActions, profileViewModel)
        }
      }
    }

    navigation(
        startDestination = Screen.PROFILE_EDIT,
        route = Route.PROFILE_EDIT,
    ) {
      composable(Screen.PROFILE_EDIT) { ProfileEditingScreen(navigationActions, tagsViewModel) }
    }

    navigation(
        startDestination = Screen.FILTER,
        route = Route.FILTER,
    ) {
      composable(Screen.FILTER) { FilterScreen(navigationActions, tagsViewModel) }
    }
  }
}
