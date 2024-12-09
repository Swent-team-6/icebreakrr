package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.Badge
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
  meetingRequestViewModel.updateInboxOfMessages {}
  val inboxCardList = profileViewModel.inboxItems.collectAsState()
  val sentCardList = profileViewModel.sentItems.collectAsState()
  val pendingLocation = profileViewModel.pendingLocalisations.collectAsState()
  val context = LocalContext.current
  var meetingRequestOption by remember { mutableStateOf(MeetingRequestOption.INBOX) }
  val myProfile = profileViewModel.selfProfile.collectAsState()
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
            notificationCount = inboxCardList.value.size + pendingLocation.value.size,
            heatMapCount = myProfile.value?.meetingRequestChosenLocalisation?.size ?: 0)
      },
      content = { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
          MeetingRequestOptionDropdown(
              selectedOption = meetingRequestOption,
              onOptionSelected = { meetingRequestOption = it },
              modifier =
                  Modifier.fillMaxWidth()
                      .background(MaterialTheme.colorScheme.primaryContainer)
                      .padding(
                          horizontal = DROPDOWN_HORIZONTAL_PADDING,
                          vertical = DROPDOWN_VERTICAL_PADDING),
              pendingLocationsSize = pendingLocation.value.size,
              inboxSize = inboxCardList.value.size)
          when (meetingRequestOption) {
            MeetingRequestOption.INBOX -> {
              DisplayTextAndCard(
                  stringResource(R.string.meeting_request_pending),
                  inboxCardList.value.map { it.key },
                  Screen.INBOX_PROFILE_VIEW,
                  context,
                  navigationActions)
            }
            MeetingRequestOption.SENT -> {
              DisplayTextAndCard(
                  stringResource(R.string.meeting_request_sent),
                  sentCardList.value,
                  "",
                  context,
                  navigationActions)
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
    modifier: Modifier = Modifier,
    pendingLocationsSize: Int,
    inboxSize: Int
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
          if (!expanded) {
            when (selectedOption) {
              MeetingRequestOption.SENT -> {
                if (pendingLocationsSize + inboxSize > 0) {
                  Badge(pendingLocationsSize + inboxSize, "badgeSent")
                }
              }
              MeetingRequestOption.INBOX -> {
                if (pendingLocationsSize > 0) {
                  Badge(pendingLocationsSize, "badgeInbox")
                }
              }
            }
          }
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
              color = MaterialTheme.colorScheme.secondaryContainer)
          when (meetingRequestOption) {
            MeetingRequestOption.INBOX -> {
              if (inboxSize > 0) {
                Badge(inboxSize, "badgeInbox")
              }
            }
            MeetingRequestOption.SENT -> {}
          }
        }
      }
    }
  }
}

/** The enumeration of all the options available for meeting request displays */
enum class MeetingRequestOption {
  INBOX,
  SENT,
}

/**
 * Function that shows the next saying on which inbox we are and the profile cards associate with it
 *
 * @param text: text to write over the cards
 * @param profiles: profiles to show in cards
 * @param onClick : function to call when we click on a card
 */
@Composable
private fun DisplayTextAndCard(
    text: String,
    profiles: List<Profile>,
    screenToNavigate: String,
    context: Context,
    navigationActions: NavigationActions
) {
  LazyColumn(
      modifier =
          Modifier.padding()
              .padding(horizontal = HORIZONTAL_PADDING)
              .testTag("notificationScroll")) {
        item {
          Text(
              text = text,
              fontWeight = FontWeight.Bold,
              modifier =
                  Modifier.padding(vertical = TEXT_VERTICAL_PADDING)
                      .testTag("notificationFirstText"))
          Column(verticalArrangement = Arrangement.spacedBy(CARD_SPACING)) {
            profiles.forEach { p ->
              ProfileCard(
                  profile = p,
                  onclick = {
                    if (screenToNavigate != "") {
                      if (isNetworkAvailableWithContext(context)) {
                        navigationActions.navigateTo(screenToNavigate + "?userId=${p.uid}")
                      } else {
                        showNoInternetToast(context)
                      }
                    }
                  },
                  greyedOut = false)
            }
          }
        }
      }
}
