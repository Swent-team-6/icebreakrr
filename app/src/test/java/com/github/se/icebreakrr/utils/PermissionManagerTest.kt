package com.github.se.icebreakrr.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionManagerTest {

  @Mock private lateinit var mockContext: Context

  @Mock private lateinit var mockActivity: ComponentActivity

  @Mock private lateinit var mockLauncher: ActivityResultLauncher<Array<String>>

  private lateinit var permissionManager: PermissionManager

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    `when`(mockContext.applicationContext).thenReturn(ApplicationProvider.getApplicationContext())
    permissionManager = PermissionManager(mockContext)
  }

  @After
  fun tearDown() {
    permissionManager.unregisterReceiver()
  }

  @Test
  fun requestPermissions_shouldLaunchPermissionRequest() {
    permissionManager.permissionLauncher = mockLauncher
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    permissionManager.requestPermissions(permissions)

    verify(mockLauncher).launch(permissions)
  }

  @Test
  fun updateAllPermissions_shouldUpdatePermissionStatus() = runBlocking {
    `when`(ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_GRANTED)

    permissionManager.updateAllPermissions()

    val status =
        permissionManager.permissionStatuses.value[Manifest.permission.ACCESS_FINE_LOCATION]
    assertEquals(PermissionStatus.Granted, status)
  }

  @Test
  fun updatePermissionStatus_shouldUpdateFlow() {
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    permissionManager.updatePermissionStatus(permission, PermissionStatus.Granted)

    assertEquals(PermissionStatus.Granted, permissionManager.permissionStatuses.value[permission])
  }

  @Test
  fun initialPermissionStatuses_shouldIncludeLocationPermission() {
    val initialStatuses = permissionManager.initialPermissionStatuses()

    assertEquals(
        PermissionStatus.Unknown, initialStatuses[Manifest.permission.ACCESS_FINE_LOCATION])
  }

  @Test
  fun unregisterReceiver_shouldUnregisterBroadcastReceiver() {
    permissionManager.unregisterReceiver()
    verify(mockContext).unregisterReceiver(any())
  }
}
