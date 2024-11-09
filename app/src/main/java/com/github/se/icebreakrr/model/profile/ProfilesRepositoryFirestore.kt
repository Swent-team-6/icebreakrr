package com.github.se.icebreakrr.model.profile

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow

class ProfilesRepositoryFirestore(private val db: FirebaseFirestore) : ProfilesRepository {

  private val collectionPath = "profiles"

  private val _isWaiting = MutableStateFlow(false)
  override val isWaiting: MutableStateFlow<Boolean> = _isWaiting

  private val _waitingDone = MutableStateFlow(false)
  override val waitingDone: MutableStateFlow<Boolean> = _waitingDone

  override val connectionTimeOutMs: Long = 15000
  override val periodicTimeCheckWaitTime: Long = 5000

  /**
   * Checks internet connection after a delay If still no connection after 15 seconds, updates
   * waitingDone state
   */
  override fun checkConnectionPeriodically(onFailure: (Exception) -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(
        object : Runnable {
          override fun run() {
            getProfilesInRadius(
                GeoPoint(0.0, 0.0),
                300.0,
                onSuccess = { profiles ->
                  Log.e("Connection Check", "Connection restored")
                  _waitingDone.value = false
                  _isWaiting.value = false
                },
                onFailure = {
                  Log.e(
                      "Connection Check",
                      "Connection still lost, retrying in ${periodicTimeCheckWaitTime/1000} seconds...")
                  handler.postDelayed(this, periodicTimeCheckWaitTime)
                })
          }
        },
        0)
  }

  private fun handleConnectionFailure(onFailure: (Exception) -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(
        {
          Log.e("Connection Check", "Retrying connection...")
          getProfilesInRadius(
              GeoPoint(0.0, 0.0),
              300.0,
              onSuccess = { profiles ->
                Log.e("Connection Check", "Connection restored")
                _waitingDone.value = false
                _isWaiting.value = false
              },
              onFailure = {
                Log.e("Connection Check", "Connection lost after retry")
                _waitingDone.value = true
                onFailure(Exception("Connection lost"))
              })
        },
        connectionTimeOutMs) // 15 secondes de délai avant de retenter la connexion
  }

  override fun updateIsWaiting(waiting: Boolean) {
    isWaiting.value = waiting
  }

  override fun updateWaitingDone(waiting: Boolean) {
    waitingDone.value = waiting
  }

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
        updateWaitingDone(false)
        updateIsWaiting(false)
      } else {
        result.exception?.let { e ->
          Log.e("ProfilesRepositoryFirestore", "Error getting profiles", e)
          if (e is com.google.firebase.firestore.FirebaseFirestoreException) {
            if (!_isWaiting.value && !_waitingDone.value) {
              _isWaiting.value = true
              handleConnectionFailure(onFailure)
            }
            onFailure(e)
          }
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
      val fcmToken = document.getString("fcmToken")

      // Create and return the Profile object
      Profile(
          uid = uid,
          name = name,
          gender = gender,
          birthDate = birthDate,
          catchPhrase = catchPhrase,
          description = description,
          tags = tags,
          profilePictureUrl = profilePictureUrl,
          fcmToken = fcmToken)
    } catch (e: Exception) {
      Log.e("ProfileRepositoryFirestore", "Error converting document to Profile", e)
      null
    }
  }
}
