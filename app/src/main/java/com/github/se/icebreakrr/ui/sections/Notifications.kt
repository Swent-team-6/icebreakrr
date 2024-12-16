package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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

// Constants for padding and list management
private val HORIZONTAL_PADDING = 7.dp
private val TEXT_VERTICAL_PADDING = 16.dp
private val CARD_SPACING = 16.dp
private val DROPDOWN_VERTICAL_PADDING = 8.dp
private val SENT_SIZE = 0
private val MEETING_REQUEST_BUTTON_SIZE = 1f

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
  val context = LocalContext.current
  var meetingRequestOption by remember { mutableStateOf(MeetingRequestOption.INBOX) }
  var alertDialogSent by remember { mutableStateOf(false) }
  var sentSelectedProfile = remember { mutableStateOf<Profile?>(null) }
  val myProfile = profileViewModel.selfProfile.collectAsState()
  if (alertDialogSent) {
    AlertDialog(
        modifier = Modifier.testTag("confirmDialog"),
        onDismissRequest = { alertDialogSent = false },
        title = { Text("Do you want to cancel this meeting request?") },
        confirmButton = {
          TextButton(
              onClick = {
                meetingRequestViewModel.sendCancellationToBothUsers(
                    sentSelectedProfile.value?.uid ?: "null",
                    sentSelectedProfile.value?.fcmToken ?: "",
                    sentSelectedProfile.value?.name ?: "",
                    MeetingRequestViewModel.CancellationType.CANCELLED)
                alertDialogSent = false
              }) {
                Text("Yes")
              }
        },
        dismissButton = { TextButton(onClick = { alertDialogSent = false }) { Text("No") } })
  }
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
            notificationCount = inboxCardList.value.size + sentCardList.value.size,
            heatMapCount = myProfile.value?.meetingRequestChosenLocalisation?.size ?: 0)
      },
      content = { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
          MeetingRequestOptionRow(
              selectedOption = meetingRequestOption,
              onOptionSelected = { meetingRequestOption = it },
              modifier =
                  Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer),
              sentSize = sentCardList.value.size,
              inboxSize = inboxCardList.value.size)
          when (meetingRequestOption) {
            MeetingRequestOption.INBOX -> {
              DisplayTextAndCard(
                  stringResource(R.string.meeting_request_pending),
                  inboxCardList.value.map { it.key },
                  Screen.INBOX_PROFILE_VIEW,
                  context,
                  navigationActions,
                  {},
                  sentSelectedProfile)
            }
            MeetingRequestOption.SENT -> {
              DisplayTextAndCard(
                  stringResource(R.string.meeting_request_sent),
                  sentCardList.value,
                  "",
                  context,
                  navigationActions,
                  { alertDialogSent = true },
                  sentSelectedProfile)
            }
          }
        }
      })
}

/**
 * A composable dropdown menu that allows the user to select the display about the meeting requests
 *
 * This row displays three options. The user can select a new meeting request display option from
 * the list, which triggers the provided callback to handle the selection.
 *
 * @param selectedOption The currently selected meeting request display option, displayed in a
 *   darker color.
 * @param onOptionSelected A callback function that is triggered when the user selects a new meeting
 *   request display option.
 * @param modifier A [Modifier] applied to the container of the dropdown for customization.
 */
@Composable
fun MeetingRequestOptionRow(
    selectedOption: MeetingRequestOption,
    onOptionSelected: (MeetingRequestOption) -> Unit,
    modifier: Modifier = Modifier,
    sentSize: Int,
    inboxSize: Int
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        MeetingRequestButton(
            option = MeetingRequestOption.INBOX,
            isSelected = selectedOption == MeetingRequestOption.INBOX,
            onClick = { onOptionSelected(MeetingRequestOption.INBOX) },
            badgeCount = inboxSize,
            modifier = Modifier.weight(MEETING_REQUEST_BUTTON_SIZE).testTag("inboxButton"))

        MeetingRequestButton(
            option = MeetingRequestOption.SENT,
            isSelected = selectedOption == MeetingRequestOption.SENT,
            onClick = { onOptionSelected(MeetingRequestOption.SENT) },
            badgeCount = SENT_SIZE,
            modifier = Modifier.weight(MEETING_REQUEST_BUTTON_SIZE).testTag("sentButton"))
      }
}

@Composable
private fun MeetingRequestButton(
    option: MeetingRequestOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier
) {
  Button(
      onClick = onClick,
      modifier = modifier,
      shape = RectangleShape,
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                  else MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondary)) {
        Box(
            modifier = Modifier.padding(vertical = DROPDOWN_VERTICAL_PADDING),
            contentAlignment = Alignment.Center,
        ) {
          Text(text = option.displayName, fontSize = 15.sp, maxLines = 1)
          if (badgeCount > 0) {
            Badge(count = badgeCount, tag = "badge${option.name}")
          }
        }
      }
}

/** The enumeration of all the options available for meeting request displays */
enum class MeetingRequestOption(val displayName: String) {
  INBOX("Inbox"),
  SENT("Sent"),
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
    navigationActions: NavigationActions,
    onClick: (Profile) -> Unit,
    selectedProfile: MutableState<Profile?>
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
                    selectedProfile.value = p
                    onClick(p)
                  },
                  greyedOut = false)
            }
          }
        }
      }
}
