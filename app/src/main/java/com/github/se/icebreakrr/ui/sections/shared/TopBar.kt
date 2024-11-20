package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue

// Define constants for layout dimensions
private val TOP_BAR_HEIGHT = 90.dp
private val BACK_BUTTON_PADDING_START = 8.dp
private val BACK_BUTTON_SIZE = 48.dp
private val BACK_BUTTON_ICON_SIZE = 32.dp

// Define constants for font sizes and colors
private val TOP_BAR_TEXT_SIZE = 40.sp
private val TOP_BAR_TEXT_COLOR = Color.White
private val TOP_BAR_BACKGROUND_COLOR = IceBreakrrBlue

/**
 * Displays the TopBar of each screen, used to show to the user in which screen he is
 *
 * @param s the string to be displayed in the TopBar
 * @param needBackButton boolean on whether we want a topBar with or without a back button
 * @param backButtonOnClick what goes in the onClick of the back button
 */
@Composable
fun TopBar(s: String, needBackButton: Boolean = false, backButtonOnClick: () -> Unit = {}) {
  Box(
      contentAlignment = Alignment.Center,
      modifier =
          Modifier.background(TOP_BAR_BACKGROUND_COLOR)
              .fillMaxWidth()
              .heightIn(TOP_BAR_HEIGHT)
              .testTag("topBar")) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          if (needBackButton) {
            IconButton(
                onClick = backButtonOnClick,
                modifier =
                    Modifier.padding(start = BACK_BUTTON_PADDING_START)
                        .size(BACK_BUTTON_SIZE)
                        .testTag("goBackButton")) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Go back",
                      tint = Color.White,
                      modifier = Modifier.size(BACK_BUTTON_ICON_SIZE))
                }
          }
          Text(
              text = s,
              fontSize = TOP_BAR_TEXT_SIZE,
              color = TOP_BAR_TEXT_COLOR,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(1f))

          if (needBackButton) {
            Spacer(modifier = Modifier.width(BACK_BUTTON_SIZE))
          }
        }
      }
}
