package com.github.se.icebreakrr.ui.sections.shared

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.profile.reportType
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.RowOfTags
import com.github.se.icebreakrr.ui.tags.TagStyle
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast

// Constants
private val INFO_SECTION_PADDING = 16.dp
private val INFO_SECTION_SPACING = 16.dp
private val TAG_HEIGHT_DP = 54.dp
private val REQUEST_BUTTON_SIZE = 55.dp
private val REQUEST_BUTTON_ELEVATION = 8.dp
private val BUTTON_ICON_SCALE = 0.7f
private val PROFILE_IMAGE_ASPECT_RATIO = 1f
private val PROFILE_IMAGE_PADDING = 16.dp
private val USERNAME_TEXT_STYLE =
    TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
private val CONTENT_TEXT_STYLE =
    TextStyle(
        fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.W400, letterSpacing = 0.1.sp)
private val INFO_SUBTITLE_STYLE =
    TextStyle(
        fontSize = 18.sp, lineHeight = 20.sp, fontWeight = FontWeight.W500, letterSpacing = 0.1.sp)
private val CATCHPHRASE_TEXT_STYLE =
    TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.W400, fontStyle = FontStyle.Italic)

private val DESCRIPTION_PADDING = 4.dp
private val COLUMN_PADDING = 4.dp
private val VERTICAL_ARRANGEMENT = 2.dp

private val PADDING_SMALL = 8.dp
private val PADDING_STANDARD = 16.dp
private val PADDING_LARGE = 24.dp
private const val MAX_DIALOG_WIDTH_FACTOR = 0.95f
private const val FLAG_BUTTON_WRAP = 0.7f
private val MODAL_TITLE_SIZE = 20.sp
private val USERNAME_BOX_SIZE = 60.dp

// a transparent vertical gradient to make the username more readable
private val USERNAME_BG_GRADIENT =
    Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.4f))

// a transparent radial gradiant to make buttons more readable
private val BUTTON_BG_GRADIENT =
    Brush.radialGradient(0f to Color.Black.copy(alpha = 0.15f), 1f to Color.Transparent)

/**
 * Displays the information about a profile with the tags
 *
 * @param profile: the profile to be shown
 * @param tagsViewModel: the tagsViewModel to know the color pattern of each tag
 */
@Composable
fun InfoSection(profile: Profile, tagsViewModel: TagsViewModel) {
  val userTags =
      profile.tags.map { tagString -> Pair(tagString, tagsViewModel.tagToColor(tagString)) }

  Column(
      modifier = Modifier
          .fillMaxWidth()
          .padding(INFO_SECTION_PADDING)
          .testTag("infoSection"),
      verticalArrangement = Arrangement.spacedBy(INFO_SECTION_SPACING, Alignment.Top),
      horizontalAlignment = Alignment.Start) {
        // Catchphrase Section
        ProfileCatchPhrase(profile.catchPhrase)

        // Description Section
        Column(
            modifier = Modifier.padding(COLUMN_PADDING),
            verticalArrangement = Arrangement.spacedBy(VERTICAL_ARRANGEMENT)) {
              Text(
                  text = "Description",
                  style = INFO_SUBTITLE_STYLE,
                  color = MaterialTheme.colorScheme.secondary)
              ProfileDescription(profile.description)
            }

        // Tags Section
        Column(
            modifier = Modifier.padding(COLUMN_PADDING),
            verticalArrangement = Arrangement.spacedBy(VERTICAL_ARRANGEMENT)) {
              Text(
                  text = "Tags",
                  style = INFO_SUBTITLE_STYLE,
                  color = MaterialTheme.colorScheme.secondary)
              TagsSection(userTags)
            }
      }
}

/**
 * Displays the profile header section with a profile image, a back button, and an edit button. It
 * also shows the username in an overlay at the bottom of the profile image.
 *
 * @param navigationActions Actions to navigate between screens.
 * @param myProfile : set to true if it is used to see our profile and false if it is used to see
 *   someone else's profile
 * @param onEditClick : function called when you click on the edit button (if you are on your
 *   profile) or on the message button (if you are on someone else's profile)
 */
