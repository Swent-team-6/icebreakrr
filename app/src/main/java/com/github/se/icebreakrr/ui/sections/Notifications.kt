package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.model.message.MeetingRequestManager
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar

// Constants for padding and list management
private val HORIZONTAL_PADDING = 7.dp
private val TEXT_VERTICAL_PADDING = 16.dp
private val CARD_SPACING = 16.dp
private const val MAX_PENDING_CARDS = 4
private const val TOAST_MESSAGE = "Requests are not implemented yet :)"
private const val MEETING_REQUEST_MSG = "Pending meeting requests"
private const val PASSED = "Passed"

/**
 * Composable function for displaying the notification screen.
 *
 * It includes a bottom navigation bar and displays the main content of the notification screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(navigationActions: NavigationActions, profileViewModel: ProfilesViewModel) {
  val context = LocalContext.current
  MeetingRequestManager.meetingRequestViewModel?.getInbox()
  val cardList = profileViewModel.inbox.collectAsState()
  Log.d("CARDLIST", cardList.toString())
  val navFunction = { Toast.makeText(context, TOAST_MESSAGE, Toast.LENGTH_SHORT).show() }

  Scaffold(
      modifier = Modifier.testTag("notificationScreen"),
      topBar = { TopBar("Inbox") },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              if (route.route != Route.NOTIFICATIONS) {
                navigationActions.navigateTo(route)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.NOTIFICATIONS)
      },
      content = { innerPadding ->
        LazyColumn(
            modifier =
                Modifier.padding(innerPadding)
                    .padding(horizontal = HORIZONTAL_PADDING)
                    .testTag("notificationScroll")) {
              item {
                Text(
                    text = MEETING_REQUEST_MSG,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(vertical = TEXT_VERTICAL_PADDING)
                            .testTag("notificationFirstText"))
                Column(verticalArrangement = Arrangement.spacedBy(CARD_SPACING)) {
                  cardList.value.take(MAX_PENDING_CARDS).forEach { p ->
                      if (p != null) {
                          ProfileCard(p, onclick = navFunction)
                      }
                  }
                }
                Text(
                    text = PASSED,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(vertical = TEXT_VERTICAL_PADDING)
                            .testTag("notificationSecondText"))
                Column(verticalArrangement = Arrangement.spacedBy(CARD_SPACING)) {
                  cardList.value.drop(MAX_PENDING_CARDS).forEach { p ->
                      if (p != null) {
                          ProfileCard(p, onclick = navFunction, greyedOut = true)
                      }
                  }
                }
              }
            }
      })
}
