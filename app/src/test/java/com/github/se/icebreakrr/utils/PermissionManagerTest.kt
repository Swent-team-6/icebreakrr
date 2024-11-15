package com.github.se.icebreakrr.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
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
    permissionManager.initializeLauncher(
        mockActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), true)
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
    assertEquals(PackageManager.PERMISSION_GRANTED, status)
  }

  @Test
  fun updatePermissionStatus_shouldUpdateFlow() {
    `when`(ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_GRANTED)
    permissionManager.updateAllPermissions()

    val status =
        permissionManager.permissionStatuses.value[Manifest.permission.ACCESS_FINE_LOCATION]
    assertEquals(PackageManager.PERMISSION_GRANTED, status)
  }

  @Test
  fun initialPermissionStatuses_shouldIncludeLocationPermission() {
    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    assertNotNull(permissionManager.permissionStatuses.value[permission])
  }

  @Test
  fun updateAllPermissions_whenNoChange_shouldNotUpdateStatuses() = runBlocking {
    `when`(ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_DENIED)

    permissionManager.updateAllPermissions()

    val status =
        permissionManager.permissionStatuses.value[Manifest.permission.ACCESS_FINE_LOCATION]
    assertEquals(PackageManager.PERMISSION_DENIED, status)
  }

  @Test
  fun requestPermissions_updatesPermissionStatusOnGrant() = runBlocking {
    `when`(ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_GRANTED)

    permissionManager.permissionLauncher = mockLauncher
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    permissionManager.requestPermissions(permissions)

    // Simulate permission granted
    permissionManager.updateAllPermissions()
    val status =
        permissionManager.permissionStatuses.value[Manifest.permission.ACCESS_FINE_LOCATION]
    assertEquals(PackageManager.PERMISSION_GRANTED, status)
  }

  @Test
  fun requestPermissions_updatesPermissionStatusOnDenial() = runBlocking {
    `when`(ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_DENIED)

    permissionManager.permissionLauncher = mockLauncher
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    permissionManager.requestPermissions(permissions)

    // Simulate permission denied
    permissionManager.updateAllPermissions()
    val status =
        permissionManager.permissionStatuses.value[Manifest.permission.ACCESS_FINE_LOCATION]
    assertEquals(PackageManager.PERMISSION_DENIED, status)
  }
}