@Composable
fun ProfileHeader(
    profile: Profile,
    navigationActions: NavigationActions,
    myProfile: Boolean,
    profilesViewModel: ProfilesViewModel,
    profileInNotification: Boolean,
    onEditClick: (() -> Unit)?
) {

  val context = LocalContext.current

  var blockReportModal by remember { mutableStateOf(false) }
  var showReportOptions by remember { mutableStateOf(false) }
  var selectedReportType by remember { mutableStateOf<reportType?>(null) }
  var showBlockConfirmation by remember { mutableStateOf(false) }

  Box(
      modifier =
      Modifier
          .fillMaxWidth()
          .aspectRatio(PROFILE_IMAGE_ASPECT_RATIO)
          .background(Color.LightGray)
          .testTag("profileHeader")) {

        // Profile image
        AsyncImage(
            model = profile.profilePictureUrl,
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("profilePicture"),
            placeholder = painterResource(id = R.drawable.nopp),
            error = painterResource(id = R.drawable.nopp))

        // Back button
        IconButton(
            onClick = { navigationActions.goBack() },
            modifier =
            Modifier
                .align(Alignment.TopStart)
                .padding(PROFILE_IMAGE_PADDING)
                .background(BUTTON_BG_GRADIENT, shape = CircleShape)
                .testTag("goBackButton")) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Go Back",
                  tint = Color.White)
            }

        // Flag button
        if (!myProfile) {
          Box(
              modifier =
              Modifier
                  .align(Alignment.TopEnd)
                  .padding(PADDING_STANDARD)
                  .size(REQUEST_BUTTON_SIZE),
              contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = { blockReportModal = true },
                    modifier =
                    Modifier
                        .background(BUTTON_BG_GRADIENT, shape = CircleShape)
                        .testTag("flagButton")) {
                      Icon(
                          painter = painterResource(id = R.drawable.flag),
                          contentDescription = "report/block user",
                          tint = Color.Red,
                          modifier = Modifier.fillMaxSize(FLAG_BUTTON_WRAP))
                    }
              }
        }

        // Edit Button or message button
        if (!profileInNotification) {
          val buttonIcon = if (myProfile) Icons.Filled.Create else Icons.AutoMirrored.Filled.Send
          val buttonDescription = if (myProfile) "Edit Profile" else "Send Request"
          val buttonTag = if (myProfile) "editButton" else "requestButton"
          Box(
              modifier =
              Modifier
                  .align(Alignment.BottomEnd)
                  .padding(PADDING_STANDARD)
                  .size(REQUEST_BUTTON_SIZE)
                  .shadow(REQUEST_BUTTON_ELEVATION, shape = CircleShape)
                  .background(MaterialTheme.colorScheme.primary, CircleShape),
              contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                      if (isNetworkAvailable()) {
                        if (onEditClick != null) {
                          onEditClick()
                        }
                      } else {
                        showNoInternetToast(context = context)
                      }
                    },
                    modifier = Modifier.testTag(buttonTag)) {
                      Icon(
                          imageVector = buttonIcon,
                          contentDescription = buttonDescription,
                          tint = Color.White,
                          modifier = Modifier.fillMaxSize(BUTTON_ICON_SCALE))
                    }
              }
        }

        // Username
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .height(USERNAME_BOX_SIZE)
                .background(USERNAME_BG_GRADIENT)) {
              Text(
                  text = profile.name,
                  style = USERNAME_TEXT_STYLE,
                  modifier =
                  Modifier
                      .align(Alignment.CenterStart)
                      .padding(horizontal = PADDING_STANDARD)
                      .testTag("username"))
            }
      }

  if (blockReportModal) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val maxWidth = screenWidth * MAX_DIALOG_WIDTH_FACTOR

    Dialog(
        onDismissRequest = {
          blockReportModal = false
          showReportOptions = false
          showBlockConfirmation = false
        }) {
          Card(
              modifier =
              Modifier
                  .widthIn(max = maxWidth)
                  .padding(PADDING_STANDARD)
                  .testTag("alertDialogReportBlock")) {
                Column(
                    modifier = Modifier.padding(PADDING_LARGE),
                    verticalArrangement = Arrangement.spacedBy(PADDING_SMALL)) {
                      Text(
                          text = stringResource(R.string.block_report_modal_title),
                          style = MaterialTheme.typography.titleLarge,
                          fontSize = MODAL_TITLE_SIZE,
                          modifier = Modifier.padding(bottom = PADDING_SMALL))
                      // Content
                      if (showReportOptions) {
                        Column {
                          Text(stringResource(R.string.report_reason_prompt))
                          reportType.values().forEach { reportType ->
                            TextButton(
                                onClick = { selectedReportType = reportType },
                                modifier = Modifier.fillMaxWidth()) {
                                  Text(
                                      reportType.displayName,
                                      color =
                                          if (selectedReportType == reportType) IceBreakrrBlue
                                          else MaterialTheme.colorScheme.onSecondary)
                                }
                          }
                        }
                      } else if (showBlockConfirmation) {
                        Text(stringResource(R.string.block_confirmation_message))
                      }
                      // Buttons
                      if (showReportOptions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                              TextButton(
                                  onClick = {
                                    blockReportModal = false
                                    showReportOptions = false
                                    selectedReportType = null
                                  }) {
                                    Text(
                                        stringResource(R.string.cancel),
                                        color = MaterialTheme.colorScheme.error)
                                  }
                              TextButton(
                                  onClick = {
                                    profilesViewModel.reportUser(selectedReportType!!)
                                    blockReportModal = false
                                    showReportOptions = false
                                    navigationActions.goBack()
                                    Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.report_success_message,
                                                selectedReportType?.displayName),
                                            Toast.LENGTH_SHORT)
                                        .show()
                                    selectedReportType = null
                                  },
                                  enabled = selectedReportType != null) {
                                    Text("Report")
                                  }
                            }
                      } else if (showBlockConfirmation) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                              TextButton(
                                  onClick = {
                                    showBlockConfirmation = false
                                    blockReportModal = false
                                  }) {
                                    Text(
                                        stringResource(R.string.cancel),
                                        color = MaterialTheme.colorScheme.error)
                                  }
                              TextButton(
                                  onClick = {
                                    if (isNetworkAvailableWithContext(context)) {
                                      profilesViewModel.blockUser(profile.uid)
                                      Toast.makeText(
                                              context,
                                              R.string.block_success_message,
                                              Toast.LENGTH_SHORT)
                                          .show()
                                      profilesViewModel.getSelfProfile {}
                                      navigationActions.goBack()
                                    } else {
                                      showNoInternetToast(context = context)
                                    }
                                    showBlockConfirmation = false
                                    blockReportModal = false
                                  }) {
                                    Text(stringResource(R.string.block))
                                  }
                            }
                      } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                              TextButton(onClick = { blockReportModal = false }) {
                                Text(
                                    stringResource(R.string.cancel),
                                    color = MaterialTheme.colorScheme.error)
                              }

                              Row {
                                TextButton(onClick = { showBlockConfirmation = true }) {
                                  Text(stringResource(R.string.block))
                                }
                                TextButton(onClick = { showReportOptions = true }) {
                                  Text(stringResource(R.string.report))
                                }
                              }
                            }
                      }
                    }
              }
        }
  }
}

