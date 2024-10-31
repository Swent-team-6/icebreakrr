package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.FilterFloatingActionButton
import com.github.se.icebreakrr.ui.sections.shared.ProfileCard
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.google.firebase.firestore.GeoPoint

/**
 * Composable function for displaying the "Around You" screen.
 *
 * It includes a bottom navigation bar and displays the main content of the screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AroundYouScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    tagsViewModel: TagsViewModel
) {

  val filteredProfiles = profilesViewModel.filteredProfiles.collectAsState()
  val isLoading = profilesViewModel.loading.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("aroundYouScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = navigationActions.currentRoute())
      },
      topBar = { TopBar("Around You") },
      content = { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading.value,
            onRefresh = profilesViewModel::getFilteredProfilesInRadius,
            modifier = Modifier.padding(innerPadding)) {
              LazyColumn(
                  contentPadding = PaddingValues(vertical = 16.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp),
                  modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    if (filteredProfiles.value.isNotEmpty()) {
                      items(filteredProfiles.value.size) { index ->
                        ProfileCard(
                            profile = filteredProfiles.value[index],
                            onclick = { navigationActions.navigateTo(Screen.PROFILE) })
                      }
                    } else {
                      item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().testTag("emptyProfilePrompt")) {
                              Text(
                                  text = "There is no one around. Try moving!",
                                  fontSize = 20.sp,
                                  fontWeight = FontWeight.Bold,
                                  color = Color(0xFF575757))
                            }
                      }
                    }
                  }
            }
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
    isRefreshing: Boolean,
    onRefresh:
        (
            center: GeoPoint,
            radiusInMeters: Double,
            genders: List<Gender>,
            ageRange: IntRange,
            tags: List<String>) -> Unit,
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

  Box(
      modifier.pullToRefresh(state = state, isRefreshing = isRefreshing) {
        // TODO Mocked values, to change with the saved filter values and current location
        onRefresh(
            GeoPoint(0.0, 0.0), 300.0, listOf(Gender.MEN, Gender.WOMEN), 2..99, listOf("travel"))
      },
      contentAlignment = contentAlignment) {
        content()
        indicator()
      }
}
