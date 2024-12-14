package com.github.se.icebreakrr.ui.map

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.location.LocationViewModel
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
private const val BOTTOM_BAR_HEIGHT = 150
private const val TEXTFIELD_MAX_CHAR = 113
private const val TEXT_FIELD_VERTICAL_PADDING = 8
private const val TEXT_FIELD_HORIZONTAL_PADDING = 16
private const val TEXT_FIELD_ELEVATION = 8
private const val TEXT_FIELD_ROUNDED_CORNER = 8
private const val MAX_DISTANCE_MEETING_POINT = 500
private const val COLOR_INSIDE_CIRCLE = 0x11FFFFFF
private const val CIRCLE_STROKE = 2f

@Composable
fun LocationViewMapScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    meetingRequestViewModel: MeetingRequestViewModel,
    navBackStackEntry: NavBackStackEntry?,
    isTesting: Boolean
) {
    val loadingSelfProfile = profilesViewModel.loadingSelf.collectAsState()
    val centerLatitude =
        profilesViewModel.selfProfile.value?.location?.latitude ?: DEFAULT_USER_LATITUDE
    val centerLongitude =
        profilesViewModel.selfProfile.value?.location?.longitude ?: DEFAULT_USER_LONGITUDE
    var markerState by remember { mutableStateOf<MarkerState?>(null) }
    val profileId = if (!isTesting) navBackStackEntry?.arguments?.getString("userId") else "2"
    val context = LocalContext.current
    var mapLoaded by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        profilesViewModel.getProfileByUidAndThen(profileId ?: "null") {
        val userInviting = profilesViewModel.selectedProfile.value
        meetingRequestViewModel.updateInboxOfMessages {
            val meetingMessages = profilesViewModel.inboxItems.value[userInviting]!!
            val (messagePair, coordinates) = meetingMessages
            val (firstMessage, secondMessage) = messagePair
            locationMessage = secondMessage
            markerState =
                MarkerState(
                    position =
                    LatLng(
                        coordinates.first,
                        coordinates.second
                    )
                )
        }
    }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(centerLatitude, centerLongitude), DEFAULT_ZOOM)
    }

    Scaffold(
        modifier =
        Modifier.fillMaxSize().background(Color.White).testTag("LocationViewMapScreen"),
        topBar = { TopBar("Meeting Point", true) { navigationActions.goBack() } },
        ) { paddingValues ->
        if (!loadingSelfProfile.value) {
            Column(verticalArrangement = Arrangement.Top) {

                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("meetingRequestLocationMap"),
                        cameraPositionState = cameraPositionState,
                        onMapClick = {},
                        onMapLoaded = { mapLoaded = true },
                        onMapLongClick = { markerState?.position = it },
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)) {
                        if (mapLoaded) {
                            Marker(
                                state = markerState!!,
                                title = "Meeting request location",
                                snippet = "YOLO",
                                onClick = { true },
                                draggable = true)
                        }
                    }
                }
                val projection = cameraPositionState.projection
                val markerScreenPosition = markerState?.let { projection?.toScreenLocation(it.position) }
                if (projection == null || markerScreenPosition == null) {
                    Log.d("MapScreen", "Projection or marker screen position is not ready")
                }
                val markerOffset =
                    markerScreenPosition?.x?.let { Offset(it.toFloat(), markerScreenPosition.y.toFloat()) }
                if (mapLoaded && markerState != null && markerOffset != null) {
                    MarkerOverlay(markerOffset, locationMessage)
                }
            }
        }
    }
}
