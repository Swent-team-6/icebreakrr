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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
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
fun LocationSelectorMapScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    meetingRequestViewModel: MeetingRequestViewModel,
    navBackStackEntry: NavBackStackEntry?,
    locationViewModel: LocationViewModel,
    isTesting: Boolean
) {
  val loadingSelfProfile = profilesViewModel.loadingSelf.collectAsState()
  val centerLatitude =
      profilesViewModel.selfProfile.value?.location?.latitude ?: DEFAULT_USER_LATITUDE
  val centerLongitude =
      profilesViewModel.selfProfile.value?.location?.longitude ?: DEFAULT_USER_LONGITUDE
  var markerState by remember { mutableStateOf<MarkerState?>(null) }
  val profileId = if (!isTesting) navBackStackEntry?.arguments?.getString("userId") else "2"
  val profile = profilesViewModel.selectedProfile.collectAsState()
  val context = LocalContext.current
  val lastKnownLocation = locationViewModel.lastKnownLocation.collectAsState()
  var mapLoaded by remember { mutableStateOf(false) }
  var stringQuery by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    profilesViewModel.getSelfProfile {}
    profilesViewModel.getProfileByUid(profileId ?: "null")
    markerState =
        MarkerState(
            position =
                LatLng(
                    lastKnownLocation.value?.latitude ?: DEFAULT_USER_LATITUDE,
                    lastKnownLocation.value?.longitude ?: DEFAULT_USER_LONGITUDE))
  }

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(centerLatitude, centerLongitude), DEFAULT_ZOOM)
  }

  Scaffold(
      modifier =
          Modifier.fillMaxSize().background(Color.White).testTag("LocationSelectorMapScreen"),
      topBar = { TopBar("Select Meeting Point", false, {}) },
      bottomBar = {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(BOTTOM_BAR_HEIGHT.dp)
                    .background(color = IceBreakrrBlue)
                    .testTag("addTextAndSendLocationBox"),
        ) {
          TextField(
              value = stringQuery,
              onValueChange = {
                if (it.length < TEXTFIELD_MAX_CHAR) {
                  stringQuery = it
                }
              },
              label = { Text("Add Details...", modifier = Modifier.testTag("labelTagSelector")) },
              modifier =
                  Modifier.padding(
                          start = TEXT_FIELD_HORIZONTAL_PADDING.dp,
                          end = TEXT_FIELD_HORIZONTAL_PADDING.dp,
                          top = TEXT_FIELD_VERTICAL_PADDING.dp,
                          bottom = TEXT_FIELD_VERTICAL_PADDING.dp)
                      .fillMaxSize()
                      .shadow(
                          elevation = TEXT_FIELD_ELEVATION.dp,
                          shape = RoundedCornerShape(TEXT_FIELD_ROUNDED_CORNER.dp))
                      .testTag("addDetailsTextField"),
          )
          IconButton(
              onClick = {
                if (mapLoaded || isTesting) {
                  val targetProfile = profile.value
                    if (targetProfile != null) {
                        meetingRequestViewModel.setMeetingRequestChangeSecondMessage(
                            location = markerState?.position?.latitude!!.toString() +
                                    ", " +
                                    markerState?.position?.longitude!!.toString(),
                            message2 = stringQuery
                        )

                        meetingRequestViewModel.sendMeetingRequest()
                        meetingRequestViewModel.addToMeetingRequestSent(profile.value!!.uid)
                        meetingRequestViewModel.startMeetingRequestTimer(
                            targetProfile.uid,
                            targetProfile.fcmToken!!,
                            targetProfile.name,
                            context
                        )
                        navigationActions.navigateTo(Route.MAP)
                    }
                }
              },
              modifier =
                  Modifier.align(Alignment.BottomEnd)
                      .padding(
                          end = TEXT_FIELD_HORIZONTAL_PADDING.dp,
                          bottom = TEXT_FIELD_VERTICAL_PADDING.dp)
                      .testTag("buttonSendMessageLocation")) {
                Icon(Icons.Default.Check, contentDescription = "Confirm")
              }
        }
      }) { paddingValues ->
        if (!loadingSelfProfile.value) {
          Column(verticalArrangement = Arrangement.Top) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("normalMap"),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLong ->
                  if (LatLng(centerLatitude, centerLongitude).sphericalDistance(latLong) <
                      MAX_DISTANCE_MEETING_POINT) {
                    markerState?.position = latLong
                  } else {
                    Toast.makeText(
                            context,
                            "You should select a meeting point inside the white circle!",
                            Toast.LENGTH_SHORT)
                        .show()
                  }
                },
                onMapLoaded = { mapLoaded = true },
                onMapLongClick = { markerState?.position = it },
                uiSettings = MapUiSettings(zoomControlsEnabled = false)) {
                  if (mapLoaded) {
                    Circle(
                        LatLng(centerLatitude, centerLongitude),
                        radius = MAX_DISTANCE_MEETING_POINT.toDouble(),
                        fillColor = Color(COLOR_INSIDE_CIRCLE),
                        strokeColor = Color.Black,
                        strokeWidth = CIRCLE_STROKE)
                    Marker(
                        state = markerState!!,
                        title = "Selected Position",
                        onClick = { true },
                        draggable = true)
                  }
                }
          }
        }
      }
}
