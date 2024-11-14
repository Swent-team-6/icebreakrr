package com.github.se.icebreakrr.utils

import ch.hsr.geohash.GeoHash

object GeoHashUtils {

  /**
   * Encodes latitude and longitude into a geohash string with the specified precision.
   *
   * @param latitude The latitude to encode.
   * @param longitude The longitude to encode.
   * @param precision The precision level of the geohash (higher values increase precision).
   * @return The geohash string representing the location.
   */
  fun encode(latitude: Double, longitude: Double, precision: Int): String {
    return GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, precision)
  }
}
