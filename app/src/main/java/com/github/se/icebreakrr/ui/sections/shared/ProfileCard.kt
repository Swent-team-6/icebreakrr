package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.getMockedProfiles

@Composable
fun ProfileCard(profile: Profile, onclick: () -> Unit) {
  Card(
      onClick = onclick,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).testTag("profileCard"),
  ) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {

          // todo: replace this with the actual image
          Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(color = Color.Gray, radius = 40.dp.toPx())
          }

          Column(
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.Start,
          ) {
            Text(text = profile.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Text(text = "\"${profile.catchPhrase}\"", fontSize = 16.sp)

            Spacer(modifier = Modifier.padding(6.dp))

            // put the 5 first tags in a string
            val tags = profile.tags.take(5).joinToString(" ") { "#$it" }
            Text(text = tags, fontSize = 16.sp)
          }
        }
  }
}

@Preview(showBackground = true)
@Composable
fun ProfileCardPreview() {
  ProfileCard(Profile.getMockedProfiles()[0], onclick = { TODO() })
}
