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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Profile

@Composable
fun ProfileCard(
    profile: Profile,
    isSettings: Boolean = false,
    greyedOut: Boolean = false,
    onclick: () -> Unit
) {
  Card(
      onClick = onclick,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).testTag("profileCard"),
  ) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
          AsyncImage(
              model = profile.profilePictureUrl,
              contentDescription = "profile picture",
              modifier = Modifier.size(80.dp).clip(CircleShape),
              placeholder = painterResource(id = R.drawable.nopp), // Default image during loading
              error = painterResource(id = R.drawable.nopp), // Fallback image if URL fails
          )

          Column(
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.Start,
          ) {
            val textColor = if (greyedOut) Color(0x88888888) else Color.Unspecified
            Text(
                text = profile.name,
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold)

            Text(text = "\"${profile.catchPhrase}\"", color = textColor, fontSize = 16.sp)

            Spacer(modifier = Modifier.padding(6.dp))

            if (!isSettings) {
              // put the 5 first tags in a string
              val tags = profile.tags.take(5).joinToString(" ") { "#$it" }
              Text(text = tags, fontSize = 16.sp, color = textColor)
            } else {
              Text(text = "Tap to preview profile")
            }
          }
        }
  }
}

@Preview(showBackground = true)
@Composable
fun ProfileCardPreview() {
  ProfileCard(Profile.getMockedProfiles()[0], onclick = { TODO() })
}
