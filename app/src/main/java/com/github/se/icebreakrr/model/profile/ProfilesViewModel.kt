package com.github.se.icebreakrr.model.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

open class ProfilesViewModel(
    private val repository: ProfilesRepository,
    private val ppRepository: ProfilePicRepository
) : ViewModel() {

  private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
  open val profiles: StateFlow<List<Profile>> = _profiles

  private val _filteredProfiles = MutableStateFlow<List<Profile>>(emptyList())
  val filteredProfiles: StateFlow<List<Profile>> = _filteredProfiles

  private val _selectedProfile = MutableStateFlow<Profile?>(null)
  open val selectedProfile: StateFlow<Profile?> = _selectedProfile

  private val _loading = MutableStateFlow(false)
  open val loading: StateFlow<Boolean> = _loading

  private val _error = MutableStateFlow<Exception?>(null)
  val error: StateFlow<Exception?> = _error

  private val _tempProfilePictureBitmap = MutableStateFlow<Bitmap?>(null)
  val tempProfilePictureBitmap: StateFlow<Bitmap?> = _tempProfilePictureBitmap

  var _isConnected = MutableStateFlow(true)
  var isConnected: StateFlow<Boolean> = _isConnected

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfilesViewModel(
                ProfilesRepositoryFirestore(Firebase.firestore),
                ProfilePicRepositoryStorage(Firebase.storage))
                as T
          }
        }
  }

  /**
   * Returns a new unique profile ID.
   *
   * @return A unique profile ID as a String.
   */
  fun getNewProfileId(): String {
    return repository.getNewProfileId()
  }

  /** Initializes the repository and fetches profiles. */
  init {
    repository.init {
      // Fetch profiles on initialization
      getFilteredProfilesInRadius(GeoPoint(0.0, 0.0), 300.0)
    }
  }

  /**
   * Fetches profiles within a given radius around a center point with optional filters.
   *
   * @param center The center location as a GeoPoint.
   * @param radiusInMeters The radius around the center in meters to search for profiles.
   * @param genders Optional filter for a list of genders.
   * @param ageRange Optional filter for age range.
   * @param tags Optional filter for specific tags.
   */
  fun getFilteredProfilesInRadius(
      center: GeoPoint,
      radiusInMeters: Double,
      genders: List<Gender>? = null,
      ageRange: IntRange? = null,
      tags: List<String>? = null
  ) {
    _loading.value = true
    repository.getProfilesInRadius(
        center = center,
        radiusInMeters = radiusInMeters,
        onSuccess = { profileList ->
          val filteredProfiles =
              profileList.filter { profile ->

                // Filter by genders if specified
                (genders == null || profile.gender in genders || genders.isEmpty()) &&

                    // Filter by age range if specified
                    (ageRange == null || profile.calculateAge() in ageRange) &&

                    // Filter by tags if specified
                    (tags == null ||
                        profile.tags.any { it.lowercase() in tags.map { it.lowercase() } } ||
                        tags.isEmpty())
              }
          _profiles.value = profileList
          _filteredProfiles.value = filteredProfiles
          _loading.value = false
          _isConnected.value = true
        },
        onFailure = { e ->
          Log.e("ConnectionCheck", "Firebase Request FAILED")
          Log.e(
              "ConnectionCheck",
              "Current state: waiting=${repository.isWaiting.value}, done=${repository.waitingDone.value}")
          handleError(e)
          if (_isConnected.value && repository.waitingDone.value) {
            _isConnected.value = false
            repository.checkConnectionPeriodically({})
          }
        })
  }

  /**
   * Adds a new profile to the repository.
   *
   * @param profile The profile to be added.
   */
  fun addNewProfile(profile: Profile) {
    _loading.value = true
    repository.addNewProfile(
        profile, onSuccess = { _loading.value = false }, onFailure = { e -> handleError(e) })
  }

  /**
   * Updates an existing profile in the repository.
   *
   * @param profile The profile with updated information.
   */
  fun updateProfile(profile: Profile) {
    _loading.value = true
    repository.updateProfile(
        profile, onSuccess = { _loading.value = false }, onFailure = { e -> handleError(e) })
  }

  /**
   * Fetches a profile by its user ID (UID).
   *
   * @param uid The unique ID of the user whose profile is being retrieved.
   */
  fun getProfileByUid(uid: String) {
    _loading.value = true
    repository.getProfileByUid(
        uid,
        onSuccess = { profile ->
          _selectedProfile.value = profile
          _loading.value = false
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Deletes a profile by its user ID (UID).
   *
   * @param uid The unique ID of the user whose profile is to be deleted.
   */
  fun deleteProfileByUid(uid: String) {
    _loading.value = true
    repository.deleteProfileByUid(
        uid, onSuccess = { _loading.value = false }, onFailure = { e -> handleError(e) })
    ppRepository.deleteProfilePicture(
        userId = uid, onSuccess = {}, onFailure = { e -> handleError(e) })
  }

  /**
   * Uploads the current user's profile picture to the remote storage system.
   *
   * @param imageData The byte array of the image file to be uploaded.
   * @throws IllegalStateException if the user is not logged in.
   */
  fun uploadCurrentUserProfilePicture(imageData: ByteArray) {
    val userId = selectedProfile.value?.uid ?: throw IllegalStateException("User not logged in")
    ppRepository.uploadProfilePicture(
        userId = userId,
        imageData = imageData,
        onSuccess = { url ->
          // _selectedProfile cannot be null here, as it must be set to current user before calling
          // this function
          _selectedProfile.update { selected -> selected!!.copy(profilePictureUrl = url) }
          updateProfile(selectedProfile.value!!)
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Generates a temporary profile picture Bitmap from an image URI.
   *
   * @param context The context used to access the content resolver.
   * @param imageUri The URI of the image to be converted.
   */
  fun generateTempProfilePictureBitmap(context: Context, imageUri: Uri) {
    val bitmap = imageUriToBitmap(context, imageUri)
    _tempProfilePictureBitmap.value = bitmap
  }

  /** Clears the temporary profile picture Bitmap. */
  fun clearTempProfilePictureBitmap() {
    _tempProfilePictureBitmap.value = null
  }

  /**
   * Validates and uploads the profile picture if a temporary profile picture Bitmap exists.
   *
   * @param context The context used to show a Toast message in case of failure.
   */
  fun validateAndUploadProfilePicture(context: Context) {
    val imageData =
        bitmapToJpgByteArray(
            tempProfilePictureBitmap.value ?: return) // do nothing if null (no selected image)
    if (imageData != null) {
      uploadCurrentUserProfilePicture(imageData)
      clearTempProfilePictureBitmap()
    } else {
      Toast.makeText(context, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
      clearTempProfilePictureBitmap()
    }
  }

  /**
   * Deletes the current user's profile picture from the remote storage system.
   *
   * @throws IllegalStateException if the user is not logged in.
   */
  fun deleteCurrentUserProfilePicture() {
    ppRepository.deleteProfilePicture(
        userId = selectedProfile.value?.uid ?: throw IllegalStateException("User not logged in"),
        onSuccess = {
          _selectedProfile.update { currentProfile ->
            currentProfile?.copy(profilePictureUrl = null)
          }
          updateProfile(selectedProfile.value!!)
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Converts an image URI to a processed Bitmap, cropping the image to a square at the center.
   *
   * @param context The context used to access the content resolver.
   * @param imageUri The URI of the image to be converted.
   * @param maxResolution The maximum resolution for the output Bitmap. Default is 600.
   * @return A Bitmap representing the processed image, or null if an error occurs.
   */
  private fun imageUriToBitmap(context: Context, imageUri: Uri, maxResolution: Int = 600): Bitmap? {
    return try {
      // Open an InputStream from the URI
      val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

      // Decode InputStream to Bitmap
      val bitmap = BitmapFactory.decodeStream(inputStream)
      inputStream?.close() // Close the InputStream after decoding

      // Calculate the dimensions for the square crop
      val dimension = minOf(bitmap.width, bitmap.height)
      val xOffset = (bitmap.width - dimension) / 2
      val yOffset = (bitmap.height - dimension) / 2

      // Crop the Bitmap to a square at the center
      val croppedBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, dimension, dimension)

      // Resize the cropped bitmap if it exceeds the maximum resolution
      if (dimension > maxResolution) {
        val scale = maxResolution.toFloat() / dimension
        Bitmap.createScaledBitmap(
            croppedBitmap,
            (croppedBitmap.width * scale).toInt(),
            (croppedBitmap.height * scale).toInt(),
            true)
      } else {
        croppedBitmap
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * Converts a Bitmap to a JPEG byte array.
   *
   * @param bitmap The Bitmap to be converted.
   * @param quality The quality of the JPEG compression (0-100). Default is 100.
   * @return A byte array representing the JPEG image, or null if an error occurs.
   */
  private fun bitmapToJpgByteArray(bitmap: Bitmap, quality: Int = 100): ByteArray? {
    return try {
      // Compress Bitmap to JPEG format and store in ByteArrayOutputStream
      val byteArrayOutputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

      // Return ByteArray of compressed JPEG image (representing .jpg)
      byteArrayOutputStream.toByteArray()
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * Handles errors by updating the error and loading states.
   *
   * @param exception The exception to log and indicate the error state.
   */
  private fun handleError(exception: Exception) {
    _error.value = exception
    _loading.value = false
  }
}
