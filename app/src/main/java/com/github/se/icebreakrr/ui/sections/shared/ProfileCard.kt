package com.github.se.icebreakrr.ui.sections.shared

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.ui.theme.grayedOut

// Define constants for layout dimensions and other configurations
private val CARD_CORNER_RADIUS = 14.dp
private val CARD_MAX_HEIGHT = 150.dp
private val CARD_PADDING_HORIZONTAL = 10.dp
private val CARD_PADDING_VERTICAL = 6.dp
private val IMAGE_SIZE = 80.dp
private val IMAGE_SPACING = 18.dp
private val TEXT_SPACER_PADDING = 6.dp

// Define constants for font sizes
private val NAME_FONT_SIZE = 24.sp
private val CATCHPHRASE_FONT_SIZE = 16.sp
private val TAGS_FONT_SIZE = 16.sp

// Define colors used
private val GREYED_OUT_COLOR = grayedOut

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
      modifier = Modifier.fillMaxWidth().heightIn(max = CARD_MAX_HEIGHT).testTag("profileCard")) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(IMAGE_SPACING),
            verticalAlignment = Alignment.CenterVertically,
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
                val textColor = if (greyedOut) GREYED_OUT_COLOR else Color.Unspecified
                Text(
                    text = profile.name,
                    color = textColor,
                    fontSize = NAME_FONT_SIZE,
                    fontWeight = FontWeight.Bold)

                Text(
                    text = "\"${profile.catchPhrase}\"",
                    color = textColor,
                    fontSize = CATCHPHRASE_FONT_SIZE)

                Spacer(modifier = Modifier.padding(TEXT_SPACER_PADDING))

                if (!isSettings) {
                  // Display the first 5 tags in a string format
                  val tags = profile.tags.take(5).joinToString(" ") { "#$it" }
                  Text(text = tags, fontSize = TAGS_FONT_SIZE, color = textColor)
                } else {
                  Text(text = "Tap to preview profile")
                }
              }
            }
      }
}
