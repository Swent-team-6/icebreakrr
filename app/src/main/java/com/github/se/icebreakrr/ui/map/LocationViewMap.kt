package com.github.se.icebreakrr.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.profile.MarkerOverlay
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LATITUDE
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LONGITUDE
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private const val DEFAULT_ZOOM = 16F
private val DEFAULT_MEETING_MESSAGES = Pair(Pair("null", "null"), Pair(0.0, 0.0))
private const val TOP_BAR_TEXT = "Meeting Point"
private const val MARKER_TITLE = "Meeting Request's location"
private const val OUR_MARKER_TITLE = "Our location"
private const val OUR_MARKER_TEXT = "You are here"
private const val TEST_UID = "2"
private val MARKER_HEIGHT = 90.dp
private const val DEFAULT_SCREEN_COORDINATE = 0F
private const val KEY_TEST_UID = "userID"

/**
 * This Screen shows the location and the location message in the received meeting request
 *
 * @param profilesViewModel: The profile view model, to get and modify profiles
 * @param navigationActions: Navigation to go between screens
 * @param navBackStackEntry: The back stack : gives the UID of the target profile
 * @param isTesting: attests if we are in testing mode or in functional mode
 */
@Composable
fun LocationViewMapScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    navBackStackEntry: NavBackStackEntry?,
    isTesting: Boolean
) {
  val loadingSelfProfile = profilesViewModel.loadingSelf.collectAsState()
  val centerLatitude =
      profilesViewModel.selfProfile.value?.location?.latitude ?: DEFAULT_USER_LATITUDE
  val centerLongitude =
      profilesViewModel.selfProfile.value?.location?.longitude ?: DEFAULT_USER_LONGITUDE
  val profileId =
      if (!isTesting) navBackStackEntry?.arguments?.getString(KEY_TEST_UID) else TEST_UID

  var mapLoaded by remember { mutableStateOf(false) }
  var locationMessage by remember { mutableStateOf("") }
  var markerState by remember { mutableStateOf<MarkerState?>(null) }
  var selfMarkerState by remember { mutableStateOf<MarkerState?>(null) }
  val markerScreenPosition = remember {
    mutableStateOf(Offset(DEFAULT_SCREEN_COORDINATE, DEFAULT_SCREEN_COORDINATE))
  }
  val selfMarkerScreenPosition = remember {
    mutableStateOf(Offset(DEFAULT_SCREEN_COORDINATE, DEFAULT_SCREEN_COORDINATE))
  }
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(centerLatitude, centerLongitude), DEFAULT_ZOOM)
  }

  // Fetch data on initial load
  LaunchedEffect(Unit) {
    profilesViewModel.getProfileByUidAndThen(profileId ?: "null") {
      val userInviting = profilesViewModel.selectedProfile.value
      val meetingMessages = profilesViewModel.inboxItems.value[userInviting]
      val (messagePair, coordinates) = meetingMessages ?: DEFAULT_MEETING_MESSAGES
      val (firstMessage, secondMessage) = messagePair

      locationMessage = secondMessage
      markerState = MarkerState(position = LatLng(coordinates.first, coordinates.second))

      val ourPosition = profilesViewModel.selfProfile.value?.location
      if (ourPosition != null) {
        selfMarkerState =
            MarkerState(position = LatLng(ourPosition.latitude, ourPosition.longitude))
      }
    }
  }
  // Update screen positions when the map is loaded
  LaunchedEffect(mapLoaded) {
    if (mapLoaded) {
      val projection = cameraPositionState.projection
      if (projection != null) {
        markerState?.position?.let { latLng ->
          val point = projection.toScreenLocation(latLng)
          markerScreenPosition.value = Offset(point.x.toFloat(), point.y.toFloat())
        }

        selfMarkerState?.position?.let { latLng ->
          val point = projection.toScreenLocation(latLng)
          selfMarkerScreenPosition.value = Offset(point.x.toFloat(), point.y.toFloat())
        }
      }
    }
  }

  // Continuously update screen positions during camera movement
  LaunchedEffect(cameraPositionState.position) {
    val projection = cameraPositionState.projection
    if (projection != null) {
      markerState?.position?.let { latLng ->
        val point = projection.toScreenLocation(latLng)
        markerScreenPosition.value = Offset(point.x.toFloat(), point.y.toFloat())
      }

      selfMarkerState?.position?.let { latLng ->
        val point = projection.toScreenLocation(latLng)
        selfMarkerScreenPosition.value = Offset(point.x.toFloat(), point.y.toFloat())
      }
    }
  }

  Scaffold(
      modifier = Modifier.fillMaxSize().background(Color.White).testTag("LocationViewMapScreen"),
      topBar = { TopBar(TOP_BAR_TEXT, true) { navigationActions.goBack() } }) { paddingValues ->
        if (!loadingSelfProfile.value) {
          Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.matchParentSize().padding(paddingValues),
                cameraPositionState = cameraPositionState,
                onMapLoaded = { mapLoaded = true },
                uiSettings =
                    MapUiSettings(
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        tiltGesturesEnabled = true,
                    ),
            ) {
              if (mapLoaded && markerState != null) {
                Marker(state = markerState!!, title = MARKER_TITLE, draggable = false)
                Marker(
                    state = selfMarkerState!!,
                    title = OUR_MARKER_TITLE,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                    draggable = false)
              }
            }

            // Overlay text for the marker
            val density = LocalDensity.current
            markerScreenPosition.value.let { screenPosition ->
              val xOffset = with(density) { screenPosition.x.toDp().toPx() }
              val yOffset = with(density) { screenPosition.y.toDp().toPx() + MARKER_HEIGHT.toPx() }
              val markerOffset = Offset(x = xOffset, y = yOffset)

              if (xOffset > 0 && yOffset > 0) {
                MarkerOverlay(position = markerOffset, text = locationMessage)
              }
            }
            selfMarkerScreenPosition.value.let { selfScreenPosition ->
              val xOffset =
                  with(density) { selfScreenPosition.x.toDp().toPx() } // Convert X to pixels
              val yOffset =
                  with(density) {
                    selfScreenPosition.y.toDp().toPx() + MARKER_HEIGHT.toPx()
                  } // Convert Y to pixels and add marker height
              val selfMarkerOffset =
                  Offset(x = xOffset, y = yOffset) // Create Offset with calculated values
              if (xOffset > 0 && yOffset > 0) {
                MarkerOverlay(position = selfMarkerOffset, text = OUR_MARKER_TEXT)
              }
            }
          }
        }
      }
}
