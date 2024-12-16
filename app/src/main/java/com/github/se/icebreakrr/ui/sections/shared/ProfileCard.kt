package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.google.accompanist.flowlayout.FlowRow

// Define constants for layout dimensions and other configurations
private val CARD_CORNER_RADIUS = 14.dp
private val CARD_MAX_HEIGHT = 200.dp
private val CARD_PADDING_HORIZONTAL = 20.dp
private val CARD_PADDING_VERTICAL = 20.dp
private val CARD_ELEVATION = 2.dp
private val IMAGE_SIZE = 80.dp
private val IMAGE_SPACING = 18.dp
private val TEXT_SPACER_PADDING = 3.dp

// Define constants for font sizes
private val NAME_FONT_SIZE = 18.sp
private val CATCHPHRASE_FONT_SIZE = 16.sp
private val TAGS_FONT_SIZE = 10.sp

// Define colors used

// Define math constants
private val TAKE_TAGS = 3

@Composable
fun TagDisplay(tags: List<String>, isSettings: Boolean) {
  if (!isSettings) {
    // Determine the number of tags to display
    val tagsToDisplay =
        when {
          tags.size > 3 -> 2 // Display only 2 if there are more than 3 tags
          else -> tags.size // Otherwise, display all available tags
        }

    // Display the tags with a background and spacing
    FlowRow(modifier = Modifier.fillMaxWidth().padding(TEXT_SPACER_PADDING)) {
      // Create a Text for each tag, limiting to the calculated number of tags
      tags.take(tagsToDisplay).forEachIndexed { index, tag ->
        Text(
            text = "#$tag",
            fontSize = TAGS_FONT_SIZE,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier =
                Modifier.padding(2.dp)
                    .clip(RoundedCornerShape(25.dp)) // Rounded corners for each tag
                    .background(MaterialTheme.colorScheme.secondaryContainer) // Tag background color
                    .padding(horizontal = 10.dp, vertical = 4.dp) // Padding inside the tag
            )
      }

      // If there are more than 3 tags, add "..." at the end
      if (tags.size > 3) {
        Text(
            text = "...",
            fontSize = TAGS_FONT_SIZE,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier =
                Modifier.padding(2.dp)
                    .clip(RoundedCornerShape(25.dp)) // Rounded corners for the ellipsis
                    .background(MaterialTheme.colorScheme.secondaryContainer) // Tag background color
                    .padding(horizontal = 10.dp, vertical = 4.dp) // Padding inside the ellipsis
            )
      }
    }
  } else {
    Text(text = "Tap to preview profile", color=MaterialTheme.colorScheme.secondaryContainer, fontWeight = FontWeight.Bold)
  }
}

/**
 * Displays the ProfileCard, a summarized version of the profiles, that are used in the AroundYou
 * screen and Notification screen
 *
 * @param profile the profile to be displayed
 * @param isSettings determines if it is the profile in the Setting's menu or not
 * @param greyedOut sets if the card needs to be grayed out or not
 * @param onclick the on click action
 */
@Composable
fun ProfileCard(
    profile: Profile,
    isSettings: Boolean = false,
    greyedOut: Boolean = false,
    onclick: () -> Unit
) {
  Card(
      onClick = onclick,
      shape = RoundedCornerShape(CARD_CORNER_RADIUS),
      elevation = CardDefaults.cardElevation(defaultElevation = CARD_ELEVATION),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondary),
      modifier = Modifier.fillMaxWidth().heightIn(max = CARD_MAX_HEIGHT).testTag("profileCard")) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(IMAGE_SPACING),
            verticalAlignment = Alignment.Top,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = CARD_PADDING_HORIZONTAL, vertical = CARD_PADDING_VERTICAL)) {
              AsyncImage(
                  model = profile.profilePictureUrl,
                  contentDescription = "profile picture",
                  modifier = Modifier.size(IMAGE_SIZE).clip(CircleShape),
                  placeholder =
                      painterResource(id = R.drawable.nopp), // Default image during loading
                  error = painterResource(id = R.drawable.nopp), // Fallback image if URL fails
              )

              Column(
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.Start,
              ) {
                // Profile name with distance on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      // Profile name
                      Text(
                          text = profile.name,
                          fontSize = NAME_FONT_SIZE,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.weight(1f) // Push distance to the right
                          )

                      // Distance text
                      Text(
                          text = profile.approxDistanceToSelfProfile(),
                          color = MaterialTheme.colorScheme.onSecondary,
                          fontSize = CATCHPHRASE_FONT_SIZE, // Smaller font size
                          fontWeight = FontWeight.Normal)
                    }

                Text(text = "\"${profile.catchPhrase}\"", fontSize = CATCHPHRASE_FONT_SIZE)

                Spacer(modifier = Modifier.padding(TEXT_SPACER_PADDING))

                TagDisplay(tags = profile.tags, isSettings = isSettings)
              }
            }
      }
}
