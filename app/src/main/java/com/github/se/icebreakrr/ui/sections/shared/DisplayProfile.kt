package com.github.se.icebreakrr.ui.sections.shared

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.reportType
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.RowOfTags
import com.github.se.icebreakrr.ui.tags.TagStyle
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast

private const val ASPECT_RATIO_SQUARE = 1f
private const val MAX_DIALOG_WIDTH_FACTOR = 0.95f
private const val FLAG_BUTTON_WRAP = 0.7f

private val PADDING_SMALL = 8.dp
private val PADDING_STANDARD = 16.dp
private val PADDING_LARGE = 24.dp
private val SPACE_BETWEEN_ELEMENTS = 11.dp

val informationTitleSize = 14.sp
val informationTitleLineHeight = 20.sp
val informationTitleFontWeight = 500
val informationTitleLetterSpacing = 0.1.sp
val catchPhraseSize = 22.sp
val catchPhraseLineHeight = 28.sp
val catchPhraseWeight = 400
val descriptionFontSize = 16.sp
val descriptionLineHeight = 24.sp
val descriptionFontWeight = 500
val descriptionLetterSpacing = 0.15.sp
val tagHeight = 40
val requestButtonSize = 55.dp
val requestButtonElevation = 8.dp

private val modalTitleSize = 20.sp

