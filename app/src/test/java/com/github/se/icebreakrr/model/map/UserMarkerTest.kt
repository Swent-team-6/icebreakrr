package com.github.se.icebreakrr.model.map

import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class UserMarkerTest {

  @Test
  fun testUserMarkerInitialization() {
    // Arrange
    val uid = "user123"
    val username = "John Doe"
    val location = LatLng(37.7749, -122.4194) // Example coordinates
    val locationDescription = "San Francisco" // Example location description
    val overlayPosition = null // Initially set to null

    // Act
    val userMarker = UserMarker(uid, username, locationDescription, location, overlayPosition)

    // Assert
    assertEquals("User ID should match", uid, userMarker.uid)
    assertEquals("Username should match", username, userMarker.username)
    assertEquals("Location should match", location, userMarker.location)
    assertEquals(
        "Location description should match", locationDescription, userMarker.locationDescription)
    assertEquals("Overlay position should be null", overlayPosition, userMarker.overlayPosition)
  }

  @Test
  fun testUserMarkerWithOverlayPosition() {
    // Arrange
    val uid = "user456"
    val username = "Jane Smith"
    val location = LatLng(34.0522, -118.2437) // Example coordinates
    val locationDescription = "Los Angeles" // Example location description
    val overlayPosition = Offset(10f, 20f) // Example overlay position

    // Act
    val userMarker = UserMarker(uid, username, locationDescription, location, overlayPosition)

    // Assert
    assertEquals("User ID should match", uid, userMarker.uid)
    assertEquals("Username should match", username, userMarker.username)
    assertEquals("Location should match", location, userMarker.location)
    assertEquals(
        "Location description should match", locationDescription, userMarker.locationDescription)
    assertEquals("Overlay position should match", overlayPosition, userMarker.overlayPosition)
  }

  @Test
  fun testUserMarkerEquality() {
    // Arrange
    val uid = "user789"
    val username = "Alice Johnson"
    val location = LatLng(40.7128, -74.0060) // Example coordinates
    val locationDescription = "New York City" // Example location description
    val userMarker1 = UserMarker(uid, username, locationDescription, location)
    val userMarker2 = UserMarker(uid, username, locationDescription, location)

    // Act & Assert
    assertEquals("UserMarkers should be equal", userMarker1, userMarker2)
  }

  @Test
  fun testUserMarkerInequality() {
    // Arrange
    val userMarker1 = UserMarker("user001", "Bob", "London", LatLng(51.5074, -0.1278))
    val userMarker2 = UserMarker("user002", "Charlie", "London", LatLng(51.5074, -0.1278))

    // Act & Assert
    assert(userMarker1 != userMarker2) // They should not be equal due to different UIDs
  }
}
