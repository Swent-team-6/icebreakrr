package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define constants for layout dimensions
private val TOP_BAR_HEIGHT_1LINE = 45.dp
private val BACK_BUTTON_PADDING_START = 8.dp
private val BACK_BUTTON_SIZE = 45.dp
private val BACK_BUTTON_ICON_SIZE = 30.dp

// Define constants for font sizes and colors
private val TOP_BAR_TEXT_SIZE_BIG = 16.sp
private val TOP_BAR_TEXT_SIZE_SMALL = 13.sp

private val TITLE_TEXT_WEIGHT = 1f
private const val MAX_CHAR_ONE_LINE = 15
private val TOP_BAR_HEIGHT_2LINES = 60.dp

@Composable
fun TopBar(s: String, needBackButton: Boolean = false, backButtonOnClick: () -> Unit = {}) {
  Box(
      contentAlignment = Alignment.Center,
      modifier =
          Modifier.background(MaterialTheme.colorScheme.primary)
              .fillMaxWidth()
              .heightIn(
                  if (s.length < MAX_CHAR_ONE_LINE) TOP_BAR_HEIGHT_1LINE else TOP_BAR_HEIGHT_2LINES)
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
                      tint = MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.size(BACK_BUTTON_ICON_SIZE))
                }
          }
          val topBarSize =
              if (s.length < MAX_CHAR_ONE_LINE) TOP_BAR_TEXT_SIZE_BIG else TOP_BAR_TEXT_SIZE_SMALL
          Text(
              text = s,
              fontSize = topBarSize,
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(TITLE_TEXT_WEIGHT))

          if (needBackButton) {
            Spacer(modifier = Modifier.width(BACK_BUTTON_SIZE))
          }
        }
      }
}
