package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.github.se.icebreakrr.model.message.MeetingRequestManager.meetingRequestViewModel
import com.github.se.icebreakrr.model.notification.EngagementNotificationManager
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.sort.SortOption
import com.github.se.icebreakrr.model.sort.SortViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.FilterFloatingActionButton
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.utils.IPermissionManager
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailable
import com.github.se.icebreakrr.utils.NetworkUtils.isNetworkAvailableWithContext
import com.github.se.icebreakrr.utils.NetworkUtils.showNoInternetToast
import com.google.firebase.firestore.GeoPoint
import java.util.Locale
import kotlinx.coroutines.delay

// Constants for layout dimensions
private val COLUMN_VERTICAL_PADDING = 16.dp
private val COLUMN_HORIZONTAL_PADDING = 8.dp
private val SORT_TOP_PADDING = 4.dp
private val TEXT_SIZE_LARGE = 20.sp
private val TEXT_SMALL_SIZE = 16.sp
private const val REFRESH_DELAY = 10_000L
private val DROPDOWN_HORIZONTAL_PADDING = 16.dp
private val DROPDOWN_VERTICAL_PADDING = 8.dp

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
    sortViewModel: SortViewModel,
    permissionManager: IPermissionManager,
    appDataStore: AppDataStore,
    isTestMode: Boolean = false,
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
  val myProfile = profilesViewModel.selfProfile.collectAsState()

  // Create the engagement notification manager
  val engagementManager = remember {
    meetingRequestViewModel?.let {
      EngagementNotificationManager(
          profilesViewModel = profilesViewModel,
          meetingRequestViewModel = it,
          appDataStore = appDataStore,
          context = context,
          filterViewModel = filterViewModel)
    }
  }

  // Start monitoring when the screen is active and we have location permission
  LaunchedEffect(isConnected.value, userLocation.value) {
    if (!isTestMode && !isNetworkAvailable()) {
      profilesViewModel.updateIsConnected(false)
    } else if (hasLocationPermission) {
      // Start engagement notifications
      engagementManager?.startMonitoring()

      while (true) {
        // Call the profile fetch function
        profilesViewModel.getFilteredProfilesInRadius(
            userLocation.value ?: GeoPoint(DEFAULT_USER_LATITUDE, DEFAULT_USER_LONGITUDE),
            filterViewModel.selectedRadius.value,
            filterViewModel.selectedGenders.value,
            filterViewModel.ageRange.value,
            tagsViewModel.filteredTags.value)

        profilesViewModel.getMessagingRadiusProfile(
            userLocation.value ?: GeoPoint(DEFAULT_USER_LATITUDE, DEFAULT_USER_LONGITUDE))

        delay(REFRESH_DELAY) // Wait before the next update
      }
    }
  }

  // Stop monitoring when the screen is disposed
  DisposableEffect(Unit) { onDispose { engagementManager?.stopMonitoring() } }

  // Generate the sorted profile list based on the selected sortOption
  val sortOption = sortViewModel.selectedSortOption.collectAsState()
  val sortedProfiles =
      remember(filteredProfiles.value, sortOption.value) {
        when (sortOption.value) {
          SortOption.AGE -> sortViewModel.sortByAge(filteredProfiles.value)
          SortOption.DISTANCE -> sortViewModel.sortByDistance(filteredProfiles.value)
          SortOption.COMMON_TAGS -> sortViewModel.sortByCommonTags(filteredProfiles.value)
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
            selectedItem = Route.AROUND_YOU,
            notificationCount = (myProfile.value?.meetingRequestInbox?.size ?: 0),
            heatMapCount = myProfile.value?.meetingRequestChosenLocalisation?.size ?: 0)
      },
      topBar = { TopBar("Around You") },
      content = { innerPadding ->

        // Wrapping dropdown and profile list in a Column
        Column(modifier = Modifier.padding(innerPadding)) {
          // Sort Options Dropdown
          SortOptionsDropdown(
              selectedOption = sortOption.value,
              onOptionSelected = { sortViewModel.updateSortOption(it) },
              modifier =
                  Modifier.fillMaxWidth()
                      .background(MaterialTheme.colorScheme.primaryContainer)
                      .padding(
                          horizontal = DROPDOWN_HORIZONTAL_PADDING,
                          vertical = DROPDOWN_VERTICAL_PADDING))

          // Profile List
          PullToRefreshBox(
              locationViewModel = locationViewModel,
              filterViewModel = filterViewModel,
              tagsViewModel = tagsViewModel,
              isRefreshing = isLoading.value,
              onRefresh = { center, radiusInMeters, genders, ageRange, tags ->
                profilesViewModel.getFilteredProfilesInRadius(
                    center, radiusInMeters, genders, ageRange, tags)
              },
              modifier = Modifier.fillMaxSize(),
              content = {
                if (!isConnected.value) {
                  EmptyProfilePrompt(
                      label = stringResource(id = R.string.no_internet),
                      testTag = "noConnectionPrompt")
                } else if (!isDiscoverable) {
                  EmptyProfilePrompt(
                      label = stringResource(R.string.ask_to_toggle_location),
                      testTag = "activateLocationPrompt")
                } else if (!isTestMode && !hasLocationPermission) {
                  EmptyProfilePrompt(
                      label = stringResource(R.string.ask_to_give_location_permission),
                      testTag = "noLocationPermissionPrompt")
                } else if (sortedProfiles.isEmpty()) {
                  EmptyProfilePrompt(
                      label = stringResource(R.string.no_one_around),
                      testTag = "emptyProfilePrompt")
                } else {
                  LazyColumn(
                      contentPadding = PaddingValues(vertical = COLUMN_VERTICAL_PADDING),
                      verticalArrangement = Arrangement.spacedBy(COLUMN_VERTICAL_PADDING),
                      modifier =
                          Modifier.fillMaxSize().padding(horizontal = COLUMN_HORIZONTAL_PADDING)) {
                        items(sortedProfiles.size) { index ->
                          ProfileCard(
                              profile = sortedProfiles[index],
                              onclick = {
                                if (isNetworkAvailableWithContext(context)) {
                                  navigationActions.navigateTo(
                                      Screen.OTHER_PROFILE_VIEW +
                                          "?userId=${sortedProfiles[index].uid}")
                                } else {
                                  showNoInternetToast(context)
                                }
                              })
                        }
                      }
                }
              })
        }
      },
      floatingActionButton = { FilterFloatingActionButton(navigationActions) })
}

