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
 * Screen that displays a list of users that the current user has already met. This screen is
 * accessible from the settings screen via "People You Already Met" button
 *
 * Currently implements only UI components and navigation. Backend integration for storing and
 * retrieving already met profiles will be implemented later.
 *
 * @param navigationActions Navigation handler for screen transitions
 * @param profilesViewModel ViewModel handling profile data operations
 */
@Composable
fun AlreadyMetScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    isTestMode: Boolean = false
) {
  val alreadyMetProfiles = profilesViewModel.profiles.collectAsState()
  val isLoading = profilesViewModel.loading.collectAsState()
  val isConnected = profilesViewModel.isConnected.collectAsState()
  val context = LocalContext.current

  var profileToUnmeet by remember { mutableStateOf<Profile?>(null) }

  ProfileListScreen(
      navigationActions = navigationActions,
      profilesViewModel = profilesViewModel,
      title = stringResource(R.string.already_met),
      emptyMessage = stringResource(R.string.no_already_met_users),
      onProfileClick = { profile ->
        if (isNetworkAvailableWithContext(context)) {
          profileToUnmeet = profile
        } else {
          showNoInternetToast(context)
        }
      },
      profiles = alreadyMetProfiles,
      isLoading = isLoading,
      isConnected = isConnected,
      onRefresh = { profilesViewModel.getAlreadyMetUsers() },
      showConfirmDialog = profileToUnmeet != null,
      confirmDialogTitle = stringResource(R.string.unmeet_confirm),
      confirmDialogMessage =
          profileToUnmeet?.let { stringResource(R.string.unmeet_confirm_message, it.name) },
      confirmButtonText = stringResource(R.string.unmeet_yes),
      dismissButtonText = stringResource(R.string.unmeet_no),
      onConfirmAction = {
        profileToUnmeet?.let { profile ->
          profilesViewModel.removeAlreadyMet(profile.uid)
          profilesViewModel.getAlreadyMetUsers()
          profileToUnmeet = null
        }
      },
      selectedProfile = profileToUnmeet,
      periodicRefreshAction = { profilesViewModel.getAlreadyMetUsers() },
      isTestMode = isTestMode)
}
