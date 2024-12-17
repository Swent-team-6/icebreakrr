package com.github.se.icebreakrr.model.profile

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.github.se.icebreakrr.utils.GeoHashUtils
import com.github.se.icebreakrr.utils.NetworkUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
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

  val DEFAULT_RADIUS = 300
  val DEFAULT_LONGITUDE = 0.0
  val DEFAULT_LATITUDE = 0.0
  private val PERIOD = 1000
  private val UID = "uid"

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
      radiusInMeters: Int,
      onSuccess: (List<Profile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Determine the precision of the geohash based on the radius
    val geohashPrecision = getGeohashPrecision(radiusInMeters)
    val centerGeohash = GeoHashUtils.encode(center.latitude, center.longitude, geohashPrecision)

    // Get profiles in geohash range
    db.collection(collectionPath)
        .whereGreaterThanOrEqualTo("geohash", centerGeohash)
        .whereLessThanOrEqualTo("geohash", centerGeohash + "\uf8ff")
        .get()
        .addOnSuccessListener { result ->
          waitingDone.value = false
          isWaiting.value = false

          val profiles = result.documents.mapNotNull { documentToProfile(it) }

          // Filter profiles within the specified radius and add their distanceToSelfProfile
          val profilesInRadius =
              profiles.mapNotNull { profile ->
                val profileLocation = profile.location
                if (profileLocation != null) {
                  val distance = calculateDistance(center, profileLocation)
                  if (distance <= radiusInMeters) {
                    profile.copy(distanceToSelfProfile = distance.toInt())
                  } else {
                    null // Exclude profiles outside the radius
                  }
                } else {
                  null // Exclude profiles without location
                }
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
          }
          onFailure(e)
        }
  }

  /**
   * Retrive multiple profiles, given a list of UID
   *
   * @param uidList: a list of UID
   * @param onSuccess: A callback invoked with a list of profiles if the operation is successful.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  override fun getMultipleProfiles(
      uidList: List<String>,
      onSuccess: (List<Profile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (uidList.isEmpty()) {
      onSuccess(emptyList())
      return
    }
    db.collection(collectionPath)
        .whereIn(UID, uidList)
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
   * Determines the geohash precision based on the specified radius. The precision dynamically
   * adjusts to optimize coverage and performance:
   * - Higher precision for smaller radii (more detailed grid).
   * - Lower precision for larger radii (coarser grid).
   *
   * @param radiusInMeters The search radius in meters.
   * @return The appropriate geohash precision as an integer (1 to 9).
   */
  private fun getGeohashPrecision(radiusInMeters: Int): Int {
    return when {
      radiusInMeters <= 1 -> 9 // Ultra-precise (~5 m x 5 m)
      radiusInMeters <= 5 -> 8 // Very high precision (~19 m x 19 m)
      radiusInMeters <= 20 -> 7 // High precision (~152 m x 152 m)
      radiusInMeters <= 100 -> 6 // Moderate precision (~600 m x 600 m)
      radiusInMeters <= 1000 -> 5 // Medium precision (~4.9 km x 4.9 km)
      radiusInMeters <= 10000 -> 4 // Low precision (~39 km x 19.5 km)
      radiusInMeters <= 50000 -> 3 // Broad region (~156 km x 156 km)
      radiusInMeters <= 250000 -> 2 // Very broad region (~1,250 km x 625 km)
      else -> 1 // Extremely coarse precision (~5,000 km x 5,000 km)
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
      val hasAlreadyMet =
          (document.get("hasAlreadyMet") as? List<*>)?.filterIsInstance<String>() ?: listOf()
      val reports =
          ((document.get("reports") as? HashMap<*, *>)
                  ?.filter { (key, value) -> key is String && value is String }
                  ?.map { (key, value) -> key as String to reportType.valueOf(value as String) }
                  ?: listOf())
              .associate { it.first to it.second }
      val meetingRequestSent =
          (document.get("meetingRequestSent") as? List<*>)?.filterIsInstance<String>() ?: listOf()
      val meetingRequestInbox =
          (document.get("meetingRequestInbox") as? Map<String, Map<Any, Any>>)
              ?.mapNotNull { (key, value) ->
                val messagePair = (value["first"] as? Map<String, String>)
                val message1 = messagePair?.get("first") ?: ""
                val message2 = messagePair?.get("second") ?: ""
                val loc = (value["second"] as? Map<String, Any>)
                val latitude = (loc?.get("first") as? Double)
                val longitude = (loc?.get("second") as? Double)
                if (latitude == null || longitude == null)
                    throw Exception("Could not retrieve location from chosen location")
                key to Pair(Pair(message1, message2), Pair(latitude, longitude))
              }
              ?.toMap() ?: mapOf()
      val meetingRequestChosenLocation =
          (document.get("meetingRequestChosenLocalisation") as? Map<String, Map<String, Any>>)
              ?.mapNotNull { (key, value) ->
                val message = (value["first"] as? String)
                val loc = (value["second"] as? Map<String, Any>)
                val latitude = (loc?.get("first") as? Double)
                val longitude = (loc?.get("second") as? Double)
                if (latitude == null || longitude == null)
                    throw Exception("Could not retrieve location from chosen location")
                key to Pair(message ?: "", Pair(latitude, longitude))
              }
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
          hasAlreadyMet = hasAlreadyMet,
          reports = reports,
          meetingRequestSent = meetingRequestSent,
          meetingRequestInbox = meetingRequestInbox,
          meetingRequestChosenLocalisation = meetingRequestChosenLocation)
    } catch (e: Exception) {
      Log.e("ProfileRepositoryFirestore", "Error converting document to Profile", e)
      null
    }
  }

  /**
   * Calculates the distance between two geographic points using the Android Location class.
   *
   * @param point1 First geographic point.
   * @param point2 Second geographic point.
   * @return Distance between the points in meters.
   */
  private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
    val location1 =
        Location("").apply {
          latitude = point1.latitude
          longitude = point1.longitude
        }
    val location2 =
        Location("").apply {
          latitude = point2.latitude
          longitude = point2.longitude
        }
    return location1.distanceTo(location2).toDouble() // Returns distance in meters
  }
}
