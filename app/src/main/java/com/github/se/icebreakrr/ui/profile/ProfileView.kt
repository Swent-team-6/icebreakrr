package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.InfoSection
import com.github.se.icebreakrr.ui.sections.shared.ProfileHeader
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/**
 * Displays the main profile view with the profile header and information section. The profile
 * header shows the profile image, back button, and user name. The information section contains a
 * catchphrase, tags, and description.
 *
 * @param navigationActions Actions to navigate between screens.
 * @param isUser boolean stating if the profile corresponds to the app user.
 */
@Composable
fun ProfileView(
    profilesViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel,
    navigationActions: NavigationActions,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

  // Launch a coroutine to fetch the profile when this composable is first displayed
  LaunchedEffect(Unit) {
    auth.currentUser?.let { profilesViewModel.getProfileByUid(it.uid) }
  }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val profile = profilesViewModel.selectedProfile.collectAsState().value

  Scaffold(
      modifier = Modifier.testTag("profileScreen"),
      content = { paddingValues ->
        if (isLoading) {
          // Show a loading indicator or message while the profile is being fetched
          Box(
              modifier = Modifier.fillMaxSize().background(Color.LightGray).testTag("loadingBox"),
              contentAlignment = Alignment.Center) {
                Text("Loading profile...", textAlign = TextAlign.Center)
              }
        } else if (profile != null) {

          Column(
              modifier = Modifier.fillMaxWidth().padding(paddingValues),
              horizontalAlignment = Alignment.CenterHorizontally) {

                // 2 sections one for the profile image with overlay and
                // one for the information section
                ProfileHeader(profile, navigationActions, true) {
                  navigationActions.navigateTo(Screen.PROFILE_EDIT)
                }
                InfoSection(profile, tagsViewModel)
              }
        }
      })
}
