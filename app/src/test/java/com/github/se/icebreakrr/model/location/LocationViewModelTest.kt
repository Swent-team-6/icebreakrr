package com.github.se.icebreakrr.model.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.icebreakrr.utils.PermissionManager
import com.google.firebase.firestore.GeoPoint
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LocationViewModelTest {

  @get:Rule var rule: TestRule = InstantTaskExecutorRule()

  @Mock private lateinit var locationService: LocationService

  @Mock private lateinit var geoFirestoreRepository: GeoFirestoreRepository

  @Mock private lateinit var permissionManager: PermissionManager

  private lateinit var locationViewModel: LocationViewModel

  // Use a test dispatcher
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher) // Configure Dispatchers.Main to use the testDispatcher

    locationViewModel =
        LocationViewModel(locationService, geoFirestoreRepository, permissionManager)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `tryToStartLocationUpdates should request permissions if not granted`() =
      runTest(testDispatcher) {
        val permissionFlow =
            MutableStateFlow(
                mapOf(Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_DENIED))
        whenever(permissionManager.permissionStatuses).thenReturn(permissionFlow)
        locationViewModel.tryToStartLocationUpdates()
        verify(permissionManager)
            .requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        assertFalse(locationViewModel.isUpdatingLocation.value)
      }

  @Test
  fun `tryToStartLocationUpdates should start updates if permission granted`() =
      runTest(testDispatcher) {
        val permissionFlow =
            MutableStateFlow(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_GRANTED))
        whenever(permissionManager.permissionStatuses).thenReturn(permissionFlow)

        locationViewModel.tryToStartLocationUpdates()
        verify(locationService).startLocationUpdates(anyOrNull(), anyOrNull())
      }

  @Test
  fun `stopLocationUpdates should stop location updates and set isUpdatingLocation to false`() =
      runTest(testDispatcher) {
        val permissionFlow =
            MutableStateFlow(
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_GRANTED))
        whenever(permissionManager.permissionStatuses).thenReturn(permissionFlow)
        whenever(locationService.startLocationUpdates(anyOrNull(), anyOrNull())).thenReturn(true)

        locationViewModel.tryToStartLocationUpdates()
        advanceUntilIdle()
        assertTrue(locationViewModel.isUpdatingLocation.value)

        // Reset the mock before the next verification to prevent double call count
        reset(locationService)

        locationViewModel.stopLocationUpdates()

        verify(locationService).stopLocationUpdates()
        assertFalse(locationViewModel.isUpdatingLocation.value)
      }

  @Test
  fun `location updates should start when permission is granted and stop when revoked`() =
      runTest(testDispatcher) {

        // At the beginning the permission is denied
        val permissionFlow =
            MutableStateFlow(
                mapOf(Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_DENIED))
        `when`(permissionManager.permissionStatuses).thenReturn(permissionFlow)

        // Try to start location updates, it should request the permission and set
        // isUpdatingLocation to false
        whenever(locationService.startLocationUpdates(anyOrNull(), anyOrNull())).thenReturn(false)
        locationViewModel.tryToStartLocationUpdates()
        verify(permissionManager)
            .requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        assertFalse(locationViewModel.isUpdatingLocation.value)

        // Give the permission, it should call startLocationUpdates thanks to the
        // permissionObserverJob
        whenever(locationService.startLocationUpdates(anyOrNull(), anyOrNull())).thenReturn(true)
        permissionFlow.value =
            mapOf(Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_GRANTED)
        advanceUntilIdle()
        verify(locationService).startLocationUpdates(anyOrNull(), anyOrNull())
        assertTrue(locationViewModel.isUpdatingLocation.value)

        // Reset the mock before the next verification to prevent double call count
        reset(locationService)

        // Remove the permission, it should call stopLocationUpdates thanks to the
        // permissionObserverJob
        permissionFlow.value =
            mapOf(Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_DENIED)
        advanceUntilIdle()
        verify(locationService).stopLocationUpdates()
        assertFalse(locationViewModel.isUpdatingLocation.value)
      }

  @Test
  fun `startServiceLocationUpdates should update Firestore on location change`() = runTest {
    val permissionFlow =
        MutableStateFlow(
            mapOf(Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_GRANTED))
    whenever(permissionManager.permissionStatuses).thenReturn(permissionFlow)

    // Captor for the `onLocationUpdate` callback
    val locationUpdateCaptor = argumentCaptor<(Location) -> Unit>()

    // Creating a simulated location
    val location =
        mock(Location::class.java).apply {
          whenever(latitude).thenReturn(40.7128)
          whenever(longitude).thenReturn(-74.0060)
        }

    // Call `tryToStartLocationUpdates` to initiate `startServiceLocationUpdates`
    locationViewModel.tryToStartLocationUpdates()

    // Check that `startLocationUpdates` is called and capture the `onLocationUpdate` callback
    verify(locationService).startLocationUpdates(locationUpdateCaptor.capture(), anyOrNull())

    // Invoke the captured callback with the simulated location
    locationUpdateCaptor.firstValue.invoke(location)

    // Capture the `GeoPoint` sent to Firestore
    val geoPointCaptor = argumentCaptor<GeoPoint>()
    verify(geoFirestoreRepository).setUserPosition(geoPointCaptor.capture())

    // Checking coordinates of `GeoPoint`
    assertEquals(40.7128, geoPointCaptor.firstValue.latitude)
    assertEquals(-74.0060, geoPointCaptor.firstValue.longitude)
  }
}
