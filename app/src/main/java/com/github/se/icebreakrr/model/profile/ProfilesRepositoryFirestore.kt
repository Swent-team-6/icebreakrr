package com.github.se.icebreakrr.model.profile

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.github.se.icebreakrr.utils.GeoHashUtils
import com.github.se.icebreakrr.utils.NetworkUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow

class ProfilesRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ProfilesRepository {

  private val collectionPath = "profiles"

  private val _isWaiting = MutableStateFlow(false)
  override val isWaiting: MutableStateFlow<Boolean> = _isWaiting

  private val _waitingDone = MutableStateFlow(false)
  override val waitingDone: MutableStateFlow<Boolean> = _waitingDone

  override val connectionTimeOutMs: Long = 15000
  override val periodicTimeCheckWaitTime: Long = 5000

  val DEFAULT_RADIUS = 300.0
  val DEFAULT_LONGITUDE = 0.0
  val DEFAULT_LATITUDE = 0.0
  private val PERIOD = 1000

  // Generated with the help of CursorAI
  /**
   * Periodically checks the connection status by attempting to fetch profiles. If the connection
   * fails, it will retry every [periodicTimeCheckWaitTime] milliseconds (5 seconds by default).
   *
   * The check is performed by attempting to fetch profiles from Firestore. If successful, it means
   * the connection is restored and the waiting states are reset. If unsuccessful, it schedules
   * another check after the specified delay.
   *
   * @param onFailure Callback function that is invoked when a connection attempt fails. Takes an
   *   Exception as parameter.
   */
  override fun checkConnectionPeriodically(onFailure: (Exception) -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(
        object : Runnable {
          override fun run() {
            getProfilesInRadius(
                GeoPoint(DEFAULT_LONGITUDE, DEFAULT_LATITUDE),
                DEFAULT_RADIUS,
                onSuccess = { profiles ->
                  Log.e("Connection Check", "Connection restored")
                  _waitingDone.value = false
                  _isWaiting.value = false
                },
                onFailure = {
                  Log.e(
                      "Connection Check",
                      "Connection still lost, retrying in ${periodicTimeCheckWaitTime / PERIOD} seconds...")
                  handler.postDelayed(this, periodicTimeCheckWaitTime)
                })
          }
        },
        0)
  }

  // Generated with the help of CursorAI
  /**
   * Handles a connection failure by attempting one final connection retry after
   * [connectionTimeOutMs] milliseconds (15 seconds by default).
   *
   * The retry is performed by attempting to fetch profiles from Firestore:
   * - If successful: Resets the waiting states (_waitingDone and _isWaiting to false)
   * - If unsuccessful: Sets _waitingDone to true and calls the onFailure callback
   *
   * @param onFailure Callback function that is invoked if the final retry attempt fails. Takes an
   *   Exception as parameter.
   */
  override fun handleConnectionFailure(onFailure: (Exception) -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(
        {
          Log.e("Connection Check", "Retrying connection...")
          getProfilesInRadius(
              GeoPoint(DEFAULT_LONGITUDE, DEFAULT_LATITUDE),
              DEFAULT_RADIUS,
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
    auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  /**
   * Retrieves profiles within a specified radius from the center point.
   *
   * @param center The center point for the radius query.
   * @param radiusInMeters The radius around the center to retrieve profiles.
   * @param onSuccess A callback invoked with a list of profiles if the operation is successful.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun getProfilesInRadius(
      center: GeoPoint,
      radiusInMeters: Double,
      onSuccess: (List<Profile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Determine geohash precision based on radius
    val geohashPrecision = if (radiusInMeters <= 50) 7 else 6
    val centerGeohash = GeoHashUtils.encode(center.latitude, center.longitude, geohashPrecision)

    // Fetch profiles within the bounding geohashes
    db.collection(collectionPath)
        .whereGreaterThanOrEqualTo("geohash", centerGeohash)
        .whereLessThanOrEqualTo("geohash", centerGeohash + "\uf8ff")
        .get()
        .addOnSuccessListener { result ->
          waitingDone.value = false
          isWaiting.value = false
          val profiles = result.documents.mapNotNull { documentToProfile(it) }
          val profilesInRadius =
              profiles.filter { profile ->
                val profileLocation = profile.location ?: return@filter false
                calculateDistance(center, profileLocation) <= radiusInMeters
              }
          onSuccess(profilesInRadius)
        }
        .addOnFailureListener { e ->
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

  override fun getBlockedProfiles(
      blockedProfiles: List<String>,
      onSuccess: (List<Profile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (blockedProfiles.isEmpty()) {
      onSuccess(emptyList())
      return
    }
    db.collection(collectionPath)
        .whereIn("uid", blockedProfiles)
        .get()
        .addOnSuccessListener { result ->
          waitingDone.value = false
          isWaiting.value = false
          val profiles = result.documents.mapNotNull { documentToProfile(it) }
          onSuccess(profiles)
        }
        .addOnFailureListener { e ->
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
    val source =
        if (NetworkUtils.isNetworkAvailable()) {
          Source.SERVER
        } else {
          Source.CACHE
        }
    Log.d("ProfilesRepositoryFirestore", "getProfileByUid")
    db.collection("profiles").document(uid).get(source).addOnCompleteListener { task ->
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

      val gender =
          try {
            Gender.valueOf(genderString)
          } catch (e: IllegalArgumentException) {
            return null
          }

      val birthDate = document.getTimestamp("birthDate") ?: return null
      val catchPhrase = document.getString("catchPhrase") ?: return null
      val description = document.getString("description") ?: return null
      val tags = (document.get("tags") as? List<*>)?.filterIsInstance<String>() ?: listOf()
      val profilePictureUrl = document.getString("profilePictureUrl")
      val fcmToken = document.getString("fcmToken")
      val location = document.getGeoPoint("location")
      val geohash = document.getString("geohash")
      val hasBlocked =
          (document.get("hasBlocked") as? List<*>)?.filterIsInstance<String>() ?: listOf()
      val meetingRequestSent =
          (document.get("meetingRequestSent") as? List<*>)?.filterIsInstance<String>() ?: listOf()

      val meetingRequestInbox =
          (document.get("meetingRequestInbox") as? Map<*, *>)
              ?.filter { (key, value) -> key is String && value is String }
              ?.map { (key, value) -> key as String to value as String }
              ?.toMap() ?: mapOf()
      Profile(
          uid = uid,
          name = name,
          gender = gender,
          birthDate = birthDate,
          catchPhrase = catchPhrase,
          description = description,
          tags = tags,
          profilePictureUrl = profilePictureUrl,
          fcmToken = fcmToken,
          location = location,
          geohash = geohash,
          hasBlocked = hasBlocked,
          meetingRequestSent = meetingRequestSent,
          meetingRequestInbox = meetingRequestInbox)
    } catch (e: Exception) {
      Log.e("ProfileRepositoryFirestore", "Error converting document to Profile", e)
      null
    }
  }

  /**
   * Converts degrees to radians.
   *
   * @param deg Angle in degrees.
   * @return Angle in radians.
   */
  private fun deg2rad(deg: Double): Double = deg * Math.PI / 180.0

  /**
   * Calculates the distance between two geographic points using the Haversine formula.
   *
   * @param point1 First geographic point.
   * @param point2 Second geographic point.
   * @return Distance between the points in meters.
   */
  private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = deg2rad(point2.latitude - point1.latitude)
    val dLon = deg2rad(point2.longitude - point1.longitude)
    val lat1 = deg2rad(point1.latitude)
    val lat2 = deg2rad(point2.latitude)

    val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
  }
}
