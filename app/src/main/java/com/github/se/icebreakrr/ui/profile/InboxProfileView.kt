package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.shared.InfoSection
import com.github.se.icebreakrr.ui.sections.shared.MessageWhenLoadingProfile
import com.github.se.icebreakrr.ui.sections.shared.ProfileHeader

private const val ACCEPT_DECLINE_ELEVATION = 4
private const val ACCEPT_DECLINE_ROUNDER_CORNER_SHAPE = 23
private const val ACCEPT_DECLINE_SHADOW_COLOR = 0x40000000
private const val ACCEPT_DECLINE_BOX_COLOR = 0xFFEAEEFF
private const val ACCEPT_DECLINE_BOX_HEIGHT = 115
private const val ACCEPT_DECLINE_BOX_WIDTH_FACTOR = 0.9
private const val ACCEPT_DECLINE_PADDING_START = 10.5
private const val ACCEPT_DECLINE_PADDING_END = 14.5
private const val ACCEPT_DECLINE_PADDING_TOP_BOTTOM = 8
private const val REQUEST_MESSAGE_FONT_SIZE = 20
private const val REQUEST_MESSAGE_LINE_HEIGHT = 20
private const val REQUEST_MESSAGE_FONT_WEIGHT = 500
private const val REQUEST_MESSAGE_LETTER_SPACING = 0.2
private const val SPACER_HEIGHT = 10
private const val ACCEPT_DECLINE_ICON_BUTTON_PADDING = 12
private const val ACCEPT_DECLINE_ICON_BUTTON_SIZE = 48
private const val ACCEPT_DECLINE_ICON_BUTTON_ROUNDED = 100
private const val ACCEPT_DECLINE_ICON_BUTTON_COLOR = 0xFF65558F

/**
 * Screen that appears when you click on a profile in the notification tab. You can accept or
 * decline the notification.
 *
 * @param profilesViewModel : view model of the profile
 * @param navBackStackEntry : used to retrieve the uid of the user that we clicked on
 * @param navigationActions : go back when we accept, decline or go back
 * @param tagsViewModel : used to display user's tag
 * @param meetingRequestViewModel : view model to send/receive meeting requests
 * @param isTesting : indicates if we are in testing mode or not
 */
@Composable
fun InboxProfileViewScreen(
    profilesViewModel: ProfilesViewModel,
    navBackStackEntry: NavBackStackEntry?,
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel,
    meetingRequestViewModel: MeetingRequestViewModel,
    isTesting: Boolean
) {

  LaunchedEffect(Unit) {
    val profileId = navBackStackEntry?.arguments?.getString("userId")
    if (profileId != null || isTesting) {
      profilesViewModel.getProfileByUid(profileId ?: "")
      meetingRequestViewModel.updateInboxOfMessagesAndThen {}
    }
  }

  val isLoading = profilesViewModel.loading.collectAsState().value
  val profile = profilesViewModel.selectedProfile.collectAsState().value
  val inboxItems = profilesViewModel.inboxItems.collectAsState()
  val context = LocalContext.current

  val message = inboxItems.value[profile]

  Scaffold(modifier = Modifier.fillMaxSize().testTag("NotificationProfileScreen")) { paddingValues
    ->
    if (isLoading) {
      MessageWhenLoadingProfile(paddingValues = paddingValues)
    } else if (profile?.fcmToken != null) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileHeader(
            profile = profile,
            navigationActions = navigationActions,
            myProfile = false,
            profilesViewModel = profilesViewModel,
            profileInNotification = true,
            onEditClick = null)
        Spacer(modifier = Modifier.height(SPACER_HEIGHT.dp))
        AcceptDeclineRequest(
            message ?: "",
            {
              acceptDeclineCode(
                  meetingRequestViewModel, navigationActions, profile.uid, true, profile.fcmToken)
            },
            {
              acceptDeclineCode(
                  meetingRequestViewModel,
                  navigationActions,
                  profile.uid,
                  false,
                  profile.fcmToken
              )
            })
        InfoSection(profile = profile, tagsViewModel = tagsViewModel)
      }
    }
  }
}

/**
 * Piece of code called when we accept or decline a meeting request. We need to send the response
 * (if we accepted or not), remove the user from your inbox, update the inbox and go back in the
 * notification screen
 *
 * @param meetingRequestViewModel : view model that receive/send messages
 * @param navigationActions : used to go back in the notification screen
 * @param uid : uid of the target user (user that send you the meeting request)
 * @param accepted : Boolean that indicates if you accepted or not the request
 * @param fcm : fcm of the target user (user that send you the meeting request)
 */
