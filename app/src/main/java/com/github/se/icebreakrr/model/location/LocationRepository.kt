package com.github.se.icebreakrr.model.location

import com.google.firebase.firestore.GeoPoint

interface LocationRepository {
  /**
   * Sets the user's geographic location in Firestore.
   *
   * @param newLocation The new geographic location of the user as a `GeoPoint`.
   */
  fun setUserPosition(newLocation: GeoPoint)

  /** Removes the geohash field for the current user in Firestore. */
  fun removeUserGeohash()
}
