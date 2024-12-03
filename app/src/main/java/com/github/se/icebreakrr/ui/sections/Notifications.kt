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
private val TEXT_WEIGHT = 1f
private val DROPDOWN_HORIZONTAL_PADDING = 16.dp
private val DROPDOWN_VERTICAL_PADDING = 8.dp
private const val UNDERSCORE = "_"
private const val SPACE = " "

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
  meetingRequestViewModel.updateInboxOfMessagesAndThen() {}
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
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(
                          horizontal = DROPDOWN_HORIZONTAL_PADDING,
                          vertical = DROPDOWN_VERTICAL_PADDING))
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

/**
 * A composable dropdown menu that allows the user to select the display about the meeting requests
 * (inspired by the SortOptionDropdown).
 *
 * This dropdown displays the currently selected meeting request display option and provides a list
 * of other available options when expanded. The user can select a new meeting request display
 * option from the list, which triggers the provided callback to handle the selection.
 *
 * @param selectedOption The currently selected meeting request display option, displayed at the top
 *   of the dropdown.
 * @param onOptionSelected A callback function that is triggered when the user selects a new meeting
 *   request display option.
 * @param modifier A [Modifier] applied to the container of the dropdown for customization.
 */
@Composable
fun MeetingRequestOptionDropdown(
    selectedOption: MeetingRequestOption,
    onOptionSelected: (MeetingRequestOption) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  val otherOptions = MeetingRequestOption.values().filter { it != selectedOption }

  Column(modifier = modifier) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(COLUMN_HORIZONTAL_PADDING)
                .testTag("MeetingRequestOptionsDropdown_Selected"),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text =
                  "Meeting Request: ${
                    selectedOption.name.replace(UNDERSCORE, SPACE).lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }",
              fontSize = TEXT_SMALL_SIZE,
              modifier = Modifier.weight(TEXT_WEIGHT).testTag("MeetingRequestOptionsDropdown_Text"))
          Icon(
              imageVector =
                  if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
              contentDescription = "Icon of the MeetingRequest option dropdown menu",
              modifier = Modifier.testTag("MeetingRequestOptionsDropdown_Arrow"))
        }

    if (expanded) {
      otherOptions.forEach { meetingRequestOption ->
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable {
                      expanded = false
                      onOptionSelected(meetingRequestOption)
                    }
                    .padding(
                        start = COLUMN_VERTICAL_PADDING,
                        top = SORT_TOP_PADDING,
                        bottom = COLUMN_HORIZONTAL_PADDING)
                    .testTag("MeetingRequestOptionsDropdown_Option_${meetingRequestOption.name}"),
        ) {
          Text(
              text =
                  meetingRequestOption.name
                      .replace(UNDERSCORE, SPACE)
                      .lowercase()
                      .replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                      },
              fontSize = TEXT_SMALL_SIZE,
              color = Color.Gray)
        }
      }
    }
  }
}

/** The enumeration of all the options available for meeting request displays */
enum class MeetingRequestOption {
  INBOX,
  SENT,
  CHOOSE_LOCATION
}
