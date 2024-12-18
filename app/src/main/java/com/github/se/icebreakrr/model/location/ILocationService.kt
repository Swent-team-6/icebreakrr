package com.github.se.icebreakrr.model.location

import android.location.Location

interface ILocationService {
  fun startLocationUpdates(
      onLocationUpdate: (Location) -> Unit,
      onError: ((String) -> Unit)? = null
  ): Boolean

  fun stopLocationUpdates()
}
