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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

// This File was written with the help of Cursor
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
    Firebase.auth.currentUser?.let { profilesViewModel.getProfileByUid(it.uid) }
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
                .padding(screenPadding)
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start) {
          if (profile != null) {
            ProfileCard(profile = profile, isSettings = true) {
              navigationActions.navigateTo(Screen.PROFILE)
            }
          } else {
            ProfileCard(profile = Profile.getMockedProfiles()[0], isSettings = true) {
              navigationActions.navigateTo(Screen.PROFILE)
            }
          }

          Spacer(modifier = Modifier.padding(vertical = verticalSpacing))

          ToggleOptionBox(
              label = "Toggle Discoverability",
              isChecked = isDiscoverable,
              onCheckedChange = { discoverable ->
                coroutineScope.launch { appDataStore.saveDiscoverableStatus(discoverable) }
              })

          Spacer(modifier = Modifier.padding(vertical = buttonVerticalSpacing))

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
              colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
              modifier = Modifier.fillMaxWidth().testTag("logOutButton")) {
                Text("Log Out", color = Color.White)
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
      shape = cardShape,
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp).height(cardHeight).testTag(label),
      elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(cardPadding),
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
