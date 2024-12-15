package com.github.se.icebreakrr.ui.map

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.profile.MarkerOverlay
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LATITUDE
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LONGITUDE
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.utils.sphericalDistance

private const val DEFAULT_ZOOM = 16F


@Composable
fun LocationViewMapScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    meetingRequestViewModel: MeetingRequestViewModel,
    navBackStackEntry: NavBackStackEntry?,
    isTesting: Boolean
) {
    val loadingSelfProfile = profilesViewModel.loadingSelf.collectAsState()
    val centerLatitude = profilesViewModel.selfProfile.value?.location?.latitude ?: DEFAULT_USER_LATITUDE
    val centerLongitude = profilesViewModel.selfProfile.value?.location?.longitude ?: DEFAULT_USER_LONGITUDE
    val profileId = if (!isTesting) navBackStackEntry?.arguments?.getString("userId") else "2"

    var mapLoaded by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf("") }
    var markerState by remember { mutableStateOf<MarkerState?>(null) }
    var selfMarkerState by remember { mutableStateOf<MarkerState?>(null) }

    LaunchedEffect(Unit) {
        profilesViewModel.getProfileByUidAndThen(profileId ?: "null") {
            val userInviting = profilesViewModel.selectedProfile.value
            meetingRequestViewModel.updateInboxOfMessages {
                val meetingMessages = profilesViewModel.inboxItems.value[userInviting]
                val (messagePair, coordinates) = meetingMessages ?: Pair(Pair("null", "null"), Pair(0.0, 0.0))
                val (firstMessage, secondMessage) = messagePair
                locationMessage = secondMessage
                markerState = MarkerState(
                    position = LatLng(coordinates.first, coordinates.second)
                )
                val ourPosition = profilesViewModel.selfProfile.value?.location
                if (ourPosition != null) {
                    selfMarkerState = MarkerState(
                        position = LatLng(ourPosition.latitude, ourPosition.longitude)
                    )
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(centerLatitude, centerLongitude),
            DEFAULT_ZOOM
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.White).testTag("LocationViewMapScreen"),
        topBar = {
            TopBar("Meeting Point", true) {
                navigationActions.goBack()
            }
        }
    ) { paddingValues ->
        if (!loadingSelfProfile.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.matchParentSize().padding(paddingValues),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = { mapLoaded = true },
                    uiSettings = MapUiSettings(
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = false,
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false
                    )
                ) {
                    if (mapLoaded && markerState != null) {
                        Marker(
                            state = markerState!!,
                            title = "Meeting Request's location",
                            onClick = { true },
                            draggable = false
                        )
                        Marker(
                            state = selfMarkerState!!,
                            title = "Our location",
                            onClick = { true },
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                            draggable = false
                        )
                    }
                }

                // Overlay text for the marker
                val projection = cameraPositionState.projection
                val markerScreenPosition = markerState?.let { projection?.toScreenLocation(it.position) }
                val selfMarkerScreenPosition = selfMarkerState?.let{projection?.toScreenLocation(it.position) }
                val density = LocalDensity.current
                markerScreenPosition?.let { screenPosition ->
                    val xOffset = with(density) { screenPosition.x.toDp().toPx() }
                    val yOffset = with(density) { screenPosition.y.toDp().toPx() + 90.dp.toPx() }
                    val markerOffset = Offset(x = xOffset, y = yOffset)
                    MarkerOverlay(position = markerOffset, text = locationMessage)
                }
                selfMarkerScreenPosition?.let { selfScreenPosition ->
                    val xOffset = with(density) { selfScreenPosition.x.toDp().toPx() } // Convert X to pixels
                    val yOffset = with(density) { selfScreenPosition.y.toDp().toPx() + 90.dp.toPx() } // Convert Y to pixels and add marker height
                    val selfMarkerOffset = Offset(x = xOffset, y = yOffset) // Create Offset with calculated values
                    MarkerOverlay(position = selfMarkerOffset, text = "You are here")
                }

            }
        }
    }
}
