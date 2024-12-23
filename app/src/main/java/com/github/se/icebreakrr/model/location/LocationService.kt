package com.github.se.icebreakrr.model.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.message.MeetingRequestManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

open class LocationService(
    private val providedFusedLocationProviderClient: FusedLocationProviderClient? = null
) : Service(), ILocationService {

  companion object {
    private const val NOTIFICATION_CHANNEL_ID = "location_service_channel"
    private const val NOTIFICATION_ID = 1

    private const val UPDATE_DISTANCE_METERS = 10.0
    private const val REQUEST_INTERVAL_MS = 10000L
    private const val MIN_UPDATE_INTERVAL_MS = 5000L
  }

  // Last known location to help compare and trigger updates only when necessary
  private var lastKnownLocation: Location? = null

  // Callback function for passing updated location data to caller
  private var onLocationUpdate: ((Location) -> Unit)? = null

  // Callback function for passing error messages to caller
  private var onError: ((String) -> Unit)? = null

  // Binder instance used to communicate with the location service
  private val binder = LocalBinder()

  // Use the provided or initialized FusedLocationProviderClient
  private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
    providedFusedLocationProviderClient ?: LocationServices.getFusedLocationProviderClient(this)
  }

  // LocationCallback instance used to handle location updates and availability changes
  val locationCallback: LocationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          locationResult.lastLocation?.let { newLocation ->
            // Only update if distance from last known location exceeds threshold
            if (lastKnownLocation == null ||
                lastKnownLocation!!.distanceTo(newLocation) > UPDATE_DISTANCE_METERS) {
              lastKnownLocation = newLocation
              onLocationUpdate?.invoke(newLocation) // Invoke callback with new location
              MeetingRequestManager.meetingRequestViewModel?.meetingDistanceCancellation()
            }
          }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
          if (!availability.isLocationAvailable) {
            onError?.invoke("Location service is currently unavailable.")
          }
        }
      }

  /**
   * Called when the service is created. Initializes the location provider and notification channel.
   */
  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  /** Called when the service is destroyed. Ensures location updates are stopped. */
  override fun onDestroy() {
    stopLocationUpdates()
    super.onDestroy()
  }

  /**
   * Handles the start command for the service, ensuring required permissions are present before
   * proceeding.
   * - If required permissions are missing, logs an error and stops the service.
   * - Starts the service in the foreground with a notification if permissions are granted.
   *
   * @param intent The Intent supplied to `startService`, if any.
   * @param flags Additional data about this start request.
   * @param startId A unique integer representing this specific request to start.
   * @return `START_STICKY` if the service should continue running until explicitly stopped, or
   *   `START_NOT_STICKY` if it should not restart after being killed.
   *
   * @requiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
   */
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (!hasRequiredPermissions()) {
      Log.e("LocationService", "Missing permissions. Stopping service.")
      stopSelf() // Stop the service if permissions are missing
      return START_NOT_STICKY
    }
    startForeground(NOTIFICATION_ID, createNotification())
    return START_STICKY
  }

  /** A local binder for providing clients access to this service instance. */
  inner class LocalBinder : Binder() {
    fun getService(): LocationService = this@LocationService
  }

  /**
   * Binds the service to a client.
   *
   * @param intent The binding intent.
   * @return The binder instance for communication.
   */
  override fun onBind(intent: Intent?): IBinder {
    return binder
  }

  /**
   * Starts high-accuracy location updates and sets the initial location immediately.
   *
   * This function begins by retrieving the last known location or, if unavailable, requesting the
   * current location. The retrieved location is immediately passed to the provided callback
   * `onLocationUpdate`. Subsequently, it starts regular location updates to track the user's
   * movement in real-time.
   *
   * @param onLocationUpdate Callback invoked with the latest [Location].
   * @param onError Callback invoked with an error message in case of failure.
   * @return `true` if updates started successfully, `false` otherwise.
   */
  @SuppressLint("MissingPermission")
  override fun startLocationUpdates(
      onLocationUpdate: (Location) -> Unit,
      onError: ((String) -> Unit)?
  ): Boolean {
    this.onLocationUpdate = onLocationUpdate
    this.onError = onError
    try {
      val locationRequest =
          LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, REQUEST_INTERVAL_MS)
              .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
              .setWaitForAccurateLocation(true)
              .build()

      // Request the last known location immediately
      fusedLocationProviderClient.lastLocation
          .addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
              this.lastKnownLocation = lastLocation
              onLocationUpdate.invoke(lastLocation)
            } else {
              // In case there is no last known location, request an immediate update
              fusedLocationProviderClient
                  .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                  .addOnSuccessListener { currentLocation ->
                    if (currentLocation != null) {
                      this.lastKnownLocation = currentLocation
                      onLocationUpdate.invoke(currentLocation)
                    } else {
                      onError?.invoke("Failed to retrieve initial location.")
                    }
                  }
                  .addOnFailureListener { e ->
                    onError?.invoke("Error retrieving current location: ${e.message}")
                  }
            }
          }
          .addOnFailureListener { e ->
            onError?.invoke("Error retrieving last known location: ${e.message}")
          }

      // Start regular location updates
      fusedLocationProviderClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper())
      Log.d("LocationService", "Location updates started")
      return true
    } catch (e: Exception) {
      onError?.invoke("Error with location request: ${e.message}")
      return false
    }
  }

  /** Stops location updates to conserve resources and ends the foreground service. */
  override fun stopLocationUpdates() {
    // Remove the location callback to stop receiving updates
    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    onLocationUpdate = null
    Log.d("LocationService", "Location updates stopped")
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  /** Creates the notification channel required for Android Oreo and later. */
  internal fun createNotificationChannel() {
    val channel =
        NotificationChannel(
            NOTIFICATION_CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_LOW)
    val manager = getSystemService(NotificationManager::class.java)
    manager?.createNotificationChannel(channel)
  }

  /**
   * Creates a notification for the foreground service.
   *
   * @return A notification to be displayed while the service is running.
   */
  private fun createNotification(): Notification {
    return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.background_notification))
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setOngoing(true)
        .build()
  }

  /**
   * Checks whether the application has the necessary permissions to access location services.
   *
   * @return `true` if both `ACCESS_FINE_LOCATION` and `FOREGROUND_SERVICE_LOCATION` permissions are
   *   granted, `false` otherwise.
   *
   * @requiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
   */
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  internal open fun hasRequiredPermissions(): Boolean {
    val fineLocationPermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    val foregroundServicePermission =
        ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    return (fineLocationPermission && foregroundServicePermission)
  }
}
