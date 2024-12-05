package com.github.se.icebreakrr.model.map

import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.LatLng

data class UserMarker(
    val uid: String,
    val username: String,
    val location: LatLng, // Assuming you have the location as LatLng
    var overlayPosition: Offset? = null // Optional property for overlay position
)
