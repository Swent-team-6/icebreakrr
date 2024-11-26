package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
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
private const val MEETING_REQUEST_MSG = "Pending meeting requests"
private const val MEETING_REQUEST_ACCEPTED = "Meeting Request Accepted !"

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
    profileViewModel: ProfilesViewModel,
    meetingRequestViewModel: MeetingRequestViewModel
) {
  meetingRequestViewModel.updateInboxOfMessages()
  val cardList = profileViewModel.inboxItems.collectAsState()
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
                  cardList.value.forEach { p ->
                    ProfileCard(
                        p.key,
                        onclick = {
                          if (p.key.fcmToken != null) {
                            meetingRequestViewModel.setMeetingResponse(
                                p.key.fcmToken!!, MEETING_REQUEST_ACCEPTED, true)
                            meetingRequestViewModel.sendMeetingResponse()
                            meetingRequestViewModel.removeFromMeetingRequestInbox(p.key.uid)
                            meetingRequestViewModel.updateInboxOfMessages()
                          } else {
                            Log.e("NOTIFICATION PAGE ERROR", "Fcm token of profile is null")
                          }
                        })
                  }
                }
              }
            }
      })
}
