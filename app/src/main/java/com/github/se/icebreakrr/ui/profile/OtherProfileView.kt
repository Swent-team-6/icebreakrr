package com.github.se.icebreakrr.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.ai.AiViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.message.SendRequestScreen
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.shared.InfoSection
import com.github.se.icebreakrr.ui.sections.shared.MessageWhenLoadingProfile
import com.github.se.icebreakrr.ui.sections.shared.ProfileHeader
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast

/**
 * In Around You, when you click on a profile, this is the composable used to display it
 *
 * @param navigationActions Actions to navigate between screens.
 */
private val ALPHA = 0.5f
private val MET_BUTTON_HORIZTONAL_PADDING = 16.dp
private val BUTTON_VERTICAL_PADDING = 16.dp
private const val USER_ALREADY_SEND_REQUEST_TOAST_MESSAGE =
    "this user has already send you a meeting request!"

@Composable
fun OtherProfileView(
    profilesViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel,
    aiViewModel: AiViewModel,
    meetingRequestViewModel: MeetingRequestViewModel,
    navigationActions: NavigationActions,
    navBackStackEntry: NavBackStackEntry?
) {
  var sendRequest by remember { mutableStateOf(false) }
  var writtenMessage by remember { mutableStateOf("") }
  // retrieving user id from navigation params
  val profileId = navBackStackEntry?.arguments?.getString("userId")
  val context = LocalContext.current

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
      MessageWhenLoadingProfile(paddingValues)
    } else if (profile != null) {

      Column(
          modifier = Modifier.fillMaxWidth().padding(paddingValues),
          horizontalAlignment = Alignment.CenterHorizontally) {

            // 2 sections one for the profile image with overlay and
            // one for the information section
            ProfileHeader(profile, navigationActions, false, profilesViewModel, false) {
              // if the user already send you a meeting request, show a toast
              if (!profile.meetingRequestSent.contains(meetingRequestViewModel.senderUID)) {
                sendRequest = true
              } else {
                Toast.makeText(context, USER_ALREADY_SEND_REQUEST_TOAST_MESSAGE, Toast.LENGTH_SHORT)
                    .show()
                sendRequest = false
              }
            }
            InfoSection(profile, tagsViewModel)

            // Add spacer for some padding
            Spacer(modifier = Modifier.height(BUTTON_VERTICAL_PADDING))

            // Already met button
            Button(
                onClick = {
                  if (isNetworkAvailableWithContext(context)) {
                    profilesViewModel.addAlreadyMet(profile.uid)
                    Toast.makeText(context, R.string.Already_Met_Button_Success, Toast.LENGTH_SHORT)
                        .show()
                    profilesViewModel.getSelfProfile()
                    navigationActions.goBack()
                  } else {
                    showNoInternetToast(context = context)
                  }
                },
                colors =
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(MET_BUTTON_HORIZTONAL_PADDING)
                        .align(Alignment.CenterHorizontally)
                        .testTag("alreadyMetButton")) {
                  Text(text = stringResource(R.string.Already_Met_Button_Text), color = Color.White)
                }

            // Add bottom padding
            Spacer(modifier = Modifier.height(BUTTON_VERTICAL_PADDING))
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
                    meetingRequestViewModel.sendMeetingRequest()
                    meetingRequestViewModel.addToMeetingRequestSent(profile.uid)
                    writtenMessage = ""
                    navigationActions.goBack()
                  },
                  onCancelClick = { sendRequest = false })
            }
      }
    }
  }
}
