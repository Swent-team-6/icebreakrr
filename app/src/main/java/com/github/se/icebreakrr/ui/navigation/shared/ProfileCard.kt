package com.github.se.icebreakrr.ui.navigation.shared

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.R

// todo, dear reviewers, if you see this it means i'm dumb, comment it.
// todo: place this file in right directory

@Composable
fun ProfileCard(
    name1: String,
    name2: String,
    body: String,
    tagList: List<String>,
    imagePainter: Painter,
    onClick: () -> Unit
    )
{
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        {
            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture",
            )

            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,

            )
            {
                Text(text = "$name1 $name2", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Text(text = "\"$body\"", fontSize = 16.sp)

                Spacer(modifier = Modifier.padding(6.dp))

                //put the 5 first tags in a string
                val tags = tagList.take(7).joinToString(" ") { "#$it" }
                Text(text = tags, fontSize = 16.sp)
            }
        }


    }


}
@Preview(showBackground = true)
@Composable
fun ProfileCardPreview() {
    ProfileCard(
        name1 = "John",
        name2 = "Doe",
        body = "This is my catchphrase, there are many like it but this one is mine!",
        tagList = listOf("tag1", "tag2", "tag3", "tag4", "tag5", "tagusdickus"),
        imagePainter = painterResource(id = R.drawable.person_90),
        onClick = { Log.d("ProfileCard", "Clicked")}
    )
}