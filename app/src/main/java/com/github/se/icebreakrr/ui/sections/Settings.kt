package com.github.se.icebreakrr.ui.sections

import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
import com.google.firebase.auth.auth

// This File was written with the help of Cursor
@Composable
fun SettingsScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
  Log.d("ComposeHierarchy", "[SettingsScreen] beginning")
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  LaunchedEffect(Unit) {
    auth.currentUser?.let {
      Log.d(
          "ComposeHierarchy",
          "[SettingsScreen] launch of settings get profile with uid : ${it.uid}")
      profilesViewModel.getProfileByUid(it.uid)
      Log.d("ComposeHierarchy", "[SettingsScreen] finish get profile")
    }
  }

  val isLoading = profilesViewModel.loading.collectAsState()
  val profile = profilesViewModel.selectedProfile.collectAsState()
  val error = profilesViewModel.error.collectAsState()

  Log.d(
      "ComposeHierarchy",
      "[SettingsScreen] selectedProfile of ProfilesViewModel : ${profile.value?.uid}")
  Log.d("ComposeHierarchy", "[SettingsScreen] error in ProfilesViewModel : $error")
  Log.d(
      "ComposeHierarchy",
      "[SettingsScreen] Has backend of ProfilesViewModel finish loading : $isLoading")

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
            Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start) {
          if (isLoading.value) {
            CircularProgressIndicator()
          } else {
            if (profile.value != null) {
              ProfileCard(profile = profile.value!!, isSettings = true) {
                navigationActions.navigateTo(Screen.PROFILE)
              }
            } else {
              ProfileCard(profile = Profile.getMockedProfiles()[0], isSettings = true) {
                navigationActions.navigateTo(Screen.PROFILE)
              }
            }
          }

          Spacer(modifier = Modifier.padding(vertical = 8.dp))

          ToggleOptionBox(label = "Toggle Location")
          ToggleOptionBox(label = "Option 1")

          Spacer(modifier = Modifier.padding(vertical = 16.dp))
          Button(
              onClick = {
                Toast.makeText(context, "Log Out (Not yet implemented)", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE0E00)),
              modifier = Modifier.fillMaxWidth().testTag("logOutButton")) {
                Text("Log Out", color = Color.White)
              }
        }
  }
  Log.d("ComposeHierarchy", "[SettingsScreen] end")
}

@Composable
fun ToggleOptionBox(label: String) {
  val toggleState = remember { mutableStateOf(false) }

  Card(
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(55.dp).testTag(label),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Text(label)
              Spacer(modifier = Modifier.weight(1f))
              Switch(
                  checked = toggleState.value,
                  modifier = Modifier.testTag("switch$label"),
                  onCheckedChange = { toggleState.value = it })
            }
      }
}
