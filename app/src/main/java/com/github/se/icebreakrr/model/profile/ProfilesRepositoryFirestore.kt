package com.github.se.icebreakrr.model.profile

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class ProfilesRepositoryFirestore(private val db: FirebaseFirestore) : ProfilesRepository {

  private val collectionPath = "profiles"

  /**
   * Generates a new unique profile ID from Firestore.
   *
   * @return A new unique profile ID as a String.
   */
  override fun getNewProfileId(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Initializes the repository by setting an authentication state listener.
   *
   * @param onSuccess A callback that is invoked when the user is authenticated.
   */
  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  /**
   * Temporary implementation: Retrieves all profiles in the Firestore database.
   *
   * @param center The center point for the radius query (currently unused).
   * @param radiusInMeters The radius around the center (currently unused).
   * @param onSuccess A callback invoked with a list of profiles if the operation is successful.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun getProfilesInRadius(
      center: GeoPoint,
      radiusInMeters: Double,
      onSuccess: (List<Profile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // TODO Add GeoFirestore functionality
    val task = db.collection(collectionPath).get(com.google.firebase.firestore.Source.SERVER)

    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        val profiles =
            result.result?.documents?.mapNotNull { document -> documentToProfile(document) }
                ?: emptyList()

        onSuccess(profiles)
      } else {
        result.exception?.let { e ->
          Log.e("ProfilesRepositoryFirestore", "Error getting profiles", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Adds a new profile to Firestore.
   *
   * @param profile The profile to add.
   * @param onSuccess A callback invoked if the operation is successful.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun addNewProfile(
      profile: Profile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  /**
   * Updates an existing profile in Firestore.
   *
   * @param profile The profile to update.
   * @param onSuccess A callback invoked if the operation is successful.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun updateProfile(
      profile: Profile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(profile.uid).set(profile), onSuccess, onFailure)
  }

  /**
   * Retrieves a profile from Firestore by its UID.
   *
   * @param uid The UID of the profile to retrieve.
   * @param onSuccess A callback invoked with the profile if the operation is successful, or null if
   *   no profile is found.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun getProfileByUid(
      uid: String,
      onSuccess: (Profile?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("ProfilesRepositoryFirestore", "getProfileByUid")
    db.collection("profiles").document(uid).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val profile = task.result?.let { document -> documentToProfile(document) }
        onSuccess(profile)
      } else {
        task.exception?.let { e ->
          Log.e("ProfilesRepositoryFirestore", "Error getting profile", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Deletes a profile from Firestore by its UID.
   *
   * @param uid The UID of the profile to delete.
   * @param onSuccess A callback invoked if the operation is successful.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun deleteProfileByUid(
      uid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(uid).delete(), onSuccess, onFailure)
  }

  /**
   * Performs a Firestore operation and calls the appropriate callback based on the result.
   *
   * @param task The Firestore task to perform.
   * @param onSuccess The callback to call if the operation is successful.
   * @param onFailure The callback to call if the operation fails.
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("TodosRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Converts a Firestore document to a Profile object.
   *
   * @param document The Firestore document to convert.
   * @return The Profile object, or null if conversion fails.
   */
  private fun documentToProfile(document: DocumentSnapshot): Profile? {
    return try {
      val uid = document.id
      val name = document.getString("name") ?: return null
      val genderString = document.getString("gender") ?: return null

      // Fallback to null if gender is not a valid enum value
      val gender =
          try {
            Gender.valueOf(genderString)
          } catch (e: IllegalArgumentException) {
            return null // Return null if the gender does not match any enum
          }
      val birthDate = document.getTimestamp("birthDate") ?: return null
      val catchPhrase = document.getString("catchPhrase") ?: return null
      val description = document.getString("description") ?: return null
      val tags = (document.get("tags") as? List<*>)?.filterIsInstance<String>() ?: listOf()
      val profilePictureUrl = document.getString("profilePictureUrl") // Nullable field

      // Create and return the Profile object
      Profile(
          uid = uid,
          name = name,
          gender = gender,
          birthDate = birthDate,
          catchPhrase = catchPhrase,
          description = description,
          tags = tags,
          profilePictureUrl = profilePictureUrl)
    } catch (e: Exception) {
      Log.e("ProfileRepositoryFirestore", "Error converting document to Profile", e)
      null
    }
  }
}
