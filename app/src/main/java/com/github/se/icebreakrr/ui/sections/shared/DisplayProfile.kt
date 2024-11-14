package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.RowOfTags
import com.github.se.icebreakrr.ui.tags.TagStyle
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast

// Constants
private val INFO_SECTION_PADDING = 16.dp
private val INFO_SECTION_SPACING = 11.dp
private val CATCHPHRASE_TEXT_STYLE =
    TextStyle(
        fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.W400, color = IceBreakrrBlue)
private val TITLE_TEXT_STYLE =
    TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.W500,
        color = IceBreakrrBlue,
        letterSpacing = 0.1.sp)
private val DESCRIPTION_TEXT_STYLE =
    TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.W500,
        color = IceBreakrrBlue,
        letterSpacing = 0.15.sp)
private val TAG_HEIGHT_DP = 50.dp
private val REQUEST_BUTTON_SIZE = 55.dp
private val REQUEST_BUTTON_ELEVATION = 8.dp
private val BUTTON_ICON_SCALE = 0.7f
private val PROFILE_IMAGE_ASPECT_RATIO = 1f
private val PROFILE_IMAGE_PADDING = 16.dp
private val USERNAME_TEXT_STYLE =
    TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
private val ALPHA_CATCHPHRASE = 0.8f
private val ALPHA_DESCRIPTION = 0.9f
private val DESCRIPTION_PADDING = 4.dp

private val COLUMN_PADDING = 4.dp
private val VERTICAL_ARRANGEMENT = 2.dp

/**
 * Displays the information about a profile with the tags
 *
 * @param profile: the profile to be shown
 * @param tagsViewModel: the tagsViewModel to know the color pattern of each tag
 */
@Composable
fun InfoSection(profile: Profile, tagsViewModel: TagsViewModel) {
  val scrollState = rememberScrollState()
  val userTags =
      profile.tags.map { tagString -> Pair(tagString, tagsViewModel.tagToColor(tagString)) }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(INFO_SECTION_PADDING)
              .verticalScroll(scrollState)
              .testTag("infoSection"),
      verticalArrangement = Arrangement.spacedBy(INFO_SECTION_SPACING, Alignment.Top),
      horizontalAlignment = Alignment.Start) {
        // Catchphrase Section
        ProfileCatchPhrase(profile.catchPhrase)

        // Description Section
        Column(
            modifier = Modifier.padding(COLUMN_PADDING),
            verticalArrangement = Arrangement.spacedBy(VERTICAL_ARRANGEMENT)) {
              Text(text = "Description", style = TITLE_TEXT_STYLE)
              ProfileDescription(profile.description)
            }

        // Tags Section
        Column(
            modifier = Modifier.padding(COLUMN_PADDING),
            verticalArrangement = Arrangement.spacedBy(VERTICAL_ARRANGEMENT)) {
              Text(text = "Tags", style = TITLE_TEXT_STYLE)
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
    onEditClick: () -> Unit
) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .aspectRatio(PROFILE_IMAGE_ASPECT_RATIO)
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
            placeholder = painterResource(id = R.drawable.nopp),
            error = painterResource(id = R.drawable.nopp))

        // Back button
        IconButton(
            onClick = { navigationActions.goBack() },
            modifier =
                Modifier.align(Alignment.TopStart)
                    .padding(PROFILE_IMAGE_PADDING)
                    .testTag("goBackButton")) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Go Back",
                  tint = Color.White)
            }

        // Overlay with username and edit button
        Row(
            modifier =
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(PROFILE_IMAGE_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              // Username
              Text(
                  text = profile.name,
                  style = USERNAME_TEXT_STYLE,
                  modifier = Modifier.testTag("username"))

              val context = LocalContext.current
              // Edit Button or message button
              val buttonIcon =
                  if (myProfile) Icons.Filled.Create else Icons.AutoMirrored.Filled.Send
              val buttonDescription = if (myProfile) "Edit Profile" else "Send Request"
              val buttonTag = if (myProfile) "editButton" else "requestButton"
              Box(
                  modifier =
                      Modifier.size(REQUEST_BUTTON_SIZE)
                          .shadow(REQUEST_BUTTON_ELEVATION, shape = CircleShape)
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
                        modifier = Modifier.testTag(buttonTag)) {
                          Icon(
                              imageVector = buttonIcon,
                              contentDescription = buttonDescription,
                              tint = Color.White,
                              modifier = Modifier.fillMaxSize(BUTTON_ICON_SCALE))
                        }
                  }
            }
      }
}

@Composable
fun ProfileCatchPhrase(catchPhrase: String) {
  Text(
      text = catchPhrase,
      style = CATCHPHRASE_TEXT_STYLE,
      color = Color.Black.copy(alpha = ALPHA_CATCHPHRASE),
      textAlign = TextAlign.Left,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.testTag("catchPhrase"))
}

@Composable
fun TagsSection(listOfTags: List<Pair<String, Color>>) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height((listOfTags.size / 2 * TAG_HEIGHT_DP.value).dp)
              .testTag("tagSection")) {
        RowOfTags(listOfTags, TagStyle())
      }
}

@Composable
fun ProfileDescription(description: String) {
  Text(
      text = description,
      style = DESCRIPTION_TEXT_STYLE,
      color = Color.Black.copy(alpha = ALPHA_DESCRIPTION),
      modifier = Modifier.padding(DESCRIPTION_PADDING).testTag("profileDescription"))
}
