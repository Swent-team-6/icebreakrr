package com.github.se.icebreakrr.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SendRequestScreen(
    onValueChange: (String) -> Unit,
    value: String,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit
) {
  Box(
      modifier =
          Modifier.padding(0.dp)
              .width(312.dp)
              .height(254.dp)
              .background(color = Color(0xFFF9F9F9), shape = RoundedCornerShape(size = 28.dp))) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
              modifier =
                  Modifier.width(312.dp)
                      .height(125.dp)
                      .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 9.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                  Text(
                      text = "Meet this person",
                      style =
                          TextStyle(
                              fontSize = 24.sp,
                              lineHeight = 32.sp,
                              fontWeight = FontWeight(400),
                              color = Color(0xFF1D1B20),
                          ))
                  Text(
                      text =
                          "You can ask this person to meet you in person, if they accept they will send you their location",

                      // M3/body/medium
                      style =
                          TextStyle(
                              fontSize = 14.sp,
                              lineHeight = 20.sp,
                              fontWeight = FontWeight(400),
                              color = Color(0xFF49454F),
                              letterSpacing = 0.25.sp,
                          ))
                }
              }
          OutlinedTextField(
              value = value,
              onValueChange = { onValueChange(it) },
              label = { Text("Your message") },
              trailingIcon = {
                IconButton(onClick = { onSendClick() }, modifier = Modifier.testTag("sendButton")) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Default.Send,
                      contentDescription = "send button")
                }
              },
              modifier =
                  Modifier.padding(0.dp).width(292.dp).height(56.dp).testTag("messageTextField"))
          Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.width(312.dp).height(49.dp)) {
                TextButton(
                    onClick = { onCancelClick() },
                    enabled = true,
                    modifier =
                        Modifier.padding(start = 8.dp, end = 24.dp).testTag("cancelButton")) {
                      Text("Cancel")
                    }
              }
        }
      }
}
