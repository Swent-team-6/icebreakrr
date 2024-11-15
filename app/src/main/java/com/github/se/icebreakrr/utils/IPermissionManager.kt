package com.github.se.icebreakrr.utils

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.StateFlow

interface IPermissionManager {
  val permissionStatuses: StateFlow<Map<String, Int>>

  fun initializeLauncher(
      activity: ComponentActivity,
      permissions: Array<String>,
      isTest: Boolean = false
  )

  fun requestPermissions(permissions: Array<String>)

  fun updateAllPermissions()
}
