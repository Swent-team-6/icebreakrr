package com.github.se.icebreakrr.ui.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.utils.sphericalDistance

@Composable
fun LocationSelectorMapScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
    meetingRequestViewModel: MeetingRequestViewModel,
    navBackStackEntry: NavBackStackEntry,
    locationViewModel: LocationViewModel
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
  val lastKnownLocation = locationViewModel.lastKnownLocation.collectAsState()
  var mapLoaded by remember { mutableStateOf(false) }
  var stringQuery by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    profilesViewModel.getSelfProfile(){}
    profilesViewModel.getProfileByUid(profileId ?: "null")
    markerState = MarkerState(position = LatLng(lastKnownLocation.value?.latitude?: DEFAULT_USER_LATITUDE, lastKnownLocation.value?.longitude?: DEFAULT_USER_LONGITUDE))
  }

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(centerLatitude, centerLongitude), 16F)
  }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.White),
        topBar = { TopBar("Select Meeting Point", false, {}) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp).background(
                        color = IceBreakrrBlue
                        ),
                contentAlignment = Alignment.Center
            ) {
                // Adjusted OutlinedTextField Modifier
                TextField(
                    value = stringQuery,
                    onValueChange = {
                        stringQuery = it
                    },
                    label = { Text("Add Details...", modifier = Modifier.testTag("labelTagSelector")) },
                    placeholder = {
                        Text("", modifier = Modifier.testTag("placeholderTagSelector"))
                    },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp) // Adds padding on the left
                        .fillMaxSize().shadow(
                            elevation = 8.dp, // Adjust shadow intensity
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp) // Optional: Rounded corners
                        ) // Reduces the width to leave space for the FAB
                )
                FloatingActionButton(
                    onClick = {
                        meetingRequestViewModel.confirmMeetingLocation(
                            profileId!!,
                            Pair(markerState?.position?.latitude!!, markerState?.position?.longitude!!)
                        )
                        meetingRequestViewModel.setMeetingConfirmation(
                            profile.value?.fcmToken!!,
                            markerState?.position?.latitude!!.toString() +
                                    ", " +
                                    markerState?.position?.longitude!!.toString()
                        )
                        meetingRequestViewModel.sendMeetingConfirmation()
                        navigationActions.navigateTo(Route.HEAT_MAP)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .zIndex(1f)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 9.dp, end = 17.dp)
                        .background(Color.Transparent)
                        .shadow(elevation = 0.dp, shape = RectangleShape),
                    shape = RectangleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirm Pin")
                }
            }
        }
    ) { paddingValues ->
        if (!loadingSelfProfile.value) {
            Column(verticalArrangement = Arrangement.Top) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("normalMap"),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLong ->
                        if (LatLng(centerLatitude, centerLongitude).sphericalDistance(latLong) < 500) {
                            markerState?.position = latLong
                        } else {
                            Toast.makeText(
                                context,
                                "You should select a meeting point inside the white circle!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onMapLoaded = { mapLoaded = true },
                    onMapLongClick = { markerState?.position = it },
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    if (mapLoaded) {
                        Circle(
                            LatLng(centerLatitude, centerLongitude),
                            radius = 500.0,
                            fillColor = Color(0x11FFFFFF),
                            strokeColor = Color.Black,
                            strokeWidth = 2f
                        )
                        Marker(state = markerState!!, title = "Selected Position", onClick = { true }, draggable = true)
                    }
                }
            }
        }
    }
    }
