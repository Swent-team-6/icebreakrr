package com.github.se.icebreakrr.model.location

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.utils.IPermissionManager
import com.github.se.icebreakrr.utils.PermissionManager
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel(
    private val locationService: ILocationService,
    private val locationRepositoryFirestore: LocationRepository,
    private val permissionManager: IPermissionManager
) : ViewModel() {

  private val _isUpdatingLocation = MutableStateFlow(false)
  val isUpdatingLocation: StateFlow<Boolean>
    get() = _isUpdatingLocation

  private val _lastKnownLocation = MutableStateFlow<GeoPoint?>(null)
  val lastKnownLocation: StateFlow<GeoPoint?>
    get() = _lastKnownLocation

  private var permissionObserverJob: Job? = null

  companion object {
    fun provideFactory(
        locationService: LocationService,
        locationRepositoryFirestore: LocationRepositoryFirestore,
        permissionManager: PermissionManager
    ): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
              return LocationViewModel(
                  locationService, locationRepositoryFirestore, permissionManager)
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
   * Initiates location updates from `LocationService` and updates Firestore with the user's
   * position.
   *
   * Converts each new location into a `GeoPoint` and saves it in Firestore through
   * `geoFirestoreRepository`. Sets `_isUpdatingLocation` to `true` if updates start successfully,
   * or `false` if an error occurs.
   */
  private fun startServiceLocationUpdates() {
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