/**
 * Displays the profile's catchphrase
 *
 * @param catchPhrase the catchphrase of the profile
 */
@Composable
fun ProfileCatchPhrase(catchPhrase: String) {
  Text(
      text = "«${catchPhrase}»",
      style = CATCHPHRASE_TEXT_STYLE,
      color = MaterialTheme.colorScheme.onSecondary,
      textAlign = TextAlign.Left,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.testTag("catchPhrase"))
}

/**
 * Displays the profile's tags
 *
 * @param listOfTags the list of the profile's tags
 */
@Composable
fun TagsSection(listOfTags: List<Pair<String, Color>>) {
  Box(modifier = Modifier
      .fillMaxWidth()
      .testTag("tagSection")) {
    RowOfTags(listOfTags, TagStyle())
  }
}

/**
 * Displays the profile's description
 *
 * @param description the description of the profile
 */
@Composable
fun ProfileDescription(description: String) {
  Text(
      text = description,
      style = CONTENT_TEXT_STYLE,
      color = MaterialTheme.colorScheme.onSecondary,
      modifier = Modifier
          .padding(vertical = DESCRIPTION_PADDING)
          .testTag("profileDescription"))
}

@Composable
fun MessageWhenLoadingProfile(paddingValues: PaddingValues) {
  Box(
      modifier =
      Modifier
          .fillMaxSize()
          .background(Color.LightGray)
          .padding(paddingValues)
          .testTag("loadingBox"),
      contentAlignment = Alignment.Center) {
        Text("Loading profile...", textAlign = TextAlign.Center)
      }
}