@Composable
fun InfoSection(profile: Profile, tagsViewModel: TagsViewModel) {

  val scrollState = rememberScrollState()
  val userTags =
      profile.tags.map { tagString -> Pair(tagString, tagsViewModel.tagToColor(tagString)) }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(PADDING_STANDARD)
              .verticalScroll(scrollState)
              .testTag("infoSection"),
      verticalArrangement = Arrangement.spacedBy(SPACE_BETWEEN_ELEMENTS, Alignment.Top),
      horizontalAlignment = Alignment.Start) {
        // Catchphrase Section
        ProfileCatchPhrase(profile.catchPhrase)

        // Description Section
        Text(
            text = "Description",
            style =
                TextStyle(
                    fontSize = informationTitleSize,
                    lineHeight = informationTitleLineHeight,
                    fontWeight = FontWeight(informationTitleFontWeight),
                    color = IceBreakrrBlue,
                    letterSpacing = informationTitleLetterSpacing,
                ))
        ProfileDescription(profile.description)

        // Tags Section
        Text(
            text = "Tags",
            style =
                TextStyle(
                    fontSize = informationTitleSize,
                    lineHeight = informationTitleLineHeight,
                    fontWeight = FontWeight(informationTitleFontWeight),
                    color = IceBreakrrBlue,
                    letterSpacing = informationTitleLetterSpacing,
                ))
        TagsSection(userTags)
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
    onEditClick: () -> Unit
) {

  val context = LocalContext.current

  var blockReportModal by remember { mutableStateOf(false) }
  var showReportOptions by remember { mutableStateOf(false) }
  var selectedReportType by remember { mutableStateOf<reportType?>(null) }
  var showBlockConfirmation by remember { mutableStateOf(false) }

  Box(
      modifier =
          Modifier.fillMaxWidth() // Make the image take full width
              .aspectRatio(
                  ASPECT_RATIO_SQUARE) // Keep the aspect ratio 1:1 (height == width) => a square
              .background(Color.LightGray)
              .testTag("profileHeader")) {

        // Profile image
        AsyncImage(
            model = profile.profilePictureUrl,
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.LightGray, CircleShape)
                    .testTag("profilePicture"),
            placeholder = painterResource(id = R.drawable.nopp), // Default image during loading
            error = painterResource(id = R.drawable.nopp) // Fallback image if URL fails
            )

        // Back button
        IconButton(
            onClick = { navigationActions.goBack() },
            modifier =
                Modifier.align(Alignment.TopStart) // Align to the top start (top-left corner)
                    .padding(PADDING_STANDARD)
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
                  Modifier.align(Alignment.TopEnd)
                      .padding(PADDING_STANDARD)
                      .size(requestButtonSize),
              contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = { blockReportModal = true },
                    modifier = Modifier.testTag("flagButton")) {
                      Icon(
                          painter = painterResource(id = R.drawable.flag),
                          contentDescription = "report/block user",
                          tint = Color.Red,
                          modifier = Modifier.fillMaxSize(FLAG_BUTTON_WRAP))
                    }
              }
        }

        // Overlay with username and edit button
        Row(
            modifier =
                Modifier.align(Alignment.BottomStart) // Aligns this content to the bottom left
                    .fillMaxWidth()
                    .padding(PADDING_STANDARD),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

              // Username
              Text(
                  text = profile.name,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.testTag("username"))

              val context = LocalContext.current
              // Edit Button or message button
              if (myProfile) {
                Box(
                    modifier =
                        Modifier.size(requestButtonSize)
                            .shadow(requestButtonElevation, shape = CircleShape)
                            .background(IceBreakrrBlue, CircleShape),
                    contentAlignment = Alignment.Center) {
                      IconButton(
                          onClick = {
                            if (isNetworkAvailable(context = context)) {
                              onEditClick()
                            } else {
                              showNoInternetToast(context = context)
                            }
                          },
                          modifier = Modifier.testTag("editButton")) {
                            Icon(
                                imageVector = Icons.Filled.Create,
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(0.7f))
                          }
                    }
              } else {
                Box(
                    modifier =
                        Modifier.size(requestButtonSize)
                            .shadow(requestButtonElevation, shape = CircleShape)
                            .background(IceBreakrrBlue, CircleShape),
                    contentAlignment = Alignment.Center) {
                      IconButton(
                          onClick = {
                            if (isNetworkAvailable(context = context)) {
                              onEditClick()
                            } else {
                              showNoInternetToast(context = context)
                            }
                          },
                          modifier = Modifier.testTag("requestButton")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "send request",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(0.7f))
                          }
                    }
              }
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
                  Modifier.widthIn(max = maxWidth)
                      .padding(PADDING_STANDARD)
                      .testTag("alertDialogReportBlock")) {
                Column(
                    modifier = Modifier.padding(PADDING_LARGE),
                    verticalArrangement = Arrangement.spacedBy(PADDING_SMALL)) {
                      Text(
                          text = stringResource(R.string.block_report_modal_title),
                          style = MaterialTheme.typography.titleLarge,
                          fontSize = modalTitleSize,
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
                                          else Color.Black)
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
                                    Text(stringResource(R.string.cancel))
                                  }
                              TextButton(
                                  onClick = {
                                    blockReportModal = false
                                    showReportOptions = false
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
                                    Text(stringResource(R.string.cancel))
                                  }
                              TextButton(
                                  onClick = {
                                    showBlockConfirmation = false
                                    blockReportModal = false
                                    Toast.makeText(
                                            context,
                                            context.getString(R.string.block_success_message),
                                            Toast.LENGTH_SHORT)
                                        .show()
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
                                Text(stringResource(R.string.cancel))
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
 * Displays the user's catchphrase in a medium-sized font with a maximum of two lines.
 *
 * @param catchPhrase The user's catchphrase.
 */
@Composable
fun ProfileCatchPhrase(catchPhrase: String) {
  Text(
      text = catchPhrase,
      style =
          TextStyle(
              fontSize = catchPhraseSize,
              lineHeight = catchPhraseLineHeight,
              fontWeight = FontWeight(catchPhraseWeight),
              color = IceBreakrrBlue,
          ),
      color = Color.Black.copy(alpha = 0.8f),
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
      textAlign = TextAlign.Left,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.testTag("catchPhrase"))
}

/**
 * Displays a section with a row of tags, each associated with a color.
 *
 * @param listOfTags A list of tags, each paired with a color.
 */
@Composable
fun TagsSection(listOfTags: List<Pair<String, Color>>) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height((((listOfTags.size / 2) * tagHeight).dp))
              .testTag("tagSection")) {
        RowOfTags(listOfTags, TagStyle())
      }
}

/**
 * Displays the user's profile description in a medium-sized font, aligned to the start.
 *
 * @param description The user's description or bio.
 */
@Composable
fun ProfileDescription(description: String) {
  Text(
      text = description,
      style =
          TextStyle(
              fontSize = descriptionFontSize,
              lineHeight = descriptionLineHeight,
              fontWeight = FontWeight(descriptionFontWeight),
              color = IceBreakrrBlue,
              letterSpacing = descriptionLetterSpacing),
      color = Color.Black.copy(alpha = 0.9f),
      fontSize = 14.sp,
      textAlign = TextAlign.Start,
      modifier = Modifier.padding(8.dp).testTag("profileDescription"))
}
