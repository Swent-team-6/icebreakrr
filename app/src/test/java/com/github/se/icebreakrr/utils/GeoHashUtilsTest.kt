package com.github.se.icebreakrr.utils

import ch.hsr.geohash.GeoHash
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoHashUtilsTest {

  @Test
  fun `encode returns correct geohash for given latitude, longitude, and precision`() {
    // Arrange
    val latitude = 37.7749 // San Francisco latitude
    val longitude = -122.4194 // San Francisco longitude
    val precision = 7 // Precision level

    // Act
    val result = GeoHashUtils.encode(latitude, longitude, precision)

    // Assert
    val expectedGeoHash =
        GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, precision)
    assertEquals(expectedGeoHash, result)
  }

  @Test
  fun `encode returns different geohashes for different locations`() {
    // Arrange
    val latitude1 = 37.7749
    val longitude1 = -122.4194
    val latitude2 = 40.7128 // New York latitude
    val longitude2 = -74.0060 // New York longitude
    val precision = 7

    // Act
    val result1 = GeoHashUtils.encode(latitude1, longitude1, precision)
    val result2 = GeoHashUtils.encode(latitude2, longitude2, precision)

    // Assert
    assert(result1 != result2) { "Geohashes should be different for distinct locations" }
  }

  @Test
  fun `encode returns shorter geohash with lower precision`() {
    // Arrange
    val latitude = 37.7749
    val longitude = -122.4194
    val highPrecision = 7
    val lowPrecision = 4

    // Act
    val highPrecisionResult = GeoHashUtils.encode(latitude, longitude, highPrecision)
    val lowPrecisionResult = GeoHashUtils.encode(latitude, longitude, lowPrecision)

    // Assert
    assert(highPrecisionResult.length > lowPrecisionResult.length) {
      "Geohash with higher precision should be longer than one with lower precision"
    }
  }
}
