package com.github.se.icebreakrr.model.location

import android.util.Log
import com.github.se.icebreakrr.utils.GeoHashUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class LocationRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : LocationRepository {

  private val collectionPath = "profiles"

  /**
   * Stores the user's position and geohash with high precision in Firestore.
   *
   * @param newLocation The new geographic location of the user as a `GeoPoint`.
   */
  override fun setUserPosition(newLocation: GeoPoint) {
    val userId = auth.currentUser?.uid
    if (userId == null) {
      Log.w("LocationRepositoryFirestore", "User is not authenticated. Cannot set location.")
      return
    }

    // Calculate the geohash with high precision for a 10-meter range
    val geohash = GeoHashUtils.encode(newLocation.latitude, newLocation.longitude, 10)

    // Data to store in Firestore
    val locationData = mapOf("geohash" to geohash, "location" to newLocation)

    // Update the location and geohash in Firestore
    db.collection(collectionPath)
        .document(userId)
        .update(locationData)
        .addOnSuccessListener {
          Log.d(
              "LocationRepositoryFirestore",
              "User location set to $newLocation with geohash $geohash")
        }
        .addOnFailureListener { exception ->
          Log.e("LocationRepositoryFirestore", "Failed to set location", exception)
        }
  }
}
