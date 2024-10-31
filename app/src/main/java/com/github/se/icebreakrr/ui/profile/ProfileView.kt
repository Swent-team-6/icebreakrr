package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.shared.InfoSection
import com.github.se.icebreakrr.ui.sections.shared.ProfileHeader

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

  Scaffold(modifier = Modifier.testTag("profileScreen")) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxWidth().padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally) {

          // 2 sections one for the profile image with overlay and
          // one for the information section
          ProfileHeader(navigationActions, true) {
            navigationActions.navigateTo("Profile Edit Screen")
          }
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
  }
}