/**
 * A composable dropdown menu that allows the user to select a sort option.
 *
 * This dropdown displays the currently selected sort option and provides a list of other available
 * options when expanded. The user can select a new sort option from the list, which triggers the
 * provided callback to handle the selection.
 *
 * @param selectedOption The currently selected sort option, displayed at the top of the dropdown.
 * @param onOptionSelected A callback function that is triggered when the user selects a new sort
 *   option.
 * @param modifier A [Modifier] applied to the container of the dropdown for customization.
 */
@Composable
fun SortOptionsDropdown(
    selectedOption: SortOption,
    onOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  // List of all options excluding the selected one
  val otherOptions = SortOption.values().filter { it != selectedOption }

  Column(modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
    // Selected option with a dropdown indicator
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(COLUMN_HORIZONTAL_PADDING)
                .testTag("SortOptionsDropdown_Selected"),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              color = MaterialTheme.colorScheme.onSecondary,
              text =
                  "Sort by: ${
                    selectedOption.name.replace("_", " ").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }",
              fontSize = TEXT_SMALL_SIZE,
              modifier =
                  Modifier.weight(1f) // Pushes the arrow to the end
                      .testTag("SortOptionsDropdown_Text"))
          Icon(
              imageVector =
                  if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
              contentDescription = if (expanded) "Collapse" else "Expand",
              modifier = Modifier.testTag("SortOptionsDropdown_Arrow"))
        }

    // Show other options when expanded
    if (expanded) {
      otherOptions.forEach { option ->
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable {
                      expanded = false
                      onOptionSelected(option)
                    }
                    .padding(COLUMN_HORIZONTAL_PADDING)
                    .testTag("SortOptionsDropdown_Option_${option.name}"),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text =
                      option.name.replace("_", " ").lowercase().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                      },
                  fontSize = TEXT_SMALL_SIZE,
                  color = MaterialTheme.colorScheme.onSecondary // Differentiate it visually
                  )
            }
      }
    }
  }
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
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center)
  }
}
