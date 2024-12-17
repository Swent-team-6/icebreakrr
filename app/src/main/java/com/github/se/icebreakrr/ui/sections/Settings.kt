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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.authentication.logout
import com.github.se.icebreakrr.config.LocalIsTesting
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.mock.emptyProfile
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
private val SPACER_HEIGHT_LARGE = 8.dp
private val CARD_SHAPE = RoundedCornerShape(16.dp)
private val CARD_ELEVATION = 4.dp
private val TOGGLE_BOX_HEIGHT = 55.dp
private val BUTTON_PADDING = 0.08f.dp
private val CARD_PADDING = 16.dp
private val ICON_PADDING = 8.dp
private val RELATIVE_SPACING = 0.02f

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
  val coroutineScope = rememberCoroutineScope()

  var deleteAccountDialog by remember { mutableStateOf(false) }

  // Collect the discoverability state from DataStore
  val isDiscoverable by appDataStore.isDiscoverable.collectAsState(initial = true)

  LaunchedEffect(Unit) { profilesViewModel.getSelfProfile {} }

  val isTesting = LocalIsTesting.current
  val myProfile = profilesViewModel.selfProfile.collectAsState()

  val screenHeight = LocalConfiguration.current.screenHeightDp.dp

  Scaffold(
      modifier = Modifier
          .testTag("settingsScreen")
          .fillMaxSize(),
      topBar = { TopBar(stringResource(R.string.settings_screen_title)) },
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
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(SCREEN_PADDING)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start) {
          // Profile Card
          val displayProfile = myProfile.value ?: Profile.emptyProfile()
          ProfileCard(profile = displayProfile, isSettings = true) {
            navigationActions.navigateTo(Screen.PROFILE)
          }

          Spacer(modifier = Modifier.height(screenHeight * RELATIVE_SPACING))

          // Toggle Options
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

          Spacer(modifier = Modifier.height(screenHeight * RELATIVE_SPACING))

          Button(
              onClick = {
                if (isNetworkAvailableWithContext(context) || isTesting) {
                  navigationActions.navigateTo(Screen.UNBLOCK_PROFILE)
                } else {
                  Toast.makeText(context, R.string.No_Internet_Toast, Toast.LENGTH_SHORT).show()
                }
              },
              elevation =  ButtonDefaults.buttonElevation(CARD_ELEVATION),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("blockedUsersButton")) {
                Text(
                    stringResource(R.string.unblock_button),
                    color = MaterialTheme.colorScheme.primaryContainer)
              }

          Spacer(modifier = Modifier.height(screenHeight * RELATIVE_SPACING))

          Button(
              onClick = {
                if (isNetworkAvailableWithContext(context) || isTesting) {
                  navigationActions.navigateTo(Screen.ALREADY_MET)
                } else {
                  Toast.makeText(context, R.string.No_Internet_Toast, Toast.LENGTH_SHORT).show()
                }
              },
              elevation =  ButtonDefaults.buttonElevation(CARD_ELEVATION),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("alreadyMetButton")) {
                Text(
                    stringResource(R.string.Already_Met_Settings_Button),
                    color = MaterialTheme.colorScheme.primaryContainer)
              }

          Spacer(modifier = Modifier.height(screenHeight * RELATIVE_SPACING))

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
              elevation =  ButtonDefaults.buttonElevation(CARD_ELEVATION),
              colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("logOutButton")) {
                Text(
                    stringResource(R.string.Already_Met_Logout_Button),
                    color = MaterialTheme.colorScheme.onPrimary)
              }

          Spacer(modifier = Modifier.height(screenHeight * RELATIVE_SPACING))

          Button(
              onClick = { deleteAccountDialog = true },
              elevation =  ButtonDefaults.buttonElevation(CARD_ELEVATION),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.error),
              modifier = Modifier
                  .fillMaxWidth()
                  .testTag("deleteAccountButton")) {
                Text(
                    stringResource(R.string.Already_Met_Delete_Accout_Button),
                    color = MaterialTheme.colorScheme.onError)
              }


        }
    DeleteAccountDialog(
        showDialog = deleteAccountDialog,
        onDismiss = { deleteAccountDialog = false },
        onDelete = {
          val uid = auth.currentUser?.uid ?: ""
          logout(
              context,
              navigationActions,
              appDataStore,
              engagementManager = engagementNotificationManager)
          profilesViewModel.deleteProfileByUid(uid)
        })
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Filled.Place,
                  contentDescription = "",
                  modifier =
                      Modifier.padding(end = ICON_PADDING) // Add padding to the right of the icon
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

@Composable
fun DeleteAccountDialog(showDialog: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit) {
  if (showDialog) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account_dialog_title)) },
        text = { Text(stringResource(R.string.delete_account_dialog_message)) },
        confirmButton = {
          TextButton(
              onClick = onDelete, modifier = Modifier.testTag("deleteAccountConfirmationButton")) {
                Text(
                    stringResource(R.string.delete_account_confirm),
                    color = MaterialTheme.colorScheme.error)
              }
        },
        dismissButton = {
          TextButton(onClick = onDismiss) {
            Text(
                stringResource(R.string.delete_account_cancel),
                color = MaterialTheme.colorScheme.onSecondary)
          }
        },
        modifier = Modifier.testTag("alertDialog"))
  }
}
