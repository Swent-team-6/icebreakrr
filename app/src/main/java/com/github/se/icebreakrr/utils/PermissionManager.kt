package com.github.se.icebreakrr.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionManager(private val context: Context) {

  lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

  // StateFlow to emit the current status of permissions
  private val _permissionStatuses = MutableStateFlow<Map<String, Int>>(emptyMap())
  val permissionStatuses: StateFlow<Map<String, Int>> = _permissionStatuses

  /**
   * Initializes the `PermissionManager`, setting up the permissions to manage. Should be called
   * from an Activity to configure the permissions and launcher.
   *
   * @param activity The `ComponentActivity` for launching permission requests.
   * @param permissions The list of permissions to be managed.
   */
  fun initializeLauncher(
      activity: ComponentActivity,
      permissions: Array<String>,
      isTest: Boolean = false
  ) {
    // Initialize the permission statuses map with the provided permissions
    _permissionStatuses.value = permissions.associateWith { PackageManager.PERMISSION_DENIED }

    if (!isTest) {
      // Set up the launcher to request permissions from the user
      permissionLauncher =
          activity.registerForActivityResult(
              ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val updatedStatuses = _permissionStatuses.value.toMutableMap()
                result.forEach { (permission, isGranted) ->
                  updatedStatuses[permission] =
                      if (isGranted) PackageManager.PERMISSION_GRANTED
                      else PackageManager.PERMISSION_DENIED
                }
                _permissionStatuses.value = updatedStatuses
              }
    }

    // Initial check to update statuses based on current permission state
    updateAllPermissions()
  }

  /**
   * Launches a request for the specified permissions.
   *
   * @param permissions The array of permissions to request from the user.
   */
  fun requestPermissions(permissions: Array<String>) {
    permissionLauncher.launch(permissions)
  }

  /**
   * Updates the statuses of all managed permissions based on their current states. Only updates the
   * state if there is a change to avoid redundant emissions.
   */
  fun updateAllPermissions() {
    val updatedStatuses = _permissionStatuses.value.toMutableMap()
    var hasChanges = false

    // Check each permission's status and update only if there's a change
    _permissionStatuses.value.forEach { (permission, currentStatus) ->
      val newStatus = ContextCompat.checkSelfPermission(context, permission)
      if (currentStatus != newStatus) {
        updatedStatuses[permission] = newStatus
        hasChanges = true
      }
    }

    // Update StateFlow if any permission status has changed
    if (hasChanges) {
      _permissionStatuses.value = updatedStatuses
      Log.d("Permissions", "Permissions updated: $updatedStatuses")
    } else {
      Log.d("Permissions", "Permissions unchanged: $updatedStatuses")
    }
  }
}
