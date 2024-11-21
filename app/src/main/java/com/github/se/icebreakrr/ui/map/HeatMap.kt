package com.github.se.icebreakrr.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

private val DEFAULT_LOCATION = LatLng(46.5197, 6.6323) // EPFL coordinates
private const val DEFAULT_ZOOM = 15f

// Define custom gradient colors (from blue to red)
private val GRADIENT_COLORS =
    intArrayOf(
        0x00000000, // Transparent
        0xFF1FAEF0.toInt(), // IceBreakrrBlue
        0xFF4B8BE0.toInt(), // Light Blue
        0xFF7768D0.toInt(), // Purple
        0xFFA346C0.toInt(), // Pink-purple
        0xFFCE0E00.toInt() // Red
        )

private val GRADIENT_START_POINTS =
    floatArrayOf(
        0.0f, // Fully transparent at 0% intensity
        0.2f, // Transition to IceBreakrrBlue
        0.4f, // Transition to Light Blue
        0.6f, // Transition to Purple
        0.8f, // Transition to Pink-purple
        1.0f // Peak at Red
        )

private fun generateFakePoints(): List<WeightedLatLng> {
  // EPFL coordinates: 46.5197, 6.6323
  val epflLat = 46.5197
  val epflLon = 6.6323

  // Create a list to store our fake points
  val fakePoints = mutableListOf<WeightedLatLng>()

  // Add points around campus with different weights
  // Main buildings area
  fakePoints.addAll(
      listOf(
          WeightedLatLng(LatLng(46.5200, 6.6325), 5.0), // BC building
          WeightedLatLng(LatLng(46.5195, 6.6330), 4.0), // Library
          WeightedLatLng(LatLng(46.5192, 6.6318), 3.0), // CM building
          WeightedLatLng(LatLng(46.5205, 6.6315), 4.0), // SG building
      ))

  // Rolex Learning Center area
  fakePoints.addAll(
      listOf(
          WeightedLatLng(LatLng(46.5185, 6.6322), 5.0),
          WeightedLatLng(LatLng(46.5183, 6.6325), 4.0),
          WeightedLatLng(LatLng(46.5184, 6.6320), 4.0),
          WeightedLatLng(LatLng(46.5186, 6.6318), 3.0),
      ))

  // Innovation Park area
  fakePoints.addAll(
      listOf(
          WeightedLatLng(LatLng(46.5175, 6.6365), 2.0),
          WeightedLatLng(LatLng(46.5173, 6.6360), 2.0),
          WeightedLatLng(LatLng(46.5177, 6.6362), 1.0),
      ))

  // Sports center area
  fakePoints.addAll(
      listOf(
          WeightedLatLng(LatLng(46.5220, 6.6380), 3.0),
          WeightedLatLng(LatLng(46.5218, 6.6385), 2.0),
          WeightedLatLng(LatLng(46.5215, 6.6382), 2.0),
      ))

  // Metro station area
  fakePoints.addAll(
      listOf(
          WeightedLatLng(LatLng(46.5225, 6.6285), 4.0),
          WeightedLatLng(LatLng(46.5223, 6.6283), 3.0),
          WeightedLatLng(LatLng(46.5227, 6.6287), 3.0),
      ))

  return fakePoints
}

@Composable
fun HeatMap(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    locationViewModel: LocationViewModel
) {
  val userLocation = locationViewModel.lastKnownLocation.collectAsState()

  Scaffold(
      modifier = Modifier.testTag("heatMapScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              if (route.route != Route.HEAT_MAP) {
                navigationActions.navigateTo(route)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.HEAT_MAP)
      }) { paddingValues ->
        if (userLocation.value == null) {
          // Show loading box when location is not available
          Box(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .background(Color.LightGray)
                      .testTag("loadingBox"),
              contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                      CircularProgressIndicator()
                      Spacer(modifier = Modifier.height(16.dp))
                      Text("Getting your location...", textAlign = TextAlign.Center)
                    }
              }
        } else {
          // Rest of your existing map code
          val mapLocation =
              remember(userLocation.value) {
                userLocation.value?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0)
              }

          val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mapLocation, DEFAULT_ZOOM)
          }

          val gradient = Gradient(GRADIENT_COLORS, GRADIENT_START_POINTS)
          val profiles = profilesViewModel.filteredProfiles.collectAsState()

          val weightedLocations =
              remember(profiles.value) {
                val realLocations =
                    profiles.value.mapNotNull { profile ->
                      profile.location?.let { location ->
                        WeightedLatLng(LatLng(location.latitude, location.longitude), 1.0)
                      }
                    }

                // Combine real locations with fake points
                realLocations + generateFakePoints()
              }

          val heatmapProvider =
              remember(weightedLocations) {
                if (weightedLocations.isNotEmpty()) {
                  HeatmapTileProvider.Builder()
                      .weightedData(weightedLocations)
                      .radius(50)
                      .opacity(0.8)
                      .gradient(gradient)
                      .maxIntensity(15.0)
                      .build()
                } else null
              }

          // Update data fetch to use current location
          LaunchedEffect(userLocation.value) {
            val center =
                userLocation.value
                    ?: GeoPoint(DEFAULT_LOCATION.latitude, DEFAULT_LOCATION.longitude)
            profilesViewModel.getFilteredProfilesInRadius(
                center = center, radiusInMeters = 5000.0 // 5km radius
                )
          }

          GoogleMap(
              modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("googleMap"),
              cameraPositionState = cameraPositionState) {
                // Only show heatmap if we have data
                heatmapProvider?.let { provider ->
                  TileOverlay(tileProvider = provider, transparency = 0.0f)
                }
              }
        }
      }
}
