package com.github.se.icebreakrr.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WIDTH = 324.dp
private val HEIGHT_MINOR = 125.dp
private val PADDING_MAJOR = 0.dp
private val PADDING_MINOR = 24.dp
private val BOX_SHAPE = RoundedCornerShape(size = 28.dp)
private val SPACING = 12.dp
private val BOTTOM_PADDING = 8.dp
private val FONT_WEIGHT = FontWeight(400)
private val FONTSIZE_1 = 24.sp
private val FONTSIZE_2 = 14.sp
private val LINE_HEIGHT_1 = 32.sp
private val LINE_HEIGHT_2 = 20.sp
private val LETTER_SPACING = 0.25.sp
private val TEXT_FIELD_HEIGHT = 58.dp
private val TEXT_FIELD_WIDTH = 292.dp
private val ROW_HEIGHT = 49.dp
private val ROW_WIDTH = 312.dp

@Composable
fun SendRequestScreen(
    onValueChange: (String) -> Unit,
    value: String,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit
) {
  Box(
      modifier =
          Modifier.padding(PADDING_MAJOR)
              .width(WIDTH)
              .background(color = MaterialTheme.colorScheme.surface, shape = BOX_SHAPE)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SPACING, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
              modifier =
                  Modifier.width(WIDTH)
                      .height(HEIGHT_MINOR)
                      .padding(
                          start = PADDING_MINOR,
                          top = PADDING_MINOR,
                          end = PADDING_MINOR,
                          bottom = BOTTOM_PADDING)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(PADDING_MAJOR, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                  Text(
                      text = "Meet this person",
                      style =
                          TextStyle(
                              fontSize = FONTSIZE_1,
                              lineHeight = LINE_HEIGHT_1,
                              fontWeight = FONT_WEIGHT,
                              color = MaterialTheme.colorScheme.onSurface,
                          ))
                  Text(
                      text =
                          "You can ask this person to meet you in person, if they accept they will send you their location",

                      // M3/body/medium
                      style =
                          TextStyle(
                              fontSize = FONTSIZE_2,
                              lineHeight = LINE_HEIGHT_2,
                              fontWeight = FONT_WEIGHT,
                              color = MaterialTheme.colorScheme.onSurface,
                              letterSpacing = LETTER_SPACING,
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
                  Modifier.width(TEXT_FIELD_WIDTH)
                      .height(TEXT_FIELD_HEIGHT * 2)
                      .testTag("messageTextField"),
          )
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.width(ROW_WIDTH).height(ROW_HEIGHT)) {
                TextButton(
                    onClick = { onCancelClick() },
                    enabled = true,
                    modifier = Modifier.padding(start = BOTTOM_PADDING).testTag("cancelButton")) {
                      Text("Cancel")
                    }
              }
          Spacer(Modifier)
        }
      }
}
