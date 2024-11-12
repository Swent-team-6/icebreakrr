package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.message.SendRequestScreen
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.shared.InfoSection
import com.github.se.icebreakrr.ui.sections.shared.ProfileHeader

/**
 * In Around You, when you click on a profile, this is the composable used to display it
 *
 * @param navigationActions Actions to navigate between screens.
 */
private val ALPHA = 0.5f

@Composable
fun OtherProfileView(
    profilesViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel,
    meetingRequestViewModel: MeetingRequestViewModel,
    navigationActions: NavigationActions,
    navBackStackEntry: NavBackStackEntry?
) {
  var sendRequest by remember { mutableStateOf(false) }
  var writtenMessage by remember { mutableStateOf("") }
  // retrieving user id from navigation params
  val profileId = navBackStackEntry?.arguments?.getString("userId")

  // Launch a coroutine to fetch the profile when this composable is first displayed
  LaunchedEffect(Unit) {
    if (profileId != null) {
      profilesViewModel.getProfileByUid(profileId)
    }
  }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val profile = profilesViewModel.selectedProfile.collectAsState().value

  Scaffold(modifier = Modifier.fillMaxSize().testTag("aroundYouProfileScreen")) { paddingValues ->
    if (isLoading) {
      Box(
          modifier =
              Modifier.fillMaxSize()
                  .background(Color.LightGray)
                  .padding(paddingValues)
                  .testTag("loadingBox"),
          contentAlignment = Alignment.Center) {
            Text("Loading profile...", textAlign = TextAlign.Center)
          }
    } else if (profile != null) {

      Column(
          modifier = Modifier.fillMaxWidth().padding(paddingValues),
          horizontalAlignment = Alignment.CenterHorizontally) {

            // 2 sections one for the profile image with overlay and
            // one for the information section
            ProfileHeader(profile, navigationActions, false) { sendRequest = true }
            InfoSection(profile, tagsViewModel)
          }

      // this displays the request messaging system
      if (sendRequest) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = ALPHA))
                    .clickable {}
                    .testTag("bluredBackground"),
            contentAlignment = Alignment.Center) {
              SendRequestScreen(
                  onValueChange = { writtenMessage = it },
                  value = writtenMessage,
                  onSendClick = {
                    meetingRequestViewModel.onMeetingRequestChange(writtenMessage)
                    meetingRequestViewModel.onLocalTokenChange(profile.fcmToken ?: "null")
                    meetingRequestViewModel.onSubmitMeetingRequest()
                    meetingRequestViewModel.sendMessage()
                    writtenMessage = ""
                    navigationActions.goBack()
                  },
                  onCancelClick = { sendRequest = false })
            }
      }
    }
  }
}
