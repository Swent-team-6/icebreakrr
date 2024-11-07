package com.github.se.icebreakrr.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PermissionManager(private val context: Context) {

  lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
  private val _permissionStatuses =
      MutableStateFlow<Map<String, PermissionStatus>>(initialPermissionStatuses())
  val permissionStatuses: StateFlow<Map<String, PermissionStatus>> = _permissionStatuses

  private val scope = CoroutineScope(Dispatchers.Main)

  // BroadcastReceiver to detect permission changes
  private val permissionReceiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          updateAllPermissions()
        }
      }

  /**
   * Initializes the `PermissionManager` by registering a `BroadcastReceiver` to listen for
   * configuration changes and periodically checks permission statuses every 5 seconds to keep them
   * up to date.
   */
  init {
    // Register the BroadcastReceiver
    context.registerReceiver(permissionReceiver, IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED))
    // Periodically check permissions to keep the state up to date
    scope.launch {
      while (true) {
        updateAllPermissions()
        kotlinx.coroutines.delay(5000) // Check every 5 seconds
      }
    }
  }

  /** Cleans up resources when the manager is no longer used. */
  fun unregisterReceiver() {
    context.unregisterReceiver(permissionReceiver)
  }

  /**
   * Initializes the launcher to ask for permissions. Called from an Activity to configure the
   * launcher.
   */
  fun initializeLauncher(activity: ComponentActivity) {
    permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
          permissions.forEach { (permission, isGranted) ->
            val status = if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied
            updatePermissionStatus(permission, status)
          }
        }
  }

  /**
   * Requests specific permissions using the launcher.
   *
   * @param permissions The permissions to request.
   */
  fun requestPermissions(permissions: Array<String>) {
    permissionLauncher.launch(permissions)
  }

  /** Updates all permissions by checking their current state. */
  fun updateAllPermissions() {

    val updatedStatuses =
        _permissionStatuses.value.keys.associateWith { permission ->
          val isGranted =
              ContextCompat.checkSelfPermission(context, permission) ==
                  PackageManager.PERMISSION_GRANTED
          if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied
        }
    _permissionStatuses.value = updatedStatuses

    Log.d("Permissions", "Current permissions: $updatedStatuses")
  }

  /** Updates the state of a permission in the flow and emits a change. */
  fun updatePermissionStatus(permission: String, status: PermissionStatus) {
    _permissionStatuses.value =
        _permissionStatuses.value.toMutableMap().apply { this[permission] = status }
  }

  // TODO Save the map of the permissions with the DataStore class instead of that
  /** Returns initial permissions including ACCESS_FINE_LOCATION. */
  fun initialPermissionStatuses(): Map<String, PermissionStatus> {
    return mapOf(Manifest.permission.ACCESS_FINE_LOCATION to PermissionStatus.Unknown)
  }
}

/** Enum representing possible states of a permission. */
enum class PermissionStatus {
  Unknown,
  Granted,
  Denied
}
