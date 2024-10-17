package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.RowOfTags
import com.github.se.icebreakrr.ui.tags.TagStyle

/**
 * Displays the main profile view with the profile header and information section. The profile
 * header shows the profile image, back button, and user name. The information section contains a
 * catchphrase, tags, and description.
 *
 * @param navigationActions Actions to navigate between screens.
 * @param isUser boolean stating if the profile corresponds to the app user.
 */
@Composable
fun ProfileView(navigationActions: NavigationActions) {

  // Predefined user for UI testing
  var name = "John Do"
  var catchphrase = "This is my catchphrase, there are many like it but this one is mine!"
  var description =
      """
                Hey there! I'm John Do, a bit of a tech geek who loves exploring all things digital. 
                When I'm not deep into coding or working on new apps, I'm usually out cycling, discovering new trails, 
                or whipping up something fun in the kitchen. I'm also a huge sci-fi fan—if you love talking about future 
                worlds or cool ideas, we'll get along great. I'm always up for a good laugh, sharing ideas, and meeting new people. 
                Let's connect, hang out, or chat about anything cool!
            """
          .trimIndent()
  val userTags = remember {
    listOf(
        Pair("salsa", Color.Red),
        Pair("pesto", Color.Green),
        Pair("psto", Color.Blue),
        Pair("pest", Color.Green),
        Pair("peest", Color.Green))
  }

  Scaffold(
      modifier = Modifier.testTag("profileScreen"),
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally) {

              // 2 sections one for the profile image with overlay and
              // one for the information section
              ProfileHeader(navigationActions)
              InfoSection(
                  catchPhrase = catchphrase,
                  listOfTags = userTags,
                  description =
                      """
                Hey there! I'm John Do, a bit of a tech geek who loves exploring all things digital. 
                When I'm not deep into coding or working on new apps, I'm usually out cycling, discovering new trails, 
                or whipping up something fun in the kitchen. I'm also a huge sci-fi fan—if you love talking about future 
                worlds or cool ideas, we'll get along great. I'm always up for a good laugh, sharing ideas, and meeting new people. 
                Let's connect, hang out, or chat about anything cool!
                """
                          .trimIndent())
            }
      })
}

/**
 * Displays the user's information section with a catchphrase, tags, and description. The content is
 * scrollable if it exceeds the available screen space.
 *
 * @param catchPhrase The user's catchphrase.
 * @param listOfTags A list of tags related to the user, paired with colors.
 * @param description The user's detailed description.
 */
@Composable
fun InfoSection(catchPhrase: String, listOfTags: List<Pair<String, Color>>, description: String) {

  val scrollState = rememberScrollState()

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(16.dp)
              .verticalScroll(scrollState)
              .testTag("infoSection")) {
        // Catchphrase Section
        Spacer(modifier = Modifier.height(12.dp))
        ProfileCatchPhrase(catchPhrase)

        // Tags Section
        Spacer(modifier = Modifier.height(8.dp))
        TagsSection(listOfTags)

        // Description Section
        Spacer(modifier = Modifier.height(16.dp))
        ProfileDescription(description)
      }
}

/**
 * Displays the profile header section with a profile image, a back button, and an edit button. It
 * also shows the username in an overlay at the bottom of the profile image.
 *
 * @param navigationActions Actions to navigate between screens.
 */
@Composable
fun ProfileHeader(navigationActions: NavigationActions) {
  Box(
      modifier =
          Modifier.fillMaxWidth() // Make the image take full width
              .aspectRatio(1f) // Keep the aspect ratio 1:1 (height == width) => a square
              .background(Color.LightGray)
              .testTag("profileHeader")) {
        // Profile image
        Image(
            painter = painterResource(id = R.drawable.turtle),
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().testTag("profilePicture"))

        // Back button
        IconButton(
            onClick = { navigationActions.goBack() },
            modifier =
                Modifier.align(Alignment.TopStart) // Align to the top start (top-left corner)
                    .padding(16.dp)
                    .testTag("goBackButton")) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Go Back",
                  tint = Color.White)
            }

        // Overlay with username and edit button
        Row(
            modifier =
                Modifier.align(Alignment.BottomStart) // Aligns this content to the bottom left
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

              // Username
              Text(
                  text = "Jean Do",
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.testTag("username"))

              // Edit Button
              IconButton(
                  onClick = { navigationActions.navigateTo("Profile Edit Screen") },
                  modifier =
                      Modifier.background(Color.White, CircleShape)
                          .padding(4.dp)
                          .testTag("editButton")) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Edit Profile",
                        tint = Color.Gray)
                  }
            }
      }
}

/**
 * Displays the user's catchphrase in a medium-sized font with a maximum of two lines.
 *
 * @param catchPhrase The user's catchphrase.
 */
@Composable
fun ProfileCatchPhrase(catchPhrase: String) {
  Text(
      text = catchPhrase,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black.copy(alpha = 0.8f),
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
      textAlign = TextAlign.Left,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis)
}

/**
 * Displays a section with a row of tags, each associated with a color.
 *
 * @param listOfTags A list of tags, each paired with a color.
 */
@Composable
fun TagsSection(listOfTags: List<Pair<String, Color>>) {
  Box(modifier = Modifier.fillMaxWidth().height(80.dp).testTag("tagSection")) {
    RowOfTags(listOfTags, TagStyle())
  }
}

/**
 * Displays the user's profile description in a medium-sized font, aligned to the start.
 *
 * @param description The user's description or bio.
 */
@Composable
fun ProfileDescription(description: String) {
  Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Black.copy(alpha = 0.9f),
      fontSize = 14.sp,
      textAlign = TextAlign.Start,
      modifier = Modifier.padding(8.dp).testTag("profileDescription"))
}