fun acceptDeclineCode(
    meetingRequestViewModel: MeetingRequestViewModel,
    navigationActions: NavigationActions,
    uid: String,
    accepted: Boolean,
    fcm: String
) {
  meetingRequestViewModel.setMeetingResponse(fcm, "accepting/decline request", accepted)
  meetingRequestViewModel.sendMeetingResponse()
  meetingRequestViewModel.removeFromMeetingRequestInbox(uid)
  meetingRequestViewModel.updateInboxOfMessagesAndThen {}
  navigationActions.goBack()
}

/**
 * Composable that shows the accept decline box with the meeting request message that goes with it
 *
 * @param requestMessage : message that the user sends you
 * @param onAcceptClick : code to execute when we accept the request
 * @param onDeclineClick : code to execute when we decline the request
 */
@Composable
fun AcceptDeclineRequest(
    requestMessage: String,
    onAcceptClick: () -> Unit = {},
    onDeclineClick: () -> Unit = {},
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  Box(
      modifier =
          Modifier.shadow(
                  elevation = ACCEPT_DECLINE_ELEVATION.dp,
                  shape = RoundedCornerShape(size = ACCEPT_DECLINE_ROUNDER_CORNER_SHAPE.dp),
                  spotColor = Color(ACCEPT_DECLINE_SHADOW_COLOR),
                  ambientColor = Color(ACCEPT_DECLINE_SHADOW_COLOR))
              .clip(RoundedCornerShape(size = ACCEPT_DECLINE_ROUNDER_CORNER_SHAPE.dp))
              .background(Color(ACCEPT_DECLINE_BOX_COLOR))
              .height(ACCEPT_DECLINE_BOX_HEIGHT.dp)
              .width((screenWidth.value * ACCEPT_DECLINE_BOX_WIDTH_FACTOR).dp)
              .padding(
                  start = ACCEPT_DECLINE_PADDING_START.dp,
                  end = ACCEPT_DECLINE_PADDING_END.dp,
                  top = ACCEPT_DECLINE_PADDING_TOP_BOTTOM.dp,
                  bottom = ACCEPT_DECLINE_PADDING_TOP_BOTTOM.dp)
              .testTag("Accept/DeclineBox")) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {
              Text(
                  text = requestMessage,
                  style =
                      TextStyle(
                          fontSize = REQUEST_MESSAGE_FONT_SIZE.sp,
                          lineHeight = REQUEST_MESSAGE_LINE_HEIGHT.sp,
                          fontWeight = FontWeight(REQUEST_MESSAGE_FONT_WEIGHT),
                          color = Color.Black,
                          letterSpacing = REQUEST_MESSAGE_LETTER_SPACING.sp,
                      ),
                  modifier = Modifier.testTag("RequestMessage"))
              Spacer(modifier = Modifier.height(SPACER_HEIGHT.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = onAcceptClick,
                    modifier =
                        Modifier.padding(ACCEPT_DECLINE_ICON_BUTTON_PADDING.dp)
                            .size(ACCEPT_DECLINE_ICON_BUTTON_SIZE.dp)
                            .clip(RoundedCornerShape(ACCEPT_DECLINE_ICON_BUTTON_ROUNDED.dp))
                            .background(Color(ACCEPT_DECLINE_ICON_BUTTON_COLOR))
                            .testTag("acceptButton")) {
                      Icon(
                          imageVector = Icons.Outlined.Check,
                          contentDescription = "Accept Request Button",
                          tint = Color.White)
                    }
                IconButton(
                    onClick = onDeclineClick,
                    modifier =
                        Modifier.padding(ACCEPT_DECLINE_ICON_BUTTON_PADDING.dp)
                            .size(ACCEPT_DECLINE_ICON_BUTTON_SIZE.dp)
                            .clip(RoundedCornerShape(ACCEPT_DECLINE_ICON_BUTTON_ROUNDED.dp))
                            .background(Color(ACCEPT_DECLINE_ICON_BUTTON_COLOR))
                            .testTag("declineButton")) {
                      Icon(
                          imageVector = Icons.Outlined.Close,
                          contentDescription = "Decline Request Button",
                          tint = Color.White)
                    }
              }
            }
      }
}
