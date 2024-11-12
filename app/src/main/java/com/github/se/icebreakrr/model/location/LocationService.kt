package com.github.se.icebreakrr.model.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

class LocationService(private val fusedLocationProviderClient: FusedLocationProviderClient) {

  companion object {
    // Distance in meters for triggering a location update
    private const val UPDATE_DISTANCE_METERS = 10.0

    // Location request interval in milliseconds
    private const val REQUEST_INTERVAL_MS = 10000L

    // Minimum interval between location updates in milliseconds
    private const val MIN_UPDATE_INTERVAL_MS = 5000L
  }

  // LocationCallback instance used to handle location updates and availability changes
  private val locationCallback: LocationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          locationResult.lastLocation?.let { newLocation ->
            // Only update if distance from last known location exceeds threshold
            if (lastKnownLocation == null ||
                lastKnownLocation!!.distanceTo(newLocation) > UPDATE_DISTANCE_METERS) {
              lastKnownLocation = newLocation
              onLocationUpdate?.invoke(newLocation) // Invoke callback with new location
            }
          }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
          if (!availability.isLocationAvailable) {
            onError?.invoke("Location service is currently unavailable.")
          }
        }
      }

  // Last known location to help compare and trigger updates only when necessary
  private var lastKnownLocation: Location? = null

  // Callback function for passing updated location data to caller
  private var onLocationUpdate: ((Location) -> Unit)? = null

  // Callback function for passing error messages to caller
  private var onError: ((String) -> Unit)? = null

  /**
   * Starts location updates and provides location data via the callback only if the user has moved
   * more than [UPDATE_DISTANCE_METERS] since the last known location.
   *
   * @param onLocationUpdate Callback function to provide the latest location data.
   * @param onError Optional callback function to handle errors, providing an error message.
   */
  @SuppressLint("MissingPermission")
  fun startLocationUpdates(
      onLocationUpdate: (Location) -> Unit,
      onError: ((String) -> Unit)? = null
  ) {

    this.onLocationUpdate = onLocationUpdate
    this.onError = onError

    try {
      // Create a location request with high accuracy and specified intervals
      val locationRequest =
          LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, REQUEST_INTERVAL_MS)
              .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
              .setWaitForAccurateLocation(true)
              .build()

      // Request location updates, passing the defined callback and main looper
      fusedLocationProviderClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper())
    } catch (e: Exception) {
      Log.e("LocationService", "Error with location request: ${e.message}")
      onError?.invoke("Error with location request: ${e.message}")
    }
  }

  /** Stops location updates to conserve resources. */
  fun stopLocationUpdates() {
    // Remove the location callback to stop receiving updates
    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    onLocationUpdate = null
    onError = null
  }
}
