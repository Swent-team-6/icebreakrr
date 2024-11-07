package com.github.se.icebreakrr.model.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayOutputStream
import java.io.InputStream

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
   * Returns a new unique profile ID
   *
   * @return A unique profile ID as a String.
   */
  fun getNewProfileId(): String {
    return repository.getNewProfileId()
  }

  /** Initializes the repository, fetch profiles */
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
        },
        onFailure = { e -> handleError(e) })
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
  fun uploadCurrentUserProfilePicture(imageData: ByteArray, onSuccess: (url: String?) -> Unit) {
    val userId = selectedProfile.value?.uid ?: throw IllegalStateException("User not logged in")
    ppRepository.uploadProfilePicture(
        userId = userId,
        imageData = imageData,
        onSuccess = { url ->
          onSuccess(url)
        },
        onFailure = { e -> handleError(e) })
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
        },
        onFailure = { e -> handleError(e) })
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

    fun imageUriToJpgByteArray(context: Context, imageUri: Uri, quality: Int = 100): ByteArray? {
    return try {
        // open an InputStream from the URI
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

        // decode InputStream to Bitmap
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close() // close the InputStream after decoding

        // todo: delegate image cropping to another function
        // calculate the dimensions for the square crop
        val dimension = minOf(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - dimension) / 2
        val yOffset = (bitmap.height - dimension) / 2

        // crop the Bitmap to a square at the center
        val croppedBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, dimension, dimension)

        // compress Bitmap to JPEG format and store in ByteArrayOutputStream
        val byteArrayOutputStream = ByteArrayOutputStream()
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // return ByteArray of compressed JPEG image (representing .jpg)
        byteArrayOutputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
}
