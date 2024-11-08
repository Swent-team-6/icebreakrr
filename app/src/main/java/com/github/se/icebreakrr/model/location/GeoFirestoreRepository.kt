package com.github.se.icebreakrr.model.location

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.setLocation

class GeoFirestoreRepository {

  private val geoFirestore = GeoFirestore(FirebaseFirestore.getInstance().collection("profiles"))
  private val auth = FirebaseAuth.getInstance()

  /**
   * Updates the user's position in Firestore using GeoFirestore.
   *
   * This function retrieves the current user ID from the authentication instance and, if
   * authenticated, sets the user's position in Firestore to the specified `newLocation`. If the
   * update fails, an error message is logged.
   *
   * @param newLocation The new geographic location of the user, represented as a `GeoPoint`.
   */
  fun setUserPosition(newLocation: GeoPoint) {
    Log.d("GeoFirestoreRepository", "Before Auth verification.")
    val userId = auth.currentUser?.uid ?: return
    Log.d("GeoFirestoreRepository", "Setting user position..")
    geoFirestore.setLocation(userId, newLocation) { exception ->
      if (exception != null) {
        Log.e("GeoFirestoreRepository", "Failed to update user position in Firestore", exception)
      } else {
        Log.d("GeoFirestoreRepository", "User position updated successfully to $newLocation")
      }
    }
  }
}
