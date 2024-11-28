package com.github.se.icebreakrr.ui.sections

import ProfileListScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast

/**
 * Composable function for displaying the "Blocked" screen.
 *
 * It includes a bottom navigation bar and displays the main content of the screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 * @param profilesViewModel The view model of the profiles
 */
@Composable
fun UnblockProfileScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    isTestMode: Boolean = false
) {
  val blockedProfiles = profilesViewModel.profiles.collectAsState()
  val isLoading = profilesViewModel.loading.collectAsState()
  val isConnected = profilesViewModel.isConnected.collectAsState()
  val context = LocalContext.current

  var profileToUnblock by remember { mutableStateOf<Profile?>(null) }

  ProfileListScreen(
      navigationActions = navigationActions,
      profilesViewModel = profilesViewModel,
      title = stringResource(R.string.blocked_profiles),
      emptyMessage = stringResource(R.string.no_blocked_users),
      onProfileClick = { profile ->
        if (isNetworkAvailableWithContext(context)) {
          profileToUnblock = profile
        } else {
          showNoInternetToast(context)
        }
      },
      profiles = blockedProfiles,
      isLoading = isLoading,
      isConnected = isConnected,
      onRefresh = { profilesViewModel.getBlockedUsers() },
      additionalRefreshAction = { profilesViewModel.getSelfProfile() },
      showConfirmDialog = profileToUnblock != null,
      confirmDialogTitle = stringResource(R.string.unblock_confirm),
      confirmDialogMessage =
          profileToUnblock?.let { stringResource(R.string.unblock_confirm_message, it.name) },
      confirmButtonText = stringResource(R.string.block_yes),
      dismissButtonText = stringResource(R.string.block_no),
      onConfirmAction = {
        profileToUnblock?.let { profile ->
          profilesViewModel.unblockUser(profile.uid)
          profilesViewModel.getBlockedUsers()
          profileToUnblock = null
        }
      },
      selectedProfile = profileToUnblock,
      periodicRefreshAction = { profilesViewModel.getBlockedUsers() },
      isTestMode = isTestMode)
}
