package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast
import java.util.Locale

// Constants for padding and list management
private val HORIZONTAL_PADDING = 7.dp
private val TEXT_VERTICAL_PADDING = 16.dp
private val CARD_SPACING = 16.dp
private const val MEETING_REQUEST_MSG = "Pending meeting requests"
private const val MEETING_REQUEST_SENT = "Meeting request sent"
private val COLUMN_VERTICAL_PADDING = 16.dp
private val COLUMN_HORIZONTAL_PADDING = 8.dp
private val SORT_TOP_PADDING = 4.dp
private val TEXT_SMALL_SIZE = 16.sp

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
  val inboxCardList = profileViewModel.inboxItems.collectAsState()
  val sentCardList = profileViewModel.sentItems.collectAsState()
  val context = LocalContext.current
  var meetingRequestOption by remember { mutableStateOf(MeetingRequestOption.INBOX) }
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
            selectedItem = Route.NOTIFICATIONS,
            notificationCount = inboxCardList.value.size)
      },
      content = { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = HORIZONTAL_PADDING)) {
          MeetingRequestOptionDropdown(
              selectedOption = meetingRequestOption,
              onOptionSelected = { meetingRequestOption = it },
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
          if (meetingRequestOption == MeetingRequestOption.INBOX) {
            LazyColumn(
                modifier =
                    Modifier.padding()
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
                      inboxCardList.value.forEach { p ->
                        ProfileCard(
                            p.key,
                            onclick = {
                              if (isNetworkAvailableWithContext(context)) {
                                navigationActions.navigateTo(
                                    Screen.INBOX_PROFILE_VIEW + "?userId=${p.key.uid}")
                              } else {
                                showNoInternetToast(context)
                              }
                            })
                      }
                    }
                  }
                }
          }
          if (meetingRequestOption == MeetingRequestOption.SENT) {
            LazyColumn(
                modifier =
                    Modifier.padding()
                        .padding(horizontal = HORIZONTAL_PADDING)
                        .testTag("notificationScroll")) {
                  item {
                    Text(
                        text = MEETING_REQUEST_SENT,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier.padding(vertical = TEXT_VERTICAL_PADDING)
                                .testTag("notificationFirstText"))
                    Column(verticalArrangement = Arrangement.spacedBy(CARD_SPACING)) {
                      sentCardList.value.forEach { p ->
                        ProfileCard(profile = p, onclick = {}, greyedOut = true)
                      }
                    }
                  }
                }
          }
        }
      })
}

@Composable
fun MeetingRequestOptionDropdown(
    selectedOption: MeetingRequestOption,
    onOptionSelected: (MeetingRequestOption) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  // List of all options excluding the selected one
  val otherOptions = MeetingRequestOption.values().filter { it != selectedOption }

  Column(modifier = modifier) {
    // Selected option with a dropdown indicator
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(COLUMN_HORIZONTAL_PADDING)
                .testTag("SortOptionsDropdown_Selected"),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text =
                  "Meeting Request: ${
                    selectedOption.name.replace("_", " ").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }",
              fontSize = TEXT_SMALL_SIZE,
              modifier =
                  Modifier.weight(1f) // Pushes the arrow to the end
                      .testTag("MeetingRequestOptionsDropdown_Text"))
          Icon(
              imageVector =
                  if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
              contentDescription = if (expanded) "Collapse" else "Expand",
              modifier = Modifier.testTag("MeetingRequestOptionsDropdown_Arrow"))
        }

    if (expanded) {
      otherOptions.forEach { option ->
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable {
                      expanded = false
                      onOptionSelected(option)
                    }
                    .padding(
                        start = COLUMN_VERTICAL_PADDING,
                        top = SORT_TOP_PADDING,
                        bottom = COLUMN_HORIZONTAL_PADDING)
                    .testTag("MeetingRequestOptionsDropdown_Option_${option.name}"),
        ) {
          Text(
              text =
                  option.name.replace("_", " ").lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                  },
              fontSize = TEXT_SMALL_SIZE,
              color = Color.Gray)
        }
      }
    }
  }
}

enum class MeetingRequestOption {
  INBOX,
  SENT,
  CHOOSE_LOCATION
}
