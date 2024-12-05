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
private val TOP_BAR_HEIGHT = 90.dp
private val BACK_BUTTON_PADDING_START = 8.dp
private val BACK_BUTTON_SIZE = 48.dp
private val BACK_BUTTON_ICON_SIZE = 32.dp
private val TOP_BAR_TEXT_SIZE = 40.sp
private val TITLE_TEXT_WEIGHT = 1f

@Composable
fun TopBar(s: String, needBackButton: Boolean = false, backButtonOnClick: () -> Unit = {}) {
  Box(
      contentAlignment = Alignment.Center,
      modifier =
          Modifier.background(MaterialTheme.colorScheme.primary)
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
                      tint = MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.size(BACK_BUTTON_ICON_SIZE))
                }
          }
          Text(
              text = s,
              fontSize = TOP_BAR_TEXT_SIZE,
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
