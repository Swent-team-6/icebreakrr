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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.icebreakrr.R
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
private const val RADIUS = 10000.0

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

val gradient = Gradient(GRADIENT_COLORS, GRADIENT_START_POINTS)

/** This function generates fake points around lausanne to showcase the heatmap display */
private fun generateFakePoints(centerLat: Double, centerLon: Double, radius: Double, numberOfPoints: Int = 50): List<WeightedLatLng> {
    val fakePoints = mutableListOf<WeightedLatLng>()

    for (i in 0 until numberOfPoints) { // Generate the specified number of fake points
        val randomLat = centerLat + (Math.random() - 0.5) * (radius / 111000) // Convert radius from meters to degrees
        val randomLon = centerLon + (Math.random() - 0.5) * (radius / (111000 * Math.cos(Math.toRadians(centerLat)))) // Adjust for latitude

        // Add the generated point with a random weight
        fakePoints.add(WeightedLatLng(LatLng(randomLat, randomLon), Math.random() * 10)) // Random weight between 0 and 10
    }

    return fakePoints
}


@Composable
fun HeatMap(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    locationViewModel: LocationViewModel
) {
    val userLocation = locationViewModel.lastKnownLocation.collectAsState()
    val profiles = profilesViewModel.filteredProfiles.collectAsState()

    // State to hold the heatmap provider
    var heatmapProvider by remember { mutableStateOf<HeatmapTileProvider?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) } // Track if the map is loaded

    // Define the coordinates for generating fake points
    val locations = listOf(
        Pair(46.51805886663674, 6.637429806298338), // Location 1 Lausanne
        Pair(47.38787303565691, 8.523686394824663), // Location 2 Zurich
        Pair(46.004976622692936, 8.95195211094253)  // Location 3 Lugano
    )

    // Generate fake points around the specified locations
    val allFakePoints = locations.flatMap { (lat, lon) ->
        generateFakePoints(lat, lon, radius = 5000.0, numberOfPoints = 20) // Adjust radius and number of points as needed
    }

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
                selectedItem = Route.HEAT_MAP
            )
        }
    ) { paddingValues ->
        if (userLocation.value == null) {
            // Show loading box when location is not available
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.LightGray)
                    .testTag("loadingBox"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading location...", textAlign = TextAlign.Center)
                }
            }
        } else {
            val location = userLocation.value!!
            val mapLocation = remember(location) { LatLng(location.latitude, location.longitude) }
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(mapLocation, DEFAULT_ZOOM)
            }

            // Update heatmap when profiles change
            LaunchedEffect(profiles.value) {
                val weightedLocations = profiles.value.mapNotNull { profile ->
                    profile.location?.let { location ->
                        WeightedLatLng(LatLng(location.latitude, location.longitude), 1.0)
                    }
                } + allFakePoints // Combine with fake points

                heatmapProvider = HeatmapTileProvider.Builder()
                    .weightedData(weightedLocations)
                    .radius(50)
                    .opacity(0.8)
                    .gradient(gradient)
                    .maxIntensity(15.0)
                    .build()
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("googleMap"),
                cameraPositionState = cameraPositionState,
                onMapLoaded = {
                    isMapLoaded = true // Set map loaded state to true
                    val center = cameraPositionState.position.target
                    profilesViewModel.getFilteredProfilesInRadius(
                        center = GeoPoint(center.latitude, center.longitude),
                        radiusInMeters = RADIUS // Adjust radius as needed
                    )
                }
            ) {
                // Only show heatmap if we have data
                heatmapProvider?.let { provider ->
                    TileOverlay(tileProvider = provider, transparency = 0.0f)
                }
            }

            // Fetch profiles when the camera position changes
            LaunchedEffect(cameraPositionState.position) {
                if (isMapLoaded) {
                    val center = cameraPositionState.position.target
                    profilesViewModel.getFilteredProfilesInRadius(
                        center = GeoPoint(center.latitude, center.longitude),
                        radiusInMeters = RADIUS // Adjust radius as needed
                    )
                }
            }
        }
    }
}
