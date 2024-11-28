package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.FilterFloatingActionButton
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.ui.theme.messageTextColor
import com.github.se.icebreakrr.utils.IPermissionManager
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.delay

// Constants for layout dimensions
private val COLUMN_VERTICAL_PADDING = 16.dp
private val COLUMN_HORIZONTAL_PADDING = 8.dp
private val TEXT_SIZE_LARGE = 20.sp
private val NO_CONNECTION_TEXT_COLOR = messageTextColor
private val EMPTY_PROFILE_TEXT_COLOR = messageTextColor
private const val REFRESH_DELAY = 10_000L

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
 * @param tagsViewModel The view model of the tags
 * @param filterViewModel The view model of the filters
 */
fun AroundYouScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel,
    filterViewModel: FilterViewModel,
    locationViewModel: LocationViewModel,
    isTestMode: Boolean = false,
    permissionManager: IPermissionManager,
    appDataStore: AppDataStore
) {

  val permissionStatuses = permissionManager.permissionStatuses.collectAsState()
  val hasLocationPermission =
      permissionStatuses.value[android.Manifest.permission.ACCESS_FINE_LOCATION] ==
          PackageManager.PERMISSION_GRANTED
  val filteredProfiles = profilesViewModel.filteredProfiles.collectAsState()
  val isLoading = profilesViewModel.loading.collectAsState()
  val context = LocalContext.current
  val isConnected = profilesViewModel.isConnected.collectAsState()
  val userLocation = locationViewModel.lastKnownLocation.collectAsState()
  // Collect the discoverability state from DataStore
  val isDiscoverable by appDataStore.isDiscoverable.collectAsState(initial = false)

  // Initial check and start of periodic update every 10 seconds
  LaunchedEffect(isConnected.value, userLocation.value) {
    if (!isTestMode && !isNetworkAvailable()) {
      profilesViewModel.updateIsConnected(false)
    } else if (hasLocationPermission) {
      while (true) {

        // Call the profile fetch function
        profilesViewModel.getFilteredProfilesInRadius(
            userLocation.value ?: GeoPoint(DEFAULT_USER_LATITUDE, DEFAULT_USER_LONGITUDE),
            filterViewModel.selectedRadius.value,
            filterViewModel.selectedGenders.value,
            filterViewModel.ageRange.value,
            tagsViewModel.filteredTags.value)

        delay(REFRESH_DELAY) // Wait before the next update
      }
    }
  }

  Scaffold(
      modifier = Modifier.testTag("aroundYouScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              if (route.route != Route.AROUND_YOU) {
                navigationActions.navigateTo(route)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.AROUND_YOU)
      },
      topBar = { TopBar("Around You") },
      content = { innerPadding ->
        PullToRefreshBox(
            locationViewModel = locationViewModel,
            filterViewModel = filterViewModel,
            tagsViewModel = tagsViewModel,
            isRefreshing = isLoading.value,
            onRefresh = { center, radiusInMeters, genders, ageRange, tags ->
              profilesViewModel.getFilteredProfilesInRadius(
                  center, radiusInMeters, genders, ageRange, tags)
            },
            modifier = Modifier.padding(innerPadding),
            content = {
              if (!isConnected.value) {
                EmptyProfilePrompt(label = "No Internet Connection", testTag = "noConnectionPrompt")
              } else if (!isDiscoverable) {
                EmptyProfilePrompt(
                    label = stringResource(R.string.ask_to_toggle_location),
                    testTag = "activateLocationPrompt")
              } else if (!isTestMode && !hasLocationPermission) {
                EmptyProfilePrompt(
                    label = stringResource(R.string.ask_to_give_location_permission),
                    testTag = "noLocationPermissionPrompt")
              } else if (filteredProfiles.value.isEmpty()) {
                EmptyProfilePrompt(
                    label = stringResource(R.string.no_one_around), testTag = "emptyProfilePrompt")
              } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = COLUMN_VERTICAL_PADDING),
                    verticalArrangement = Arrangement.spacedBy(COLUMN_VERTICAL_PADDING),
                    modifier =
                        Modifier.fillMaxSize().padding(horizontal = COLUMN_HORIZONTAL_PADDING)) {
                      items(filteredProfiles.value.size) { index ->
                        ProfileCard(
                            profile = filteredProfiles.value[index],
                            onclick = {
                              if (isNetworkAvailableWithContext(context)) {
                                navigationActions.navigateTo(
                                    Screen.OTHER_PROFILE_VIEW +
                                        "?userId=${filteredProfiles.value[index].uid}")
                              } else {
                                showNoInternetToast(context)
                              }
                            })
                      }
                    }
              }
            })
      },
      floatingActionButton = { FilterFloatingActionButton(navigationActions) })
}

/**
 * A composable container with pull-to-refresh functionality, displaying a refresh indicator while
 * refreshing.
 *
 * @param isRefreshing Whether the refresh operation is ongoing; the indicator shows while true.
 * @param onRefresh Called on pull-to-refresh with parameters for filtering:
 * - [center]: the central [GeoPoint] for location-based filtering.
 * - [radiusInMeters]: search radius in meters.
 * - [genders]: list of [Gender] to filter by.
 * - [ageRange]: age range for filtering.
 * - [tags]: tags for filtering.
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
    locationViewModel: LocationViewModel,
    filterViewModel: FilterViewModel,
    tagsViewModel: TagsViewModel,
    isRefreshing: Boolean,
    onRefresh:
        (
            center: GeoPoint,
            radiusInMeters: Int,
            genders: List<Gender>?,
            ageRange: IntRange?,
            tags: List<String>?) -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
      Indicator(
          modifier = Modifier.align(Alignment.TopCenter).testTag("refreshIndicator"),
          isRefreshing = isRefreshing,
          state = state)
    },
    content: @Composable BoxScope.() -> Unit
) {
  val userLocation = locationViewModel.lastKnownLocation.collectAsState()
  Box(
      modifier.pullToRefresh(state = state, isRefreshing = isRefreshing) {
        onRefresh(
            userLocation.value ?: GeoPoint(DEFAULT_USER_LATITUDE, DEFAULT_USER_LONGITUDE),
            filterViewModel.selectedRadius.value,
            filterViewModel.selectedGenders.value,
            filterViewModel.ageRange.value,
            tagsViewModel.filteredTags.value)
      },
      contentAlignment = contentAlignment) {
        content()
        indicator()
      }
}

@Composable
fun EmptyProfilePrompt(label: String, testTag: String) {
  Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().testTag(testTag)) {
    Text(
        text = label,
        fontSize = TEXT_SIZE_LARGE,
        fontWeight = FontWeight.Bold,
        color = messageTextColor,
        textAlign = TextAlign.Center)
  }
}
