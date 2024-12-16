package com.github.se.icebreakrr.ui.sections

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.authentication.logout
import com.github.se.icebreakrr.config.LocalIsTesting
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.notification.EngagementNotificationManager
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Constants for dimensions and other settings
private val SCREEN_PADDING = 16.dp
private val SPACER_HEIGHT_SMALL = 8.dp
private val SPACER_HEIGHT_LARGE = 16.dp
private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_HEIGHT = 55.dp
private val CARD_ELEVATION = 4.dp
private val BUTTON_COLOR = Color.Red
private val BUTTON_TEXT_COLOR = Color.White
private const val LOGOUT_BUTTON_TAG = "logOutButton"
private val TOGGLE_BOX_HEIGHT = 55.dp
private val BUTTON_PADDING = 8.dp
private val CARD_PADDING = 16.dp

/**
 * Composable function for displaying the Setting screen.
 *
 * It includes a bottom navigation bar and displays the main content of the setting screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 * @param profilesViewModel the View model for the profiles
 * @param appDataStore sets the data or the app
 */
@Composable
fun SettingsScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    appDataStore: AppDataStore,
    locationViewModel: LocationViewModel,
    engagementNotificationManager: EngagementNotificationManager,
    auth: FirebaseAuth
) {

  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()

  // Collect the discoverability state from DataStore
  val isDiscoverable by appDataStore.isDiscoverable.collectAsState(initial = true)

  LaunchedEffect(Unit) { auth.currentUser?.let { profilesViewModel.getProfileByUid(it.uid) } }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val profile = profilesViewModel.selectedProfile.collectAsState().value
  val isTesting = LocalIsTesting.current
  val myProfile = profilesViewModel.selfProfile.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("settingsScreen").fillMaxSize(),
      topBar = { TopBar("Settings") },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              if (route.route != Route.SETTINGS) {
                navigationActions.navigateTo(route)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.SETTINGS,
            notificationCount = (myProfile.value?.meetingRequestInbox?.size ?: 0),
            heatMapCount = myProfile.value?.meetingRequestChosenLocalisation?.size ?: 0)
      },
  ) { innerPadding ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(SCREEN_PADDING)
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start) {
          // Display ProfileCard
          val displayProfile = profile ?: Profile.getMockedProfiles().first()
          ProfileCard(profile = displayProfile, isSettings = true) {
            navigationActions.navigateTo(Screen.PROFILE)
          }

          Spacer(modifier = Modifier.height(SPACER_HEIGHT_SMALL))

          // Display Toggle Options
          ToggleOptionBox(
              label = "Toggle Discoverability",
              isChecked = isDiscoverable,
              onCheckedChange = { discoverable ->
                when (discoverable) {
                  true -> locationViewModel.tryToStartLocationUpdates()
                  false -> locationViewModel.stopLocationUpdates()
                }
                coroutineScope.launch { appDataStore.saveDiscoverableStatus(discoverable) }
              })
          Spacer(modifier = Modifier.height(SPACER_HEIGHT_LARGE))

          Button(
              onClick = {
                if (isNetworkAvailableWithContext(context) || isTesting) {
                  navigationActions.navigateTo(Screen.UNBLOCK_PROFILE)
                } else {
                  Toast.makeText(context, R.string.No_Internet_Toast, Toast.LENGTH_SHORT).show()
                }
              },
              colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              modifier = Modifier.fillMaxWidth().testTag("blockedUsersButton")) {
                Text(stringResource(R.string.unblock_button), color = Color.White)
              }

          Spacer(modifier = Modifier.padding(vertical = BUTTON_PADDING))

          Button(
              onClick = {
                if (isNetworkAvailableWithContext(context) || isTesting) {
                  navigationActions.navigateTo(Screen.ALREADY_MET)
                } else {
                  Toast.makeText(context, R.string.No_Internet_Toast, Toast.LENGTH_SHORT).show()
                }
              },
              colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              modifier = Modifier.fillMaxWidth().testTag("alreadyMetButton")) {
                Text(stringResource(R.string.Already_Met_Settings_Button), color = Color.White)
              }

          Spacer(modifier = Modifier.padding(vertical = BUTTON_PADDING))

          Button(
              onClick = {
                if (isTesting) {
                  navigationActions.navigateTo(Screen.AUTH)
                } else {
                  auth.currentUser?.let {
                    logout(
                        context,
                        navigationActions,
                        appDataStore = appDataStore,
                        engagementManager = engagementNotificationManager)
                  }
                  logout(
                      context,
                      navigationActions,
                      appDataStore,
                      engagementManager = engagementNotificationManager)
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = BUTTON_COLOR),
              modifier = Modifier.fillMaxWidth().testTag(LOGOUT_BUTTON_TAG)) {
                Text("Log Out", color = BUTTON_TEXT_COLOR)
              }
        }
  }
}

@Composable
fun ToggleOptionBox(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

  Card(
      shape = CARD_SHAPE,
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = BUTTON_PADDING)
              .height(TOGGLE_BOX_HEIGHT)
              .testTag(label),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.background,
              contentColor = MaterialTheme.colorScheme.onSecondary)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "",
                modifier = Modifier.padding(end = 8.dp) // Add padding to the right of the icon
            )
              Text(label, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.weight(1f))
              Switch(
                  checked = isChecked,
                  modifier = Modifier.testTag("switch$label"),
                  colors =
                      SwitchDefaults.colors()
                          .copy(
                              checkedThumbColor = MaterialTheme.colorScheme.primary,
                              checkedTrackColor = MaterialTheme.colorScheme.onTertiary),
                  onCheckedChange = onCheckedChange)
            }
      }
}
