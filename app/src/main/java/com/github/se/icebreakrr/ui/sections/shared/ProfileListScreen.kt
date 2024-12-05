import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.delay

// This file was written with the help of CursorAI

private val COLUMN_VERTICAL_PADDING = 16.dp
private val COLUMN_HORIZONTAL_PADDING = 8.dp
private val TEXT_SIZE_LARGE = 20.sp
private const val REFRESH_INTERVAL = 10_000L

/**
 * A composable function that displays a list of profiles with various interactive features.
 *
 * @param navigationActions Handler for navigation actions within the app
 * @param profilesViewModel ViewModel managing profile-related data
 * @param title Screen title displayed in the top bar
 * @param emptyMessage Message to display when no profiles are available
 * @param onProfileClickConfirm Callback when a profile is clicked and confirmed
 * @param onProfileClickDismiss Callback when a profile selection is dismissed
 * @param profiles State containing the list of profiles to display
 * @param isLoading State indicating if data is being loaded
 * @param isConnected State indicating network connectivity
 * @param onRefresh Callback for refresh action
 * @param additionalRefreshAction Optional additional action to perform on refresh
 * @param showConfirmDialog Whether to show confirmation dialog
 * @param confirmDialogTitle Title for confirmation dialog
 * @param confirmDialogMessage Message for confirmation dialog
 * @param confirmButtonText Text for confirm button
 * @param dismissButtonText Text for dismiss button
 * @param onConfirmAction Callback for confirmation action
 * @param selectedProfile Currently selected profile
 * @param periodicRefreshAction Optional action for periodic refresh
 * @param isTestMode Flag to indicate if running in test mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileListScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    title: String,
    emptyMessage: String,
    onProfileClickConfirm: (Profile) -> Unit,
    onProfileClickDismiss: (Profile) -> Unit,
    profiles: State<List<Profile>>,
    isLoading: State<Boolean>,
    isConnected: State<Boolean>,
    onRefresh: () -> Unit,
    additionalRefreshAction: (() -> Unit)? = null,
    showConfirmDialog: Boolean = false,
    confirmDialogTitle: String? = null,
    confirmDialogMessage: String? = null,
    confirmButtonText: String? = null,
    dismissButtonText: String? = null,
    onConfirmAction: (() -> Unit)? = null,
    selectedProfile: Profile? = null,
    periodicRefreshAction: (() -> Unit)? = null,
    isTestMode: Boolean = false,
    notificationCount: Int
) {
  LaunchedEffect(isConnected.value) {
    if (!isTestMode && !isNetworkAvailable()) {
      profilesViewModel.updateIsConnected(false)
    } else {
      while (true) {
        periodicRefreshAction?.invoke()
        delay(REFRESH_INTERVAL)
      }
    }
  }

  // Add dialog if confirmation is needed
  if (showConfirmDialog && selectedProfile != null) {
    AlertDialog(
        modifier = Modifier.testTag("confirmDialog"),
        onDismissRequest = { onProfileClickDismiss(selectedProfile) }, // Dismiss dialog
        title = { Text(confirmDialogTitle ?: "") },
        text = { Text(confirmDialogMessage ?: "") },
        confirmButton = {
          TextButton(onClick = { onConfirmAction?.invoke() }) { Text(confirmButtonText ?: "") }
        },
        dismissButton = {
          TextButton(
              onClick = { onProfileClickDismiss(selectedProfile) } // Dismiss dialog
              ) {
                Text(dismissButtonText ?: "")
              }
        })
  }

  Scaffold(
      modifier = Modifier.testTag("profileListScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              if (navigationActions.currentRoute() != Route.SETTINGS &&
                  route.route == Route.SETTINGS) {
                navigationActions.navigateTo(Route.SETTINGS)
              } else {
                navigationActions.navigateTo(route.route)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.UNBLOCK_PROFILE,
            notificationCount = notificationCount)
      },
      topBar = { TopBar(title, true) { navigationActions.goBack() } },
      content = { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading.value,
            onRefresh = {
              onRefresh()
              additionalRefreshAction?.invoke()
            },
            modifier = Modifier.padding(innerPadding)) {
              Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = COLUMN_VERTICAL_PADDING),
                    verticalArrangement = Arrangement.spacedBy(COLUMN_VERTICAL_PADDING),
                    modifier =
                        Modifier.fillMaxSize().padding(horizontal = COLUMN_HORIZONTAL_PADDING)) {
                      if (profiles.value.isNotEmpty()) {
                        items(profiles.value.size) { index: Int ->
                          ProfileCard(
                              profile = profiles.value[index],
                              onclick = { onProfileClickConfirm(profiles.value[index]) })
                        }
                      } else {
                        item {
                          Box(
                              contentAlignment = Alignment.Center,
                              modifier = Modifier.fillMaxSize().testTag("emptyProfilePrompt")) {
                                Text(
                                    text = emptyMessage,
                                    fontSize = TEXT_SIZE_LARGE,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                              }
                        }
                      }
                    }
              }
            }
      })
}

/**
 * A composable container with pull-to-refresh functionality, displaying a refresh indicator while
 * refreshing.
 *
 * @param isRefreshing Whether the refresh operation is ongoing; the indicator shows while true.
 * @param onRefresh Called on pull-to-refresh with parameters for filtering:
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
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable (BoxScope.() -> Unit) = {
      Indicator(
          modifier = Modifier.align(Alignment.TopCenter).testTag("refreshIndicator"),
          isRefreshing = isRefreshing,
          state = state)
    },
    content: @Composable (BoxScope.() -> Unit)
) {

  Box(
      modifier.pullToRefresh(state = state, isRefreshing = isRefreshing) { onRefresh() },
      contentAlignment = contentAlignment) {
        content()
        indicator()
      }
}
