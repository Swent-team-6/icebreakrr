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

@Composable
fun TopBar() {
  Box(
      contentAlignment = Alignment.Center,
      modifier =
          Modifier.background(Color(0xFF1FAEF0)) // fixme: use icebreakrr blue
              .fillMaxWidth()
              .heightIn(90.dp)
              .testTag("topBar")) {
        Text(
            text = "Around You",
            fontSize = 40.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
      }
}
