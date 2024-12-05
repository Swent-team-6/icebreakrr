package com.github.se.icebreakrr.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
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
private val BUTTONS_HORIZONTAL_PADDING = 16.dp
private val BUTTON_VERTICAL_PADDING = 16.dp
private val SHEET_INNER_PADDING = 16.dp
private val MIN_SHEET_HEIGHT = 400.dp
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
  var bottomSheetVisible by remember { mutableStateOf(false) }
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

  // todo: replace these two values by actual AI viewmodel flows
  val isAILoading = false
  val aiSuggestion =
      "Hey Botond! As a fellow lover of details, I can appreciate your game strategy on the tennis court—let's rally some ideas about how we can serve up solutions for climate change and immigration reform!"

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

            // Scrollable content
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
              InfoSection(profile, tagsViewModel)

              // Add spacer for some padding
              Spacer(modifier = Modifier.height(BUTTON_VERTICAL_PADDING))

              // Ai button
              Button(
                  onClick = { bottomSheetVisible = true },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.primary),
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(BUTTONS_HORIZONTAL_PADDING)
                          .align(Alignment.CenterHorizontally)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(text = stringResource(R.string.AI_button), color = Color.White)
                          Spacer(modifier = Modifier.width(8.dp))

                          Icon(
                              imageVector = Icons.Filled.Warning,
                              contentDescription = "AI icon") // todo: replace by AI icon
                    }
                  }

              // Already met button
              Button(
                  onClick = {
                    if (isNetworkAvailableWithContext(context)) {
                      profilesViewModel.addAlreadyMet(profile.uid)
                      meetingRequestViewModel.removeChosenLocalisation(profile.uid)Toast.makeText(
                              context, R.string.Already_Met_Button_Success, Toast.LENGTH_SHORT)
                          .show()
                      profilesViewModel.getSelfProfile{}
                      navigationActions.goBack()
                    } else {
                      showNoInternetToast(context = context)
                    }
                  },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.primary),
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(BUTTONS_HORIZONTAL_PADDING)
                          .align(Alignment.CenterHorizontally)
                          .testTag("alreadyMetButton")) {
                    Text(
                        text = stringResource(R.string.Already_Met_Button_Text),
                        color = MaterialTheme.colorScheme.onPrimary)
                  }

              // Add bottom padding
              Spacer(modifier = Modifier.height(BUTTON_VERTICAL_PADDING))
            }
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

      // this displays the bottom sheet
      if (bottomSheetVisible) {
        BottomSheet(isLoading = isAILoading, aiSuggestion = aiSuggestion) {
          bottomSheetVisible = false
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(isLoading: Boolean, aiSuggestion: String, onDismissRequest: () -> Unit) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
      sheetState = sheetState,
      onDismissRequest = onDismissRequest,
  ) {
    Column(
        modifier =
            Modifier.fillMaxWidth().heightIn(min = MIN_SHEET_HEIGHT).padding(SHEET_INNER_PADDING),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start) {
          // Header
          Text(
              text = "Here is a possible starter:",
              fontWeight = FontWeight.Bold,
              fontSize = TextUnit(25f, TextUnitType.Sp),
              lineHeight = TextUnit(30f, TextUnitType.Sp))

          Spacer(modifier = Modifier.height(16.dp))

          // Content
          if (!isLoading) {
            Text(
                text = "\"$aiSuggestion\"",
                fontWeight = FontWeight.Normal,
                fontSize = TextUnit(20f, TextUnitType.Sp),
                lineHeight = TextUnit(25f, TextUnitType.Sp))
          } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
          }
        }
  }
}
