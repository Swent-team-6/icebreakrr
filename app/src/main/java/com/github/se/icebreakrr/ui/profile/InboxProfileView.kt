package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun InboxProfileViewScreen(
    profilesViewModel: ProfilesViewModel,
    navBackStackEntry: NavBackStackEntry?,
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel,
    meetingRequestViewModel: MeetingRequestViewModel
) {

  LaunchedEffect(Unit) {
    val profileId = navBackStackEntry?.arguments?.getString("userId")
    if (profileId != null) {
      profilesViewModel.getProfileByUid(profileId)
      meetingRequestViewModel.updateInboxOfMessages()
    }
  }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val profile = profilesViewModel.selectedProfile.collectAsState().value
  val inboxItems = profilesViewModel.inboxItems.collectAsState()
  val context = LocalContext.current

  val message = inboxItems.value[profile]

  Scaffold(modifier = Modifier.fillMaxSize().testTag("NotificationProfileScreen")) { paddingValues
    ->
    if (isLoading) {
      MessageWhenLoadingProfile(paddingValues = paddingValues)
    } else if (profile != null) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileHeader(
            profile = profile,
            navigationActions = navigationActions,
            myProfile = false,
            profilesViewModel = profilesViewModel,
            profileInNotification = true,
            onEditClick = {})
        Spacer(modifier = Modifier.height(10.dp))
        AcceptDeclineRequest(
            message ?: "",
            {
              acceptDeclineCode(
                  meetingRequestViewModel, navigationActions, profile.uid, true, profile.fcmToken!!)
            },
            {
              acceptDeclineCode(
                  meetingRequestViewModel,
                  navigationActions,
                  profile.uid,
                  false,
                  profile.fcmToken!!)
            })
        InfoSection(profile = profile, tagsViewModel = tagsViewModel)
      }
    }
  }
}

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
  meetingRequestViewModel.updateInboxOfMessages()
  navigationActions.goBack()
}

@Composable
@Preview
fun AcceptDeclineRequest(
    requestMessage: String = "Hey, do you want to meet in front of the coop?",
    onAcceptClick: () -> Unit = {},
    onDeclineClick: () -> Unit = {},
) {
  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp.dp
  val screenWidth = configuration.screenWidthDp.dp
  Box(
      modifier =
          Modifier.shadow(
                  elevation = 4.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
              .height(115.dp)
              .width((screenWidth.value * 0.9).dp)
              .background(Color(0xFFEAEEFF), RoundedCornerShape(size = 23.dp))
              .padding(start = 10.5.dp, end = 14.5.dp, top = 8.dp, bottom = 8.dp)) {
        Column(
            modifier =
                Modifier.fillMaxSize(1f)
                    .background(Color(0xFFEAEEFF), RoundedCornerShape(size = 23.dp)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {
              Text(
                  text = requestMessage,
                  style =
                      TextStyle(
                          fontSize = 20.sp,
                          lineHeight = 20.sp,
                          fontWeight = FontWeight(500),
                          color = Color(0xFF000000),
                          letterSpacing = 0.2.sp,
                      ))
              Spacer(modifier = Modifier.height(10.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = onAcceptClick,
                    modifier =
                        Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
                            .width(48.dp)
                            .height(48.dp)
                            .background(
                                color = Color(0xFF65558F),
                                shape = RoundedCornerShape(size = 100.dp))) {
                      Icon(
                          imageVector = Icons.Outlined.Check,
                          contentDescription = "Accept Request Button",
                          tint = Color.White)
                    }
                IconButton(
                    onClick = onDeclineClick,
                    modifier =
                        Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
                            .width(48.dp)
                            .height(48.dp)
                            .background(
                                color = Color(0xFF65558F),
                                shape = RoundedCornerShape(size = 100.dp))) {
                      Icon(
                          imageVector = Icons.Outlined.Close,
                          contentDescription = "Decline Request Button",
                          tint = Color.White)
                    }
              }
            }
      }
}
