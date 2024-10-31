package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.icebreakrr.ui.message.SendRequestScreen
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.shared.InfoSection
import com.github.se.icebreakrr.ui.sections.shared.ProfileHeader

/**
 * In Around You, when you click on a profile, this is the composable used to display it
 *
 * @param navigationActions Actions to navigate between screens.
 */
@Composable
fun OtherProfileView(navigationActions: NavigationActions) {
  var sendRequest by remember { mutableStateOf(false) }
  var writtenMessage by remember { mutableStateOf("") }

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

  Box(modifier = Modifier.fillMaxSize().testTag("aroundYouProfileScreen")) {
    Scaffold(
        modifier = Modifier.testTag("profileScreen"),
        content = { paddingValues ->
          Column(
              modifier = Modifier.fillMaxWidth().padding(paddingValues),
              horizontalAlignment = Alignment.CenterHorizontally) {

                // 2 sections one for the profile image with overlay and
                // one for the information section
                ProfileHeader(navigationActions, false) { sendRequest = true }
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
    // this displays the request messaging system
    if (sendRequest) {
      Box(
          modifier =
              Modifier.fillMaxSize()
                  .background(Color.Black.copy(alpha = 0.5f))
                  .clickable {}
                  .testTag("bluredBackground"),
          contentAlignment = Alignment.Center) {
            SendRequestScreen(
                onValueChange = { writtenMessage = it },
                value = writtenMessage,
                onSendClick = { sendRequest = false },
                onCancelClick = { sendRequest = false })
          }
    }
  }
}
