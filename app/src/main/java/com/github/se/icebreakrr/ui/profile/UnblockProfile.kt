package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.sections.IcebreakrrBlue
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.theme.messageTextColor
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast
import kotlinx.coroutines.delay

// Constants for layout dimensions
private val COLUMN_VERTICAL_PADDING = 16.dp
private val COLUMN_HORIZONTAL_PADDING = 8.dp
private val TEXT_SIZE_LARGE = 20.sp
private val NO_CONNECTION_TEXT_COLOR = messageTextColor
private val EMPTY_PROFILE_TEXT_COLOR = messageTextColor

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable

/**
 * Composable function for displaying the "Around You" screen.
 *
 * It includes a bottom navigation bar and displays the main content of the screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 * @param profilesViewModel The view model of the profiles
 */
fun UnblockProfileScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    isTestMode: Boolean = false
) {

  val blockedProfiles = profilesViewModel.profiles.collectAsState()
  val isLoading = profilesViewModel.loading.collectAsState()
  val context = LocalContext.current
  val isConnected = profilesViewModel.isConnected.collectAsState()

  var profileToUnblock by remember { mutableStateOf<Profile?>(null) }

  if (profileToUnblock != null) {
      AlertDialog(
          modifier = Modifier.testTag("unblockDialog"),
          onDismissRequest = { profileToUnblock = null },
          title = { Text("Confirm Unblock") },
          text = { Text("Are you sure you want to unblock ${profileToUnblock!!.name}?") },
          confirmButton = {
              TextButton(onClick = {
                  profilesViewModel.unblockUser(profileToUnblock!!.uid)
                  profilesViewModel.getBlockedUsers()
                  profileToUnblock = null
              }) {
                  Text("Yes")
              }
          },
          dismissButton = {
              TextButton(onClick = { profileToUnblock = null }) {
                  Text("No")
              }
          }
      )
  }

  // Initial check and start of periodic update every 10 seconds
  LaunchedEffect(isConnected.value) {
    if (!isTestMode && !isNetworkAvailable()) {
      profilesViewModel.updateIsConnected(false)
    } else {
      while (true) {
        // Call the profile fetch function
        profilesViewModel.getBlockedUsers()

        // Wait 10 seconds before the next update
        delay(10_000L)
      }
    }
  }

  Scaffold(
      modifier = Modifier.testTag("unblockScreen"),
      bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { route ->
                  if (navigationActions.currentRoute() != Route.SETTINGS && route.route == Route.SETTINGS) {
                      navigationActions.navigateTo(Route.SETTINGS)
                  }
                  else {
                      navigationActions.navigateTo(route.route)
                  }
              },
              tabList = LIST_TOP_LEVEL_DESTINATIONS,
              selectedItem = Route.UNBLOCK_PROFILE)
      },
      topBar = {
          TopAppBar(
              modifier = Modifier.testTag("unblockTopBar"),
              title = { Text("Blocked Users",color= Color.White, modifier = Modifier.testTag("UnblockTopBarTitle")) },
              navigationIcon = {
                  IconButton(
                      modifier = Modifier.testTag("goBackButton"),
                      onClick = {
                          navigationActions.goBack()
                      }) {
                      Icon(
                          Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back",
                          tint = Color.White)
                    }
              },
              colors = TopAppBarDefaults.topAppBarColors(containerColor = IcebreakrrBlue))
      },
      content = { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading.value,
            onRefresh = {profilesViewModel.getSelfProfile(); profilesViewModel.getBlockedUsers()},
            modifier = Modifier.padding(innerPadding)
        ) {
              LazyColumn(
                  contentPadding = PaddingValues(vertical = COLUMN_VERTICAL_PADDING),
                  verticalArrangement = Arrangement.spacedBy(COLUMN_VERTICAL_PADDING),
                  modifier =
                      Modifier.fillMaxSize().padding(horizontal = COLUMN_HORIZONTAL_PADDING)) {
                    if (!isConnected.value) {
                      item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().testTag("noConnectionPrompt")) {
                              Text(
                                  text = "No Internet Connection",
                                  fontSize = TEXT_SIZE_LARGE,
                                  fontWeight = FontWeight.Bold,
                                  color = NO_CONNECTION_TEXT_COLOR)
                            }
                      }
                    } else if (blockedProfiles.value.isNotEmpty()) {
                      items(blockedProfiles.value.size) { index ->
                        ProfileCard(
                            profile = blockedProfiles.value[index],
                            onclick = {
                              if (isNetworkAvailableWithContext(context)) {
                                profileToUnblock = blockedProfiles.value[index]
                              } else {
                                showNoInternetToast(context)
                              }
                            })
                      }
                    } else {
                      item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().testTag("emptyProfilePrompt")) {
                              Text(
                                  text = stringResource(id = R.string.no_blocked_users),
                                  fontSize = TEXT_SIZE_LARGE,
                                  fontWeight = FontWeight.Bold,
                                  color = EMPTY_PROFILE_TEXT_COLOR)
                            }
                      }
                    }
                  }
            }
      }
  )
}

/**
 * A composable container with pull-to-refresh functionality, displaying a refresh indicator while
 * refreshing.
 *
 * @param isRefreshing Whether the refresh operation is ongoing; the indicator shows while true.
 * @param onRefresh Called on pull-to-refresh with parameters for filtering:
 *
 * @param modifier [Modifier] for styling the container.
 * @param state State for managing pull-to-refresh gesture.
 * @param contentAlignment Alignment for the content within the box.
 * @param indicator Refresh indicator composable, centered by default.
 * @param content The main content to display inside the container.
 */
@Composable
@ExperimentalMaterial3Api
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh:
        () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable (BoxScope.() -> Unit) = {
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter).testTag("refreshIndicator"),
            isRefreshing = isRefreshing,
            state = state
        )
    },
    content: @Composable (BoxScope.() -> Unit)
) {

  Box(
      modifier.pullToRefresh(state = state, isRefreshing = isRefreshing) {
        onRefresh()
      },
      contentAlignment = contentAlignment) {
        content()
        indicator()
      }
}

