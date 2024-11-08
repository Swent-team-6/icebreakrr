package com.github.se.icebreakrr.model.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

class LocationService(private val context: Context) {

  companion object {
    private const val UPDATE_DISTANCE_METERS = 10.0
  }

  private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
  private var locationCallback: LocationCallback? = null
  private var lastKnownLocation: Location? = null

  /**
   * Starts location updates and only provides updates if the user has moved more than
   * [UPDATE_DISTANCE_METERS].
   *
   * @param onLocationUpdate Callback with the latest location data.
   */
  @SuppressLint("MissingPermission")
  fun startLocationUpdates(onLocationUpdate: (Location) -> Unit) {

    if (locationCallback != null) {
      Log.d("LocationService", "Can't start location updates. Already running.")
      return
    }

    val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .setWaitForAccurateLocation(true)
            .build()

    locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { newLocation ->
              Log.d("GEOLOCATION", "Current location: $lastKnownLocation")
              Log.d("GEOLOCATION", "New location: $newLocation")
              if (lastKnownLocation == null ||
                  lastKnownLocation!!.distanceTo(newLocation) > UPDATE_DISTANCE_METERS) {
                lastKnownLocation = newLocation
                Log.d("CALLBACK", "CALLBACK: $onLocationUpdate")
                onLocationUpdate(newLocation)
              }
            }
          }
        }

    fusedLocationProviderClient.requestLocationUpdates(
        locationRequest, locationCallback!!, Looper.getMainLooper())
  }

  /** Stops location updates to conserve resources. */
  fun stopLocationUpdates() {
    locationCallback?.let {
      fusedLocationProviderClient.removeLocationUpdates(it)
      locationCallback = null
    }
  }
}
