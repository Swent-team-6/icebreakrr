package com.github.se.icebreakrr.ui.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LATITUDE
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LONGITUDE
import com.github.se.icebreakrr.ui.sections.shared.TopBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.utils.sphericalDistance

@Composable
fun LocationSelectorMapScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    meetingRequestViewModel: MeetingRequestViewModel,
    navBackStackEntry: NavBackStackEntry
) {
  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp
  val screenWidth = configuration.screenWidthDp

  val loadingSelfProfile = profilesViewModel.loadingSelf.collectAsState()
  val centerLatitude =
      profilesViewModel.selfProfile.value?.location?.latitude ?: DEFAULT_USER_LATITUDE
  val centerLongitude =
      profilesViewModel.selfProfile.value?.location?.longitude ?: DEFAULT_USER_LONGITUDE
  var markerState by remember { mutableStateOf<MarkerState?>(null) }
  var showConfirmButton by remember { mutableStateOf(false) }
  val profileId = navBackStackEntry.arguments?.getString("userId")
  val profile = profilesViewModel.selectedProfile.collectAsState()
  val context = LocalContext.current
  var buttonOffset by remember { mutableStateOf(Offset.Zero) }

  LaunchedEffect(Unit) {
    profilesViewModel.getSelfProfile()
    profilesViewModel.getProfileByUid(profileId ?: "null")
  }

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(centerLatitude, centerLongitude), 15F)
  }

  Scaffold(
      modifier = Modifier.fillMaxSize().background(Color.White),
      topBar = { TopBar("Select Meeting Point", false, {}) }) { paddingValues ->
        if (!loadingSelfProfile.value) {
          GoogleMap(
              modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("normalMap"),
              cameraPositionState = cameraPositionState,
              onMapClick = { latLong ->
                if (LatLng(centerLatitude, centerLongitude).sphericalDistance(latLong) < 500) {
                  markerState = MarkerState(position = latLong)
                  showConfirmButton = true
                } else {
                  Toast.makeText(
                          context,
                          "You should select a meeting point inside the white circle!",
                          Toast.LENGTH_SHORT)
                      .show()
                }
              },
          ) {
            Circle(
                LatLng(centerLatitude, centerLongitude),
                radius = 500.0,
                fillColor = Color(0x11FFFFFF),
                strokeColor = Color.Black,
                strokeWidth = 2f)
            markerState?.let { state ->
              Marker(state = state, title = "Selected Position", onClick = { true })
            }
          }

          if (showConfirmButton) {
            FloatingActionButton(
                onClick = {
                  showConfirmButton = false
                  meetingRequestViewModel.confirmMeetingLocation(
                      profileId!!,
                      Pair(markerState?.position?.latitude!!, markerState?.position?.longitude!!))
                  meetingRequestViewModel.setMeetingConfirmation(
                      profile.value?.fcmToken!!,
                      markerState?.position?.latitude!!.toString() +
                          ", " +
                          markerState?.position?.longitude!!.toString())
                  meetingRequestViewModel.sendMeetingConfirmation()
                  navigationActions.navigateTo(Route.HEAT_MAP)
                },
                modifier =
                    Modifier.absoluteOffset(x = (screenWidth - 75).dp, y = (screenHeight - 170).dp)
                        .size(60.dp)) {
                  Icon(Icons.Default.Check, contentDescription = "Confirm Pin")
                }
          }
        }
      }
}
