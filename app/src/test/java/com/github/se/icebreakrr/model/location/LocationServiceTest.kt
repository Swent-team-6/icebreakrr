package com.github.se.icebreakrr.model.location

import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LocationServiceTest {

  private lateinit var mockFusedLocationProviderClient: FusedLocationProviderClient
  private lateinit var locationService: LocationService

  @Before
  fun setUp() {
    mockFusedLocationProviderClient = mock()
    locationService = LocationService(mockFusedLocationProviderClient)
  }

  @Test
  fun `test startLocationUpdates starts updates successfully`() = runBlockingTest {
    // Arrange
    val locationRequest =
        LocationRequest.Builder(10000L)
            .setMinUpdateIntervalMillis(5000L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .build()

    val mockLocation =
        mock<Location> {
          on { latitude } doReturn 37.4219983
          on { longitude } doReturn -122.084
        }

    val mockTask: Task<Location> = Tasks.forResult(mockLocation)

    // Mock the getLastLocation to return a successful task
    whenever(mockFusedLocationProviderClient.lastLocation).thenReturn(mockTask)

    // Mock requestLocationUpdates to capture the callback
    whenever(
            mockFusedLocationProviderClient.requestLocationUpdates(
                any(), any<LocationCallback>(), eq(Looper.getMainLooper())))
        .thenAnswer { invocation ->
          val callback = invocation.arguments[1] as LocationCallback
          // Simulate location update immediately
          val locationResult = LocationResult.create(listOf(mockLocation))
          callback.onLocationResult(locationResult)
          null
        }

    var locationUpdated: Location? = null

    // Act
    val started =
        locationService.startLocationUpdates(
            onLocationUpdate = { location -> locationUpdated = location },
            onError = { error -> fail("Error callback invoked: $error") })

    // Assert
    assertTrue(started)
    assertNotNull(locationUpdated)
    locationUpdated?.latitude?.let { assertEquals(37.4219983, it, 0.0) }
    locationUpdated?.longitude?.let { assertEquals(-122.084, it, 0.0) }

    // Verify interactions
    verify(mockFusedLocationProviderClient)
        .requestLocationUpdates(any(), any<LocationCallback>(), eq(Looper.getMainLooper()))
  }

  @Test
  fun `test stopLocationUpdates stops updates successfully`() = runBlockingTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockFusedLocationProviderClient.removeLocationUpdates(any<LocationCallback>()))
        .thenReturn(mockTask)

    // Act
    locationService.stopLocationUpdates()

    // Assert
    verify(mockFusedLocationProviderClient).removeLocationUpdates(any<LocationCallback>())
  }

  @Test
  fun `test onBind returns binder`() {
    // Act
    val binder = locationService.onBind(null)

    // Assert
    assertNotNull(binder)
    assertTrue(binder is LocationService.LocalBinder)
    assertEquals(locationService, (binder as LocationService.LocalBinder).getService())
  }

  @Test
  fun `test onDestroy stops location updates`() = runBlockingTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockFusedLocationProviderClient.removeLocationUpdates(any<LocationCallback>()))
        .thenReturn(mockTask)

    // Act
    locationService.onDestroy()

    // Assert
    verify(mockFusedLocationProviderClient).removeLocationUpdates(any<LocationCallback>())
  }
}
