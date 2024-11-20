package com.github.se.icebreakrr.ui.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast

private val COLUMN_VERTICAL_PADDING = 16.dp
private val COLUMN_HORIZONTAL_PADDING = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlreadyMetScreen(navigationActions: NavigationActions, profilesViewModel: ProfilesViewModel) {
  val cardList = profilesViewModel.profiles.collectAsState()
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("alreadyMetScreen"),
      topBar = { TopBar("Already Met", true) { navigationActions.goBack() } },
      content = { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          Text(
              "Already Met Screen - To be implemented",
              modifier = Modifier.align(Alignment.CenterHorizontally))

          LazyColumn(
              contentPadding = PaddingValues(vertical = COLUMN_VERTICAL_PADDING),
              verticalArrangement = Arrangement.spacedBy(COLUMN_VERTICAL_PADDING),
              modifier = Modifier.fillMaxSize().padding(horizontal = COLUMN_HORIZONTAL_PADDING)) {
                items(cardList.value.size) { index ->
                  ProfileCard(
                      profile = cardList.value[index],
                      onclick = {
                        if (isNetworkAvailableWithContext(context)) {
                          navigationActions.navigateTo(
                              Screen.OTHER_PROFILE_VIEW + "?userId=${cardList.value[index].uid}")
                        } else {
                          showNoInternetToast(context)
                        }
                      })
                }
              }
        }
      })
}
