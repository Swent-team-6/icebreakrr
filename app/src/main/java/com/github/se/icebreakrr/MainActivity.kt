package com.github.se.icebreakrr

import ImageCropperScreen
import ProfileCreationScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.se.icebreakrr.config.LocalIsTesting
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.ai.AiViewModel
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.location.LocationRepositoryFirestore
import com.github.se.icebreakrr.model.location.LocationService
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestManager
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.notification.EngagementNotificationManager
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepositoryFirestore
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.sort.SortViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.authentication.SignInScreen
import com.github.se.icebreakrr.ui.map.LocationSelectorMapScreen
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.profile.InboxProfileViewScreen
import com.github.se.icebreakrr.ui.profile.MapScreen
import com.github.se.icebreakrr.ui.profile.OtherProfileView
import com.github.se.icebreakrr.ui.profile.ProfileEditingScreen
import com.github.se.icebreakrr.ui.profile.ProfileView
import com.github.se.icebreakrr.ui.sections.AlreadyMetScreen
import com.github.se.icebreakrr.ui.sections.AroundYouScreen
import com.github.se.icebreakrr.ui.sections.FilterScreen
import com.github.se.icebreakrr.ui.sections.NotificationScreen
import com.github.se.icebreakrr.ui.sections.SettingsScreen
import com.github.se.icebreakrr.ui.sections.UnblockProfileScreen
import com.github.se.icebreakrr.ui.theme.IceBreakrrTheme
import com.github.se.icebreakrr.utils.IPermissionManager
import com.github.se.icebreakrr.utils.NetworkUtils
import com.github.se.icebreakrr.utils.PermissionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.storage

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  private lateinit var firestore: FirebaseFirestore
  private lateinit var authStateListener: FirebaseAuth.AuthStateListener
  private lateinit var engagementNotificationManager: EngagementNotificationManager
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var locationService: LocationService
  private lateinit var locationRepositoryFirestore: LocationRepositoryFirestore
  private lateinit var permissionManager: PermissionManager
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var appDataStore: AppDataStore
  private lateinit var functions: FirebaseFunctions

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    // Retrieve the testing flag from the Intent
    val isTesting = intent?.getBooleanExtra("IS_TESTING", false) ?: false

    // Initialize Firebase
    FirebaseApp.initializeApp(this)
    auth = FirebaseAuth.getInstance()
    firestore = FirebaseFirestore.getInstance()
    functions = FirebaseFunctions.getInstance()

    // Initialize Utils
    NetworkUtils.init(this)

    // Initialize DataStore
    appDataStore = AppDataStore(context = this)

    // Create and initialize the PermissionManager
    requestNotificationPermission()
    permissionManager = PermissionManager(this)
    permissionManager.initializeLauncher(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

    // Initialize location services
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    locationService = LocationService(fusedLocationClient)
    locationRepositoryFirestore = LocationRepositoryFirestore(firestore, auth)

    // Initialize ViewModels
    locationViewModel =
        ViewModelProvider(
            this,
            LocationViewModel.provideFactory(
                locationService, locationRepositoryFirestore, permissionManager))[
            LocationViewModel::class.java]

    val profilesViewModel =
        ProfilesViewModel(
            repository = ProfilesRepositoryFirestore(firestore, auth),
            ppRepository = ProfilePicRepositoryStorage(Firebase.storage),
            auth = auth)

    val filterViewModel = FilterViewModel()

    val meetingRequestViewModel =
        MeetingRequestViewModel(profilesViewModel = profilesViewModel, functions = functions)

    val tagsViewModel = TagsViewModel(TagsRepository(firestore, auth))

    engagementNotificationManager =
        EngagementNotificationManager(
            profilesViewModel,
            meetingRequestViewModel,
            appDataStore,
            filterViewModel,
            tagsViewModel)

    // Monitor login/logout events
    authStateListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->
          if (auth.currentUser != null) {
            locationViewModel.tryToStartLocationUpdates()
          } else {
            locationViewModel.stopLocationUpdates()
          }
        }

    val chatGptApiKey = getChatGptApiKey()

    setContent {
      CompositionLocalProvider(LocalIsTesting provides isTesting) {
        IceBreakrrTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            IcebreakrrApp(
                auth = auth,
                functions = functions,
                appDataStore = appDataStore,
                locationViewModel = locationViewModel,
                firestore = firestore,
                chatGptApiKey = chatGptApiKey,
                isTesting = isTesting,
                permissionManager = permissionManager,
            )
          }
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    auth.addAuthStateListener(authStateListener)
  }

  override fun onResume() {
    super.onResume()
    permissionManager.updateAllPermissions()
  }

  override fun onStop() {
    super.onStop()
    auth.removeAuthStateListener(authStateListener)
    // Stop monitoring when app goes to background
    engagementNotificationManager.stopMonitoring()
  }

  private fun getChatGptApiKey(): String {
    return try {
      val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
      appInfo.metaData?.getString("com.openai.chatgpt.API_KEY")
          ?: throw Exception("API Key not found in AndroidManifest.xml")
    } catch (e: Exception) {
      Log.e("MainActivity", "Error retrieving API Key: ${e.message}")
      ""
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
fun IcebreakrrApp(
    auth: FirebaseAuth,
    functions: FirebaseFunctions,
    appDataStore: AppDataStore,
    locationViewModel: LocationViewModel,
    firestore: FirebaseFirestore,
    chatGptApiKey: String,
    isTesting: Boolean,
    permissionManager: IPermissionManager,
) {
  val profileViewModel: ProfilesViewModel =
      viewModel(factory = ProfilesViewModel.Companion.Factory(auth, firestore))
  val tagsViewModel: TagsViewModel =
      viewModel(factory = TagsViewModel.Companion.Factory(auth, firestore))
  val filterViewModel: FilterViewModel = viewModel(factory = FilterViewModel.Factory)
  MeetingRequestManager.meetingRequestViewModel =
      viewModel(factory = MeetingRequestViewModel.Companion.Factory(profileViewModel, functions))
  val sortViewModel: SortViewModel =
      viewModel(factory = SortViewModel.createFactory(profileViewModel))
  val aiViewModel: AiViewModel =
      viewModel(factory = AiViewModel.provideFactory(chatGptApiKey, profileViewModel))
  val meetingRequestViewModel = MeetingRequestManager.meetingRequestViewModel
  // Initialize EngagementManager
  val engagementManager =
      meetingRequestViewModel?.let {
        EngagementNotificationManager(
            profilesViewModel = profileViewModel,
            meetingRequestViewModel = it,
            appDataStore = appDataStore,
            filterViewModel = filterViewModel,
            tagsViewModel = tagsViewModel)
      }

  val startDestination = if (isTesting) Route.AROUND_YOU else Route.AUTH

  IcebreakrrNavHost(
      profileViewModel,
      tagsViewModel,
      filterViewModel,
      sortViewModel,
      meetingRequestViewModel,
      appDataStore,
      locationViewModel,
      startDestination,
      auth,
      permissionManager,
      aiViewModel,
      isTesting,
      engagementManager)
}

@Composable
fun IcebreakrrNavHost(
    profileViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel,
    filterViewModel: FilterViewModel,
    sortViewModel: SortViewModel,
    meetingRequestViewModel: MeetingRequestViewModel?,
    appDataStore: AppDataStore,
    locationViewModel: LocationViewModel,
    startDestination: String,
    auth: FirebaseAuth,
    permissionManager: IPermissionManager,
    aiViewModel: AiViewModel,
    isTesting: Boolean,
    engagementNotificationManager: EngagementNotificationManager?
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  NavHost(navController = navController, startDestination = startDestination) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) {
        if (meetingRequestViewModel != null) {
          if (engagementNotificationManager != null) {
            SignInScreen(
                profileViewModel,
                meetingRequestViewModel,
                engagementNotificationManager = engagementNotificationManager,
                navigationActions,
                filterViewModel = filterViewModel,
                tagsViewModel = tagsViewModel,
                appDataStore = appDataStore,
                locationViewModel = locationViewModel)
          }
        } else {
          throw IllegalStateException(
              "The Meeting Request View Model shouldn't be null : Bad initialization")
        }
      }
      composable(Screen.PROFILE_CREATION) {
        ProfileCreationScreen(tagsViewModel, profileViewModel, navigationActions)
      }
    }

    navigation(
        startDestination = Screen.AROUND_YOU,
        route = Route.AROUND_YOU,
    ) {
      composable(Screen.AROUND_YOU) {
        AroundYouScreen(
            navigationActions,
            profileViewModel,
            tagsViewModel,
            filterViewModel,
            locationViewModel,
            sortViewModel,
            permissionManager,
            appDataStore,
            isTesting)
      }
      composable(Screen.OTHER_PROFILE_VIEW + "?userId={userId}") { navBackStackEntry ->
        if (meetingRequestViewModel != null) {
          OtherProfileView(
              profileViewModel,
              tagsViewModel,
              aiViewModel,
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
      composable(Screen.SETTINGS) {
        if (engagementNotificationManager != null) {
          SettingsScreen(
              profilesViewModel = profileViewModel,
              navigationActions = navigationActions,
              appDataStore = appDataStore,
              locationViewModel = locationViewModel,
              engagementNotificationManager = engagementNotificationManager,
              auth = auth)
        }
      }
      composable(Screen.PROFILE) {
        ProfileView(profileViewModel, tagsViewModel, navigationActions, auth)
      }
      composable(Screen.ALREADY_MET) { AlreadyMetScreen(navigationActions, profileViewModel) }
    }

    navigation(
        startDestination = Screen.NOTIFICATIONS,
        route = Route.NOTIFICATIONS,
    ) {
      composable(Screen.NOTIFICATIONS) {
        if (meetingRequestViewModel != null) {
          NotificationScreen(navigationActions, profileViewModel, meetingRequestViewModel)
        } else {
          throw IllegalStateException(
              "The Meeting Request View Model shouldn't be null : Bad initialization")
        }
      }
      composable(Screen.INBOX_PROFILE_VIEW + "?userId={userId}") { navBackStackEntry ->
        if (meetingRequestViewModel != null) {
          InboxProfileViewScreen(
              profileViewModel,
              navBackStackEntry,
              navigationActions,
              tagsViewModel,
              meetingRequestViewModel,
              isTesting)
        } else {
          throw IllegalStateException(
              "The Meeting Request View Model shouldn't be null : Bad initialization")
        }
      }
      composable(Screen.MAP_MEETING_LOCATION_SCREEN + "?userId={userId}") { navBackStackEntry ->
        if (meetingRequestViewModel != null) {
          LocationSelectorMapScreen(
              profileViewModel,
              navigationActions,
              meetingRequestViewModel,
              navBackStackEntry,
              locationViewModel,
              isTesting)
        } else {
          throw IllegalStateException(
              "The Meeting Request View Model shouldn't be null : Bad initialization")
        }
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

      navigation(
          startDestination = Screen.CROP,
          route = Route.CROP,
      ) {
        composable(Screen.CROP) { ImageCropperScreen(profileViewModel, navigationActions) }
      }

      navigation(
          startDestination = Screen.UNBLOCK_PROFILE,
          route = Route.UNBLOCK_PROFILE,
      ) {
        composable(Screen.UNBLOCK_PROFILE) {
          UnblockProfileScreen(navigationActions, profileViewModel)
        }
      }

      navigation(
          startDestination = Screen.MAP,
          route = Route.MAP,
      ) {
        composable(Screen.MAP) { MapScreen(navigationActions, profileViewModel, locationViewModel) }
      }
    }
  }
}
