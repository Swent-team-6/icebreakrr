package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.model.profile.MockProfileViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.FilterFloatingActionButton
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar

/**
 * Composable function for displaying the notification screen.
 *
 * It includes a bottom navigation bar and displays the main content of the notification screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(
    navigationActions: NavigationActions,
    profileViewModel: MockProfileViewModel
) {
  val cardList = profileViewModel.profiles.collectAsState()
  val navFunction = {
    navigationActions.navigateTo(Screen.PROFILE_EDIT)
  } // fixme : put Route.View instead
  Scaffold(
      modifier = Modifier.testTag("notificationScreen"),
      topBar = { TopBar("Inbox") },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.NOTIFICATIONS)
      },
      content = { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().testTag("notificationScroll"),
        ) {
          item {
            Text(
                text = "Pending meeting requests",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp).testTag("notificationFirstText"))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
              cardList.value.take(4).forEach { p -> ProfileCard(p, onclick = navFunction) }
            }
            Text(
                text = "Passed",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp).testTag("notificationSecondText"))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
              cardList.value.drop(4).forEach { p -> ProfileCard(p, onclick = navFunction) }
            }
          }
        }
      },
      floatingActionButton = { FilterFloatingActionButton(navigationActions) },
  )
}
