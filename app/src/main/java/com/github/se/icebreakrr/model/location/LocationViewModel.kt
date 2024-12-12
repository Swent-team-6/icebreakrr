package com.github.se.icebreakrr.model.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.utils.IPermissionManager
import com.github.se.icebreakrr.utils.PermissionManager
import com.google.firebase.firestore.GeoPoint
import java.lang.ref.WeakReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel(
    private val locationService: ILocationService,
    private val locationRepositoryFirestore: LocationRepository,
    private val permissionManager: IPermissionManager,
    context: Context
) : ViewModel() {

  private val contextRef = WeakReference(context)

  private val _isUpdatingLocation = MutableStateFlow(false)
  val isUpdatingLocation: StateFlow<Boolean>
    get() = _isUpdatingLocation

  private val _lastKnownLocation = MutableStateFlow<GeoPoint?>(null)
  val lastKnownLocation: StateFlow<GeoPoint?>
    get() = _lastKnownLocation

  private var permissionObserverJob: Job? = null

  /**
   * Companion object to provide a factory for creating instances of LocationViewModel.
   *
   * This factory simplifies the creation of LocationViewModel by injecting the required
   * dependencies:
   * - LocationService
   * - LocationRepositoryFirestore
   * - PermissionManager
   * - Context
   */
  companion object {
    fun provideFactory(
        locationService: LocationService,
        locationRepositoryFirestore: LocationRepositoryFirestore,
        permissionManager: PermissionManager,
        context: Context
    ): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
              return LocationViewModel(
                  locationService, locationRepositoryFirestore, permissionManager, context)
                  as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }

  /**
   * Initializes the ViewModel by restoring the last known location.
   *
   * The `init` block ensures that the `restoreLastKnownLocation` function is called when the
   * ViewModel is created, setting up the initial state for location tracking.
   */
  init {
    restoreLastKnownLocation()
  }

  /**
   * Starts location updates if `ACCESS_FINE_LOCATION` permission is granted.
   *
   * Checks if location updates are already active. If not, verifies permission status:
   * - Requests permission if not granted.
   * - Starts location updates if permission is granted.
   *
   * Begins observing permission changes to manage updates accordingly.
   */
  fun tryToStartLocationUpdates() {
    if (_isUpdatingLocation.value) return
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val permissionStatus = permissionManager.permissionStatuses.value[permission]
    if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
      permissionManager.requestPermissions(arrayOf(permission))
    } else startServiceLocationUpdates()
    if (permissionObserverJob == null) observePermissions()
  }

  /** Stops location updates and stops observing permission status changes. */
  fun stopLocationUpdates() {
    if (_isUpdatingLocation.value) {
      locationService.stopLocationUpdates()
      stopObservingPermissions()
      _isUpdatingLocation.value = false
    }
  }

  /**
   * Starts the location updates service to track the user's location in the background.
   *
   * This function ensures that the `Context` is available before starting the service. It
   * initializes the location updates by starting the `LocationService` in the foreground. On
   * receiving location updates, the user's position is updated in Firestore and stored as the last
   * known location.
   *
   * If the service fails to start, it stops any ongoing location updates and sets the internal
   * state accordingly.
   *
   * @throws IllegalStateException If the `Context` is no longer available.
   */
  private fun startServiceLocationUpdates() {
    val context = contextRef.get()
    if (context == null) {
      Log.e("LocationViewModel", "Context is no longer available, unable to start service.")
      return
    }
    val serviceIntent = Intent(context, LocationService::class.java)
    ContextCompat.startForegroundService(context, serviceIntent)

    val locationUpdatesStarted =
        locationService.startLocationUpdates(
            onLocationUpdate = { location ->
              val geoPoint = GeoPoint(location.latitude, location.longitude)
              locationRepositoryFirestore.setUserPosition(geoPoint)
              _lastKnownLocation.value = geoPoint

              viewModelScope.launch {
                try {
                  (locationRepositoryFirestore as? AppDataStore)?.saveLastKnownLocation(geoPoint)
                } catch (e: Exception) {
                  Log.e("LocationUpdate", "Failed to save location", e)
                }
              }
            })

    if (locationUpdatesStarted) {
      _isUpdatingLocation.value = true
    } else {
      locationService.stopLocationUpdates()
      _isUpdatingLocation.value = false
    }
  }

  /**
   * Observes permission statuses and manages location updates accordingly.
   *
   * Starts `startServiceLocationUpdates` if location permission is granted and not currently
   * updating; calls `stopLocationUpdates` if permission is revoked while updating.
   */
  private fun observePermissions() {
    stopObservingPermissions() // Cancel any existing observer job
    permissionObserverJob =
        viewModelScope.launch {
          permissionManager.permissionStatuses.collect { statuses ->
            val locationPermissionStatus = statuses[Manifest.permission.ACCESS_FINE_LOCATION]
            when (locationPermissionStatus) {
              PackageManager.PERMISSION_GRANTED -> {
                // Start location updates if permission is granted
                if (!_isUpdatingLocation.value) tryToStartLocationUpdates()
              }
              else -> {
                // Stop location updates if permission is revoked
                if (_isUpdatingLocation.value) {
                  locationService.stopLocationUpdates()
                  _isUpdatingLocation.value = false
                }
              }
            }
          }
        }
  }

  /** Stops observing permission status changes. */
  private fun stopObservingPermissions() {
    permissionObserverJob?.takeIf { it.isActive }?.cancel()
    permissionObserverJob = null
  }

  /**
   * Restores the last known location from the data store and updates the internal state.
   *
   * This function launches a coroutine in the ViewModel scope to observe changes in the
   * `lastKnownLocation` from the `AppDataStore` and updates `_lastKnownLocation`.
   */
  private fun restoreLastKnownLocation() {
    viewModelScope.launch {
      (locationRepositoryFirestore as? AppDataStore)?.lastKnownLocation?.collect { location ->
        _lastKnownLocation.value = location
      }
    }
  }
}
