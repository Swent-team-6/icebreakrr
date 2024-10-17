package com.github.se.icebreakrr.model.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class ProfilesViewModel(private val repository: ProfilesRepository) : ViewModel() {

  private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
  val profiles: StateFlow<List<Profile>> = _profiles

  private val _selectedProfile = MutableStateFlow<Profile?>(null)
  val selectedProfile: StateFlow<Profile?> = _selectedProfile

  private val _loading = MutableStateFlow(false)
  val loading: StateFlow<Boolean> = _loading

  private val _error = MutableStateFlow<Exception?>(null)
  val error: StateFlow<Exception?> = _error

  init {
    repository.init { // TODO getProfilesInRadius}
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfilesViewModel(ProfilesRepositoryFirestore(Firebase.firestore)) as T
          }
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
                (genders == null || profile.gender in genders) &&

                    // Filter by age range if specified
                    (ageRange == null || profile.calculateAge() in ageRange) &&

                    // Filter by tags if specified
                    (tags == null || profile.tags.containsAll(tags))
              }
          _profiles.value = filteredProfiles
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
