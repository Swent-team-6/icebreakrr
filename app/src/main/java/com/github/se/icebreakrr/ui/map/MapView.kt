package com.github.se.icebreakrr.ui.profile

import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.map.UserMarker
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.utils.cropUsername
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlin.math.roundToInt

// Constants for default values
private val DEFAULT_LOCATION = LatLng(46.5197, 6.6323) // EPFL coordinates
private const val DEFAULT_ZOOM = 15f
private const val DEFAULT_RADIUS = 10000 // Radius in meters
private const val MOVED_RELOAD_DISTANCE = 200 // Distance in meters to trigger reload
private const val HEATMAP_RADIUS = 50 // Radius for heatmap points
private const val HEATMAP_OPACITY = 0.8 // Opacity for heatmap
private const val HEATMAP_MAX_INTENSITY = 15.0 // Maximum intensity for heatmap

// Padding constants
private val BUTTON_PADDING = 16.dp
private val LOADING_BOX_PADDING = 16.dp
private val OVERLAY_WIDTH = 90.dp // Width of the overlay
private val OVERLAY_HEIGHT = 25.dp // Height of the overlay
private val OVERLAY_OFFSET_Y = 10 // Offset for overlay position

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

@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    profilesViewModel: ProfilesViewModel,
    locationViewModel: LocationViewModel,
) {
    val userLocation = locationViewModel.lastKnownLocation.collectAsState()
    val profiles = profilesViewModel.filteredProfiles.collectAsState()
    val myProfile = profilesViewModel.selfProfile.collectAsState()

    var heatmapProvider by remember { mutableStateOf<HeatmapTileProvider?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }
    var lastCameraPosition by remember { mutableStateOf<LatLng?>(null) }
    var isHeatmapVisible by remember { mutableStateOf(true) }

    // Observe the meetingRequestChosenLocalisation
    val meetingRequestChosenLocalisation =
        myProfile.value?.meetingRequestChosenLocalisation ?: emptyMap()

    // Create a list of UIDs to fetch profiles
    val uidsToFetch = meetingRequestChosenLocalisation.keys.toList()

    // Create a mutable list to hold the fetched profiles
    val profilesMeeting = remember { mutableStateListOf<Profile>() }

    // Fetch profiles for the UIDs
    LaunchedEffect(uidsToFetch) {
        if (uidsToFetch.isNotEmpty()) {
            profilesMeeting.clear()
            uidsToFetch.forEach { uid ->
                profilesViewModel.getProfileByUidAndThen(uid) {
                    // Add the profile to the list after fetching
                    profilesViewModel.selectedProfile.value?.let { profile -> profilesMeeting.add(profile) }
                }
            }
        }
    }

    // Create UserMarkers from meetingRequestChosenLocalisation
    val userMarkers =
        meetingRequestChosenLocalisation.mapNotNull { (uid, pair) ->
            val (message, coordinates) = pair
            val profile = profilesMeeting.find { it.uid == uid }
            profile?.let {
                UserMarker(
                    uid = uid,
                    username = cropUsername(it.name, 10), // Crop username to 10 characters
                    location = LatLng(coordinates.first, coordinates.second),
                    overlayPosition = null // Initially set to null
                )
            }
        }

    val markerStates =
        userMarkers.map { userMarker -> rememberMarkerState(position = userMarker.location) }

    Scaffold(
        modifier = Modifier.testTag("MapScreen"),
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route ->
                    if (route.route != Route.MAP) {
                        navigationActions.navigateTo(route)
                    }
                },
                tabList = LIST_TOP_LEVEL_DESTINATIONS,
                selectedItem = Route.MAP,
                notificationCount = myProfile.value?.meetingRequestInbox?.size ?: 0,
                heatMapCount = myProfile.value?.meetingRequestChosenLocalisation?.size ?: 0
            )
        }) { paddingValues ->
        if (userLocation.value == null) {
            // Show loading box when location is not available
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("loadingBox"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val location = userLocation.value!!
            val mapLocation = LatLng(location.latitude, location.longitude)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(mapLocation, DEFAULT_ZOOM)
            }

            // Update heatmap when profiles change
            LaunchedEffect(profiles.value) {
                val weightedLocations =
                    profiles.value.mapNotNull { profile ->
                        profile.location?.let { loc ->
                            WeightedLatLng(LatLng(loc.latitude, loc.longitude), 1.0)
                        }
                    }

                // Check if weightedLocations is not empty before updating the heatmap
                if (weightedLocations.isNotEmpty()) {
                    heatmapProvider =
                        HeatmapTileProvider.Builder()
                            .weightedData(weightedLocations)
                            .radius(HEATMAP_RADIUS)
                            .opacity(HEATMAP_OPACITY)
                            .gradient(gradient)
                            .maxIntensity(HEATMAP_MAX_INTENSITY)
                            .build()
                } else {
                    // Optionally handle the case where there are no valid locations
                    Log.w("MapScreen", "No valid locations to display on the heatmap.")
                }
            }

            // Example of cleaning up profilesMeeting when no longer needed
            DisposableEffect(Unit) {
                onDispose {
                    profilesMeeting.clear() // Clear the list when the composable is disposed
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("googleMap"),
                cameraPositionState = cameraPositionState,
                onMapLoaded = {
                    isMapLoaded = true
                    val center = cameraPositionState.position.target
                    profilesViewModel.getFilteredProfilesInRadius(
                        center = GeoPoint(center.latitude, center.longitude),
                        radiusInMeters = DEFAULT_RADIUS
                    )
                    lastCameraPosition = center
                },
                onMapClick = {
                    // Update overlay position when the map is clicked (optional)
                }) {
                if (isHeatmapVisible) {
                    heatmapProvider?.let { provider ->
                        TileOverlay(tileProvider = provider, transparency = 0.0f)
                    }
                }

                userMarkers.forEachIndexed { index, userMarker ->
                    val markerState = markerStates[index]

                    // Add the marker to the map
                    Marker(
                        contentDescription = "Marker for ${userMarker.username}",
                        state = markerState,
                        title = userMarker.username,
                        snippet = "This is ${userMarker.username}'s location",
                        onClick = {
                            // Handle marker click, e.g., navigate to user profile
                            navigationActions.navigateTo(
                                Screen.OTHER_PROFILE_VIEW + "?userId=${userMarker.uid}"
                            )
                            true // Return true to indicate the event was handled
                        })

                    // Update overlay position based on the marker's position
                    val projection = cameraPositionState.projection
                    val markerScreenPosition = projection?.toScreenLocation(userMarker.location)
                    if (markerScreenPosition != null) {
                        userMarker.overlayPosition =
                            Offset(markerScreenPosition.x.toFloat(), markerScreenPosition.y.toFloat())
                    }
                }
            }

            userMarkers.forEach { userMarker ->
                // Use the MarkerOverlay composable to display the text above the marker
                userMarker.overlayPosition?.let {
                    MarkerOverlay(position = it, text = userMarker.username)
                }
            }

            // Toggle button for heatmap visibility
            Box(modifier = Modifier.padding(BUTTON_PADDING)) {
                Button(onClick = { isHeatmapVisible = !isHeatmapVisible }) {
                    Text(if (isHeatmapVisible) "Hide Heatmap" else "Show Heatmap")
                }
            }

            // Fetch profiles when the camera position changes
            LaunchedEffect(cameraPositionState.position) {
                if (isMapLoaded) {
                    val center = cameraPositionState.position.target
                    val lastPosition = lastCameraPosition ?: center

                    // Ensure lastPosition is valid before creating Location objects
                    if (lastPosition.latitude.isNaN() || lastPosition.longitude.isNaN()) {
                        return@LaunchedEffect
                    }

                    // Create Location objects for distance calculation
                    val currentLocation =
                        Location("").apply {
                            latitude = center.latitude
                            longitude = center.longitude
                        }

                    val previousLocation =
                        Location("").apply {
                            latitude = lastPosition.latitude
                            longitude = lastPosition.longitude
                        }

                    // Calculate the distance moved
                    val distanceMoved = currentLocation.distanceTo(previousLocation)

                    // Check if the distance moved is greater than the current radius
                    if (distanceMoved >= MOVED_RELOAD_DISTANCE) {
                        // Validate inputs before calling the function
                        if (!center.latitude.isNaN() && !center.longitude.isNaN() && DEFAULT_RADIUS > 0) {
                            try {
                                profilesViewModel.getFilteredProfilesInRadius(
                                    center = GeoPoint(center.latitude, center.longitude),
                                    radiusInMeters = DEFAULT_RADIUS
                                )
                                lastCameraPosition = center // Update last camera position
                            } catch (e: Exception) {
                                Log.e("HeatMap", "Error fetching profiles: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkerOverlay(position: Offset, text: String) {
    Box(
        modifier =
            Modifier.offset {
                IntOffset(
                    position.x.roundToInt() - (OVERLAY_WIDTH.toPx() / 2).roundToInt(),
                    position.y.roundToInt() + OVERLAY_OFFSET_Y
                ) // Center under the pin
            }
            .size(OVERLAY_WIDTH, OVERLAY_HEIGHT) // Set the size of the overlay
            .testTag("markerOverlay")
            .background(
                Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp) // Rounded corners
            )
    ) {
        Text(
            text = text,
            color = Color.White, // White text for contrast
            fontSize = 9.sp, // Change this value to adjust the font size
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.align(Alignment.Center) // Center the text within the box
                    .wrapContentSize() // Ensure the text wraps correctly
                    .padding(2.dp) // Optional: Add padding to the text for better spacing
        )
    }
}
