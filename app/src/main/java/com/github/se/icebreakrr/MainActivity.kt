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
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestManager
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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the testing flag from the Intent
        val isTesting = intent?.getBooleanExtra("IS_TESTING", false) ?: false
        FirebaseApp.initializeApp(this)
        if (!isTesting){
            auth = FirebaseAuth.getInstance()
            firestore = Firebase.firestore
        }
        requestNotificationPermission()
        functions = FirebaseFunctions.getInstance()
        Log.d("COmposeHierarchy", "auth uid : ${auth.currentUser?.uid}")

        setContent {
            // Provide the `isTesting` flag to the entire composable tree
            CompositionLocalProvider(LocalIsTesting provides isTesting) {
                SampleAppTheme {
                    Surface(modifier = Modifier.fillMaxSize()) { IcebreakrrApp(auth, functions, isTesting, firestore) }
                }
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
fun IcebreakrrApp(auth: FirebaseAuth, functions: FirebaseFunctions, isTesting: Boolean, firestore: FirebaseFirestore) {
    Log.d("COmposeHierarchy", "auth uid : ${auth.currentUser?.uid}")
    val profileViewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Companion.Factory(auth, firestore))
    val tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Companion.Factory(auth, firestore))
    val filterViewModel: FilterViewModel = viewModel(factory = FilterViewModel.Factory)
    val ourUserUid = auth.currentUser?.uid ?: "null"
    val ourName = auth.currentUser?.displayName ?: "null"

    MeetingRequestManager.meetingRequestViewModel =
        viewModel(
            factory =
            MeetingRequestViewModel.Companion.Factory(
                profileViewModel, functions, ourUserUid, ourName))
    val meetingRequestViewModel = MeetingRequestManager.meetingRequestViewModel
    val startDestination = if (isTesting) Route.AROUND_YOU else Route.AUTH

    IcebreakrrNavHost(
        profileViewModel, tagsViewModel, filterViewModel, meetingRequestViewModel, startDestination, auth)
}

@Composable
fun IcebreakrrNavHost(
    profileViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel,
    filterViewModel: FilterViewModel,
    meetingRequestViewModel: MeetingRequestViewModel?,
    startDestination: String,
    auth: FirebaseAuth
) {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    Log.d("COmposeHierarchy", "auth uid : ${auth.currentUser?.uid}")
    NavHost(navController = navController, startDestination = startDestination) {
        navigation(
            startDestination = Screen.AUTH,
            route = Route.AUTH,
        ) {
            composable(Screen.AUTH) {
                if (meetingRequestViewModel != null) {
                    SignInScreen(
                        profileViewModel,
                        meetingRequestViewModel,
                        navigationActions,
                        filterViewModel = filterViewModel,
                        tagsViewModel = tagsViewModel)
                } else {
                    throw IllegalStateException(
                        "The Meeting Request View Model shouldn't be null : Bad initialization")
                }
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
                if (meetingRequestViewModel != null) {
                    OtherProfileView(
                        profileViewModel,
                        tagsViewModel,
                        meetingRequestViewModel,
                        navigationActions,
                        navBackStackEntry)
                } else {
                    throw IllegalStateException(
                        "The Meeting Request View Model shouldn't be null : Bad initialization")
                }
            }
        }

        navigation(
            startDestination = Screen.SETTINGS,
            route = Route.SETTINGS,
        ) {
            composable(Screen.SETTINGS) { SettingsScreen(profileViewModel, navigationActions, auth) }
            composable(Screen.PROFILE) { ProfileView(profileViewModel, tagsViewModel, navigationActions, auth) }
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
                ProfileEditingScreen(navigationActions, tagsViewModel, profileViewModel, auth)
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