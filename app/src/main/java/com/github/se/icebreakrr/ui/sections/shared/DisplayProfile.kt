package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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

/**
 * Displays the user's information section with a catchphrase, tags, and description. The content is
 * scrollable if it exceeds the available screen space.
 *
 * @param catchPhrase The user's catchphrase.
 * @param listOfTags A list of tags related to the user, paired with colors.
 * @param description The user's detailed description.
 */
@Composable
fun InfoSection(profile: Profile, tagsViewModel: TagsViewModel) {

    val scrollState = rememberScrollState()
    val userTags = profile.tags.map { tagString ->
        Pair(tagString, tagsViewModel.tagToColor(tagString))
    }

    Column(
        modifier =
        Modifier.fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("infoSection")) {
        // Catchphrase Section
        Spacer(modifier = Modifier.height(12.dp))
        ProfileCatchPhrase(profile.catchPhrase)

        // Tags Section
        Spacer(modifier = Modifier.height(8.dp))
        TagsSection(userTags)

        // Description Section
        Spacer(modifier = Modifier.height(16.dp))
        ProfileDescription(profile.description)
    }
}

/**
 * Displays the profile header section with a profile image, a back button, and an edit button. It
 * also shows the username in an overlay at the bottom of the profile image.
 *
 * @param navigationActions Actions to navigate between screens.
 */
@Composable
fun ProfileHeader(profile: Profile, navigationActions: NavigationActions, myProfile: Boolean, onEditClick: () -> Unit) {
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray, CircleShape)
                .testTag("profilePicture"),
            placeholder = painterResource(id = R.drawable.nopp), // Default image during loading
            error = painterResource(id = R.drawable.nopp)        // Fallback image if URL fails
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

            if (myProfile) {
                IconButton(
                    onClick = { onEditClick() },
                    modifier =
                    Modifier.background(Color.White, CircleShape)
                        .padding(4.dp)
                        .testTag("editButton")) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Edit Profile",
                        tint = Color.Gray)
                }
            } else {
                IconButton(
                    onClick = { onEditClick() },
                    modifier =
                    Modifier.background(Color.White, CircleShape)
                        .padding(4.dp)
                        .testTag("requestButton")) {
                    Icon(
                        imageVector = Icons.Filled.MailOutline,
                        contentDescription = "send request",
                        tint = Color.Gray)
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
      style = MaterialTheme.typography.bodyMedium,
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
  Box(modifier = Modifier.fillMaxWidth().height(80.dp).testTag("tagSection")) {
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
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black.copy(alpha = 0.9f),
      fontSize = 14.sp,
      textAlign = TextAlign.Start,
      modifier = Modifier.padding(8.dp).testTag("profileDescription"))
}
