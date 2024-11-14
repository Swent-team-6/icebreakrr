package com.github.se.icebreakrr.ui.sections

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.authentication.logout
import com.github.se.icebreakrr.config.LocalIsTesting
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.google.firebase.auth.FirebaseAuth

// Constants for dimensions and other settings
private val SCREEN_PADDING = 16.dp
private val SPACER_HEIGHT_SMALL = 8.dp
private val SPACER_HEIGHT_LARGE = 16.dp
private val CARD_CORNER_RADIUS = 16.dp
private val CARD_HEIGHT = 55.dp
private val CARD_ELEVATION = 4.dp
private val BUTTON_COLOR = Color.Red
private val BUTTON_TEXT_COLOR = Color.White
private const val LOGOUT_BUTTON_TAG = "logOutButton"
private val TOGGLE_BOX_HEIGHT = 55.dp

@Composable
fun SettingsScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    appDataStore: AppDataStore
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()

  // Collect the discoverability state from DataStore
  val isDiscoverable by appDataStore.isDiscoverable.collectAsState(initial = true)

  lateinit var auth: FirebaseAuth

  // Constants for padding and spacing
  val screenPadding = 16.dp
  val verticalSpacing = 8.dp
  val buttonVerticalSpacing = 16.dp

  LaunchedEffect(Unit) {
    FirebaseAuth.getInstance().currentUser?.let { profilesViewModel.getProfileByUid(it.uid) }
  }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val profile = profilesViewModel.selectedProfile.collectAsState().value
  val isTesting = LocalIsTesting.current

  Scaffold(
      topBar = { TopBar("Settings") },
      modifier = Modifier.testTag("settingsScreen").fillMaxSize(),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              if (route.route != Route.SETTINGS) {
                navigationActions.navigateTo(route)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.SETTINGS)
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
          ToggleOptionBox(label = "Toggle Location")
          ToggleOptionBox(label = "Option 1")

          Spacer(modifier = Modifier.height(SPACER_HEIGHT_LARGE))

          Button(
              onClick = {
                if (isTesting) {
                  navigationActions.navigateTo(Screen.AUTH)
                } else {
                  auth = FirebaseAuth.getInstance()
                  auth.currentUser?.let {
                    logout(context, navigationActions, appDataStore = appDataStore)
                  }
                  logout(context, navigationActions, appDataStore)
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
  // Constants for card styling
  val cardShape = RoundedCornerShape(16.dp)
  val cardElevation = 4.dp
  val cardHeight = 55.dp
  val cardPadding = 16.dp

  Card(
      shape = RoundedCornerShape(CARD_CORNER_RADIUS),
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = SPACER_HEIGHT_SMALL)
              .height(TOGGLE_BOX_HEIGHT)
              .testTag(label),
      elevation = CardDefaults.cardElevation(defaultElevation = CARD_ELEVATION)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(SCREEN_PADDING),
            verticalAlignment = Alignment.CenterVertically) {
              Text(label)
              Spacer(modifier = Modifier.weight(1f))
              Switch(
                  checked = isChecked,
                  modifier = Modifier.testTag("switch$label"),
                  onCheckedChange = onCheckedChange)
            }
      }
}
