package com.github.se.icebreakrr.ui.sections.shared

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.reportType
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.RowOfTags
import com.github.se.icebreakrr.ui.tags.TagStyle
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue

/**
 * Displays the user's information section with a catchphrase, tags, and description. The content is
 * scrollable if it exceeds the available screen space.
 *
 * @param catchPhrase The user's catchphrase.
 * @param listOfTags A list of tags related to the user, paired with colors.
 * @param description The user's detailed description.
 */
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

@Composable
fun InfoSection(profile: Profile, tagsViewModel: TagsViewModel) {

  val scrollState = rememberScrollState()
  val userTags =
      profile.tags.map { tagString -> Pair(tagString, tagsViewModel.tagToColor(tagString)) }

  Column(
      modifier =
          Modifier.fillMaxWidth().padding(16.dp).verticalScroll(scrollState).testTag("infoSection"),
      verticalArrangement = Arrangement.spacedBy(11.dp, Alignment.Top),
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

    var reportModal by remember { mutableStateOf(false) };
    var showReportOptions by remember { mutableStateOf(false) }
    var selectedReportType by remember { mutableStateOf<reportType?>(null) }
    var showBlockConfirmation by remember { mutableStateOf(false) }

  Box(
      modifier =
          Modifier.fillMaxWidth() // Make the image take full width
              .aspectRatio(1f) // Keep the aspect ratio 1:1 (height == width) => a square
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
                    .padding(16.dp)
                    .testTag("goBackButton")) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Go Back",
                  tint = Color.White)
            }

      // Flag button
      if (!myProfile){
          Box(
              modifier =
              Modifier.align(Alignment.TopEnd).padding(16.dp).size(requestButtonSize),
              contentAlignment = Alignment.Center) {
              IconButton(
                  onClick = { reportModal = true },
                  modifier = Modifier.testTag("flagButton")) {
                  Icon(
                      painter = painterResource(id = R.drawable.ic_flag),
                      contentDescription = "report/block user",
                      tint = Color.White,
                      modifier = Modifier.fillMaxSize(0.7f))
              }
          }

      }

        // Overlay with username and edit button
        Row(
            modifier =
                Modifier.align(Alignment.BottomStart) // Aligns this content to the bottom left
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

              // Username
              Text(
                  text = profile.name,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.testTag("username"))

              // Edit Button or message button
              if (myProfile) {
                Box(
                    modifier =
                        Modifier.size(requestButtonSize)
                            .shadow(requestButtonElevation, shape = CircleShape)
                            .background(IceBreakrrBlue, CircleShape),
                    contentAlignment = Alignment.Center) {
                      IconButton(
                          onClick = { onEditClick() }, modifier = Modifier.testTag("editButton")) {
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
                          onClick = { onEditClick() },
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

    if (reportModal){
        // Report modal
        AlertDialog(
            onDismissRequest = { reportModal = false },
            title = { Text("What would you like to do?") },
            text = {
                if (showReportOptions) {
                    Column {
                        Text("Select a reason for reporting:")
                        reportType.values().forEach { reportType ->
                            TextButton(
                                onClick = { selectedReportType = reportType },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    reportType.displayName,
                                    color = if (selectedReportType == reportType) IceBreakrrBlue else Color.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (showReportOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                reportModal = false
                                showReportOptions = false
                                selectedReportType = null
                            }
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                // Handle report submission
                                reportModal = false
                                showReportOptions = false
                                selectedReportType = null
                            },
                            enabled = selectedReportType != null
                        ) {
                            Text("Report")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showBlockConfirmation = true
                                reportModal = false
                            }
                        ) {
                            Text("Block")
                        }
                        TextButton(
                            onClick = { showReportOptions = true }
                        ) {
                            Text("Report")
                        }
                    }
                }
            },
            dismissButton = {
                if (!showReportOptions) {
                    TextButton(
                        onClick = { showReportOptions = false }
                    ) {
                        Text("Back")
                    }
                }
            },
            modifier = Modifier.testTag("alertDialogReportBlock")
        )
    }

    if (showBlockConfirmation) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmation = false },
            title = { Text("Block User") },
            text = { Text("Do you really want to block this user?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Handle block user logic here
                        showBlockConfirmation = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBlockConfirmation = false }
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("blockConfirmationDialog")
        )
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
