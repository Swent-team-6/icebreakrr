package com.github.se.icebreakrr.model.location

import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationServiceTest {

  @Mock private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

  @Mock private lateinit var locationResult: LocationResult

  @Mock private lateinit var location: Location

  @Captor private lateinit var locationCallbackCaptor: ArgumentCaptor<LocationCallback>

  private lateinit var locationService: LocationService

  @Before
  fun setup() {

    // Mock the FusedLocationProviderClient to use in the test
    fusedLocationProviderClient = mock(FusedLocationProviderClient::class.java)

    // Explicitly initialize other mocks
    locationResult = mock(LocationResult::class.java)
    location = mock(Location::class.java) // Explicit initialization of `location`

    // Explicit initialization of the captor
    locationCallbackCaptor = ArgumentCaptor.forClass(LocationCallback::class.java)

    // Inject the context and the mocked location client into LocationService
    locationService = LocationService(fusedLocationProviderClient)
  }

  @Test
  fun `test startLocationUpdates calls onLocationUpdate with new location`() {
    val onLocationUpdate: (Location) -> Unit = mock()
    val onError: (String) -> Unit = mock()

    // Start location updates
    locationService.startLocationUpdates(onLocationUpdate, onError)

    // Capture the LocationCallback passed to fusedLocationProviderClient
    verify(fusedLocationProviderClient)
        .requestLocationUpdates(any(), locationCallbackCaptor.capture(), eq(Looper.getMainLooper()))

    val locationCallback = locationCallbackCaptor.value

    // Simulate a location result with a valid location
    `when`(locationResult.lastLocation).thenReturn(location)
    locationCallback.onLocationResult(locationResult)

    // Verify that the onLocationUpdate callback is called with the simulated location
    verify(onLocationUpdate).invoke(location) // Ensure the `invoke` method is called
  }

  @Test
  fun `test startLocationUpdates calls onError when location unavailable`() {
    val onLocationUpdate: (Location) -> Unit = mock()
    val onError: (String) -> Unit = mock()

    locationService.startLocationUpdates(onLocationUpdate, onError)

    verify(fusedLocationProviderClient)
        .requestLocationUpdates(any(), locationCallbackCaptor.capture(), eq(Looper.getMainLooper()))

    val locationCallback = locationCallbackCaptor.value

    val locationAvailability = mock(LocationAvailability::class.java)
    `when`(locationAvailability.isLocationAvailable).thenReturn(false)
    locationCallback.onLocationAvailability(locationAvailability)

    verify(onError).invoke("Location service is currently unavailable.")
  }

  @Test
  fun `test stopLocationUpdates removes location callback`() {
    locationService.startLocationUpdates(mock(), mock())
    locationService.stopLocationUpdates()

    verify(fusedLocationProviderClient).removeLocationUpdates(any<LocationCallback>())
  }

  @Test
  fun `test startLocationUpdates handles ApiException`() {
    val onLocationUpdate: (Location) -> Unit = mock()
    val onError: (String) -> Unit = mock()

    // Simulate a RuntimeException instead of ApiException
    doThrow(RuntimeException("Simulated exception"))
        .`when`(fusedLocationProviderClient)
        .requestLocationUpdates(
            any(LocationRequest::class.java), // Explicit type for LocationRequest
            any(LocationCallback::class.java), // Explicit type for LocationCallback
            any(Looper::class.java) // Explicit type for Looper
            )

    locationService.startLocationUpdates(onLocationUpdate, onError)

    // Verify that the onError callback is called with the error message
    verify(onError).invoke("Unexpected error occurred: Simulated exception")
  }

  @Test
  fun `test startLocationUpdates does not update if location difference is below threshold`() {
    val onLocationUpdate: (Location) -> Unit = mock()
    val onError: (String) -> Unit = mock()

    // Start location updates
    locationService.startLocationUpdates(onLocationUpdate, onError)

    // Capture the LocationCallback passed to fusedLocationProviderClient
    verify(fusedLocationProviderClient)
        .requestLocationUpdates(
            anyOrNull(), locationCallbackCaptor.capture(), eq(Looper.getMainLooper()))

    val locationCallback = locationCallbackCaptor.value

    // Simulate a location result with the initial location
    `when`(locationResult.lastLocation).thenReturn(location)
    `when`(location.distanceTo(location)).thenReturn(5.0f) // Distance less than the threshold

    // Simulate the first location update
    locationCallback.onLocationResult(locationResult)
    verify(onLocationUpdate).invoke(location) // First update should invoke the callback

    // Reset the onLocationUpdate mock to track only the second update
    reset(onLocationUpdate)

    // Simulate a second location result with minimal movement
    locationCallback.onLocationResult(locationResult)

    // Verify that the onLocationUpdate callback is not called again
    verify(onLocationUpdate, never()).invoke(anyOrNull())
  }

  @Test
  fun `test startLocationUpdates updates immediately if lastKnownLocation is null`() {
    val onLocationUpdate: (Location) -> Unit = mock()
    val onError: (String) -> Unit = mock()

    // Start location updates
    locationService.startLocationUpdates(onLocationUpdate, onError)

    // Capture the LocationCallback
    verify(fusedLocationProviderClient)
        .requestLocationUpdates(
            anyOrNull(), locationCallbackCaptor.capture(), eq(Looper.getMainLooper()))

    val locationCallback = locationCallbackCaptor.value

    // Simulate a location result with a new location
    `when`(locationResult.lastLocation).thenReturn(location)
    locationCallback.onLocationResult(locationResult)

    // Verify that onLocationUpdate is called immediately since lastKnownLocation is initially null
    verify(onLocationUpdate).invoke(location)
  }
}
