package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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

// Define constants for font sizes and colors
private val TOP_BAR_TEXT_SIZE = 40.sp
private val TOP_BAR_TEXT_COLOR = Color.White
private val TOP_BAR_BACKGROUND_COLOR = IceBreakrrBlue

/**
 * Displays the TopBar of each screen, used to show to the user in which screen he is
 *
 * @param s the string to be displayed in the TopBar
 */
@Composable
fun TopBar(s: String) {
  Box(
      contentAlignment = Alignment.Center,
      modifier =
          Modifier.background(TOP_BAR_BACKGROUND_COLOR)
              .fillMaxWidth()
              .heightIn(TOP_BAR_HEIGHT)
              .testTag("topBar")) {
        Text(
            text = s,
            fontSize = TOP_BAR_TEXT_SIZE,
            color = TOP_BAR_TEXT_COLOR,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
      }
}
