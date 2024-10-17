package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.icebreakrr.model.profile.MockProfileViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.FilterFloatingActionButton
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar

/**
 * Composable function for displaying the "Around You" screen.
 *
 * It includes a bottom navigation bar and displays the main content of the screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AroundYouScreen(
    navigationActions: NavigationActions,
    mockProfileViewModel: MockProfileViewModel = viewModel(factory = MockProfileViewModel.Factory)
) {

  val profiles = mockProfileViewModel.profiles.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("aroundYouScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = navigationActions.currentRoute())
      },
      topBar = { TopBar("Around You") },
      content = { innerPadding ->
        val context = LocalContext.current
        if (profiles.value.isNotEmpty()) {
          LazyColumn(
              contentPadding = PaddingValues(vertical = 16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(innerPadding)) {
                items(profiles.value.size) { index ->
                  ProfileCard(
                      profile = profiles.value[index],
                      onclick = { navigationActions.navigateTo(Screen.PROFILE) })
                }
              }
        } else {
          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Text(
                    modifier = Modifier.padding(innerPadding).testTag("emptyProfilePrompt"),
                    text = "There is no one around. Try moving!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF575757))
              }
        }
      },
      floatingActionButton = { FilterFloatingActionButton(navigationActions) })
}
