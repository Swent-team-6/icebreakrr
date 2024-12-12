package com.github.se.icebreakrr.model.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.icebreakrr.model.profile.ProfilesViewModel.ProfilePictureState.TO_DELETE
import com.github.se.icebreakrr.ui.sections.DEFAULT_RADIUS
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LATITUDE
import com.github.se.icebreakrr.ui.sections.DEFAULT_USER_LONGITUDE
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

private val MEETING_REQUEST_MAX_RADIUS = 500
private val MAX_RESOLUTION = 600
private val DEFAULT_QUALITY = 100
private val MAX_REPORTS_BEFORE_BAN = 2

open class ProfilesViewModel(
    private val repository: ProfilesRepository,
    private val ppRepository: ProfilePicRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

  private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
  open val profiles: StateFlow<List<Profile>> = _profiles

  private val _messagingProfiles = MutableStateFlow<List<Profile>>(emptyList())
  open val messagingProfiles: StateFlow<List<Profile>> = _profiles

  private val _inboxProfiles = MutableStateFlow<List<Profile?>>(emptyList())
  private val _sentProfiles = MutableStateFlow<List<Profile?>>(emptyList())

  private val _inboxItems =
      MutableStateFlow<Map<Profile, Pair<Pair<String, String>, Pair<Double, Double>>>>(emptyMap())
  open val inboxItems: StateFlow<Map<Profile, Pair<Pair<String, String>, Pair<Double, Double>>>> =
      _inboxItems

  private val _sentItems = MutableStateFlow<List<Profile>>(emptyList())
  open val sentItems: StateFlow<List<Profile>> = _sentItems

  private val _filteredProfiles = MutableStateFlow<List<Profile>>(emptyList())
  val filteredProfiles: StateFlow<List<Profile>> = _filteredProfiles

  private val _cancellationMessageProfile = MutableStateFlow<List<Profile>>(emptyList())

  private val _selectedProfile = MutableStateFlow<Profile?>(null)
  open val selectedProfile: StateFlow<Profile?> = _selectedProfile

  private val _selfProfile = MutableStateFlow<Profile?>(null)
  open var selfProfile: StateFlow<Profile?> = _selfProfile

  // This stores the state of the modification during the profile edition (null if not in edition)
  private val _editedCurrentProfile = MutableStateFlow<Profile?>(null)
  open val editedCurrentProfile: StateFlow<Profile?> = _editedCurrentProfile

  // This stores the state of the modification during the profile edition
  private val _pictureChangeState = MutableStateFlow(ProfilePictureState.UNCHANGED)
  open val pictureChangeState: StateFlow<ProfilePictureState> = _pictureChangeState

  private val _loading = MutableStateFlow(false)
  open val loading: StateFlow<Boolean> = _loading

  private val _loadingSelf = MutableStateFlow(false)
  open val loadingSelf: StateFlow<Boolean> = _loadingSelf

  private val _error = MutableStateFlow<Exception?>(null)
  val error: StateFlow<Exception?> = _error

  private val _tempProfilePictureBitmap = MutableStateFlow<Bitmap?>(null)
  val tempProfilePictureBitmap: StateFlow<Bitmap?> = _tempProfilePictureBitmap

  private val _isConnected = MutableStateFlow(true)
  open var isConnected: StateFlow<Boolean> = _isConnected

  private val _pendingLocalisations = MutableStateFlow<List<Profile>>(emptyList())
  open var pendingLocalisations: StateFlow<List<Profile>> = _pendingLocalisations

  private val _chosenLocalisations =
      MutableStateFlow<Map<Profile, Pair<String, Pair<Double, Double>>>>(emptyMap())
  open var chosenLocalisations: StateFlow<Map<Profile, Pair<String, Pair<Double, Double>>>> =
      _chosenLocalisations

  fun updateIsConnected(boolean: Boolean) {
    _isConnected.value = boolean
    repository.checkConnectionPeriodically({})
  }

  companion object {
    class Factory(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) :
        ViewModelProvider.Factory {

      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
          return ProfilesViewModel(
              ProfilesRepositoryFirestore(firestore, auth),
              ProfilePicRepositoryStorage(Firebase.storage),
              auth)
              as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
      }
    }
  }

  /** Represents the state of a profile picture change. */
  enum class ProfilePictureState {
    UNCHANGED,
    TO_UPLOAD,
    TO_DELETE
  }

  /**
   * Updates the profile picture change state.
   *
   * @param state The new state of the profile picture change.
   */
  fun setPictureChangeState(state: ProfilePictureState) {
    _pictureChangeState.value = state
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
      val defaultPoint = GeoPoint(DEFAULT_USER_LATITUDE, DEFAULT_USER_LONGITUDE)
      getSelfProfile {
        getFilteredProfilesInRadius(defaultPoint, DEFAULT_RADIUS)
        getMessagingRadiusProfile(defaultPoint)
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
      radiusInMeters: Int,
      genders: List<Gender>? = null,
      ageRange: IntRange? = null,
      tags: List<String>? = null
  ) {
    _loading.value = true
    repository.getProfilesInRadius(
        center = center,
        radiusInMeters = radiusInMeters,
        onSuccess = { profileList ->
          val currentUserId = _selfProfile.value?.uid ?: ""
          val filteredProfiles =
              profileList.filter { profile ->
                // Exclude the currently logged-in user's profile
                profile.uid != currentUserId &&

                    // Filter users than have too many reports
                    (profile.reports.keys.size <= MAX_REPORTS_BEFORE_BAN) &&

                    // Filter by genders if specified
                    (genders == null || profile.gender in genders || genders.isEmpty()) &&

                    // Filter by age range if specified
                    (ageRange == null || profile.calculateAge() in ageRange) &&

                    // Filter by tags if specified
                    (tags == null ||
                        profile.tags.any { it.lowercase() in tags.map { it.lowercase() } } ||
                        tags.isEmpty()) &&

                    // Filter by hasBlocked
                    !(_selfProfile.value?.hasBlocked?.contains(profile.uid) ?: false) &&

                    // Filter by isBlocked
                    !(profile.hasBlocked.contains(currentUserId)) &&
                    !(profile.hasBlocked.contains(_selfProfile.value?.uid ?: "")) &&

                    // Filter by hasAlreadyMet
                    !(_selfProfile.value?.hasAlreadyMet?.contains(profile.uid) ?: false) &&

                    // Filter by how you have reported
                    profile.reports[currentUserId] == null
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
   * Fetches profiles within the messaging max radius around a certain point
   *
   * @param center: The center location as a GeoPoint.
   */
  fun getMessagingRadiusProfile(center: GeoPoint) {
    _loading.value = true
    repository.getProfilesInRadius(
        center = center,
        radiusInMeters = MEETING_REQUEST_MAX_RADIUS,
        onSuccess = { profileList ->
          val currentUserId = _selfProfile.value?.uid ?: ""
          val filteredProfiles = profileList.filter { profile -> profile.uid != currentUserId }
          _messagingProfiles.value = filteredProfiles
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
   * @param onComplete : callback to avoid racing conditions
   * @param onFailure: callback to propagate erorrs higher
   */
  fun updateProfile(profile: Profile, onComplete: () -> Unit, onFailure: (Exception) -> Unit) {
    _loading.value = true
    _selfProfile.value = profile
    repository.updateProfile(
        profile,
        onSuccess = {
          _loading.value = false
          onComplete()
        },
        onFailure = { e ->
          handleError(e)
          onFailure(e)
        })
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
   * Fetches a profile by its user ID (UID), and then runs the rest
   *
   * @param uid The unique ID of the user whose profile is being retrieved.
   * @param andThen The rest of the code to run
   */
  fun getProfileByUidAndThen(uid: String, andThen: () -> Unit) {
    _loading.value = true
    repository.getProfileByUid(
        uid,
        onSuccess = { profile ->
          _selectedProfile.value = profile
          _loading.value = false
          andThen()
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
   * @param imageData The byte array representing the image data.
   * @param onSuccess A callback function that is invoked when the upload is successful.
   */
  private fun uploadCurrentUserProfilePicture(
      imageData: ByteArray,
      onSuccess: (url: String?) -> Unit
  ) {
    val userId = _selfProfile.value?.uid ?: throw IllegalStateException("User not logged in")
    ppRepository.uploadProfilePicture(
        userId = userId,
        imageData = imageData,
        onSuccess = onSuccess,
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

  /**
   * Saves the edited profile to the state.
   *
   * @param profile The profile to be saved.
   */
  fun saveEditedProfile(profile: Profile) {
    _editedCurrentProfile.value = profile
  }

  /** Clears the temporary profile picture Bitmap. */
  fun clearTempProfilePictureBitmap() {
    _tempProfilePictureBitmap.value = null
  }

  /** Clears the edited profile from the state. */
  private fun clearEditedProfile() {
    _editedCurrentProfile.value = null
  }

  /** Resets every state related to profile edition. */
  fun resetProfileEditionState() {
    clearTempProfilePictureBitmap()
    clearEditedProfile()
    _pictureChangeState.value = ProfilePictureState.UNCHANGED
  }

  /**
   * Processes the cropped image by compressing it if it is too large.
   *
   * @param bitmap The cropped Bitmap to be processed.
   */
  fun processCroppedImage(bitmap: Bitmap) {
    _tempProfilePictureBitmap.value = compressImageIfTooLarge(bitmap)
    _pictureChangeState.value = ProfilePictureState.TO_UPLOAD
  }

  /**
   * Validates the profile changes and updates both the image and the profile repository.
   *
   * @param context The context used to show a Toast message in case of failure.
   */
  fun validateProfileChanges(context: Context) {
    when (_pictureChangeState.value) {
      ProfilePictureState.TO_UPLOAD ->
          validateAndUploadProfilePicture(context) { url ->
            val newProfile = _editedCurrentProfile.value!!.copy(profilePictureUrl = url)
            updateProfile(newProfile, {}, {})
            _selfProfile.value = newProfile
            // todo: "_selectedProfile.value = newProfile" needs to be removed when all the other
            // personal profile views are updated to use the selfProfile instead of the
            // selectedProfile
            _selectedProfile.value = newProfile

            resetProfileEditionState()
          }
      TO_DELETE ->
          deleteCurrentUserProfilePicture {
            val newProfile = _editedCurrentProfile.value?.copy(profilePictureUrl = null)
            updateProfile(newProfile!!, {}, {})
            _selfProfile.value = newProfile
            _selectedProfile.value = newProfile // todo: same as above
            resetProfileEditionState()
          }
      else -> {
        updateProfile(_editedCurrentProfile.value!!, {}, {})
        _selfProfile.value = _editedCurrentProfile.value
        _selectedProfile.value = _editedCurrentProfile.value // todo: same as above
        resetProfileEditionState()
      }
    }
  }

  /**
   * Validates and uploads the profile picture to the remote storage system.
   *
   * @param context The context used to show a Toast message in case of failure.
   * @param onSuccess A callback function that is invoked when the upload is successful.
   */
  fun validateAndUploadProfilePicture(context: Context, onSuccess: (url: String?) -> Unit) {
    val imageData = bitmapToJpgByteArray(tempProfilePictureBitmap.value ?: return)
    if (imageData != null) {
      uploadCurrentUserProfilePicture(imageData, onSuccess)
    } else {
      Toast.makeText(context, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * Deletes the current user's profile picture from the remote storage system.
   *
   * @param onSuccess A callback function that is invoked when the deletion is successful.
   */
  private fun deleteCurrentUserProfilePicture(onSuccess: () -> Unit) {
    ppRepository.deleteProfilePicture(
        userId = _selfProfile.value?.uid ?: throw IllegalStateException("User not logged in"),
        onSuccess = onSuccess,
        onFailure = { e -> handleError(e) })
  }

  /**
   * Blocks a user by updating the blocking relationship in the repository.
   *
   * @param uid The unique ID of the user being blocked.
   */
  fun blockUser(uid: String) {
    val updatedProfile =
        selfProfile.value?.copy(
            hasBlocked = selfProfile.value?.hasBlocked?.plus(uid)?.toSet()?.toList() ?: emptyList())
    updateProfile(
        updatedProfile!!,
        {
          _selfProfile.update { currentProfile ->
            currentProfile?.copy(hasBlocked = currentProfile.hasBlocked.plus(uid))
          }
        },
        {})
  }

  /**
   * Unblocks a user by updating the blocking relationship in the repository.
   *
   * @param uid The unique ID of the user being unblocked.
   */
  fun unblockUser(uid: String) {
    _selfProfile.update { currentProfile ->
      currentProfile?.copy(hasBlocked = currentProfile.hasBlocked.filter { it != uid })
    }
    updateProfile(selfProfile.value!!, { getBlockedUsers() }, {})
  }

  /**
   * Reports a user by updating its reports Map with the selfProfile Uid and the specified reason
   *
   * @param profile the profile to report.
   * @param reason the reason for reporting.
   */
  fun reportUser(reason: reportType) {
    if (_selectedProfile.value != null) {
      _selectedProfile.update { currentProfile ->
        currentProfile?.copy(
            reports = currentProfile.reports.plus(_selfProfile.value!!.uid to reason))
      }
      updateProfile(_selectedProfile.value!!, {}, {})
    }
  }

  /** Fetches all the blocked users */
  fun getBlockedUsers() {
    _loading.value = true
    repository.getMultipleProfiles(
        selfProfile.value?.hasBlocked ?: emptyList(),
        onSuccess = { profileList ->
          _profiles.value = profileList
          _loading.value = false
          _isConnected.value = true
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Adds user to current user's already met list in the repository.
   *
   * @param uid The unique ID of the user being added.
   */
  fun addAlreadyMet(uid: String) {
    _selfProfile.update { currentProfile ->
      currentProfile?.copy(hasAlreadyMet = currentProfile.hasAlreadyMet + uid)
    }
    updateProfile(selfProfile.value!!, {}, {})
  }

  /**
   * Removes user to current user's already met list in the repository.
   *
   * @param uid The unique ID of the user being removed.
   */
  fun removeAlreadyMet(uid: String) {
    _selfProfile.update { currentProfile ->
      currentProfile?.copy(hasAlreadyMet = currentProfile.hasAlreadyMet.filter { it != uid })
    }
    updateProfile(selfProfile.value!!, { getBlockedUsers() }, {})
  }

  /** Gets the Already Met users */
  fun getAlreadyMetUsers() {
    _loading.value = true
    repository.getMultipleProfiles(
        selfProfile.value?.hasAlreadyMet ?: emptyList(),
        onSuccess = { profileList ->
          _profiles.value = profileList
          _loading.value = false
          _isConnected.value = true
        },
        onFailure = { e -> handleError(e) })
  }
  /**
   * Fetches all the profiles that send a message to our profile
   *
   * @param inboxUserUid: The list of UID of the profiles that have sent a message to our user inbox
   * @param onComplete : callback to avoid racing conditions
   */
  private fun getInboxUsers(inboxUserUid: List<String>, onComplete: () -> Unit) {
    _loading.value = true
    repository.getMultipleProfiles(
        inboxUserUid,
        onSuccess = { profileList ->
          _inboxProfiles.value = profileList
          _loading.value = false
          _isConnected.value = true
          onComplete()
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Function that get a list of uids in the database and update the observable variable
   * _pendingLocalisations
   *
   * @param pendingUserUid : list of users to fetch from database
   * @param onComplete : callback function to avoid race conditions
   */
  private fun getPendingLocationUsers(pendingUserUid: List<String>, onComplete: () -> Unit) {
    _loading.value = true
    repository.getMultipleProfiles(
        pendingUserUid,
        onSuccess = { profileList ->
          _pendingLocalisations.value = profileList
          _loading.value = false
          _isConnected.value = true
          onComplete()
        },
        onFailure = { e -> handleError(e) })
  }
  /**
   * Function that get a list of uids in the database and update the observable variable
   * _¨chosenLocations
   *
   * @param inboxUserUid : list of users to fetch from database with the message and localisation
   *   they have sent
   */
  private fun getChosenLocationsUsers(
      inboxUserUid: Map<String, Pair<String, Pair<Double, Double>>>,
  ) {
    _loading.value = true
    repository.getMultipleProfiles(
        inboxUserUid.map { it.key },
        onSuccess = { profileList ->
          val profileLookup = profileList.associateBy { it.uid }
          _chosenLocalisations.value =
              inboxUserUid
                  .mapNotNull { (uid, coordinates) ->
                    val profile = profileLookup[uid]
                    if (profile != null) {
                      profile to coordinates
                    } else {
                      null
                    }
                  }
                  .toMap()
          _loading.value = false
          _isConnected.value = true
        },
        onFailure = { e -> handleError(e) })
  }
  /**
   * Fetches all the users to which we sent messages to
   *
   * @param sentUserUid: The list of uid of the users we sent a meeting request to
   * @param onComplete : callback to avoir racing conditions
   */
  private fun getSentUsers(sentUserUid: List<String>, onComplete: () -> Unit) {
    _loading.value = true
    repository.getMultipleProfiles(
        sentUserUid,
        onSuccess = { profileList ->
          _sentProfiles.value = profileList
          _loading.value = false
          _isConnected.value = true
          onComplete()
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Fetches all the users that our profile has been in contact with (received or sent messages)
   *
   * @param onComplete : callback to avoid racing conditions
   */
  fun getMessageCancellationUsers(onComplete: () -> Unit) {
    _loading.value = true
    val allContactedUsers =
        (selfProfile.value?.meetingRequestInbox?.map { it.key } ?: listOf()) +
            (selfProfile.value?.meetingRequestSent ?: listOf())
    repository.getMultipleProfiles(
        allContactedUsers,
        onSuccess = { profileList ->
          _cancellationMessageProfile.value = profileList
          _loading.value = false
          _isConnected.value = true
          onComplete()
        },
        onFailure = { e -> handleError(e) })
  }

  /**
   * Get the inbox of our user
   *
   * @param onComplete : callback to avoid race conditions
   */
  fun getInboxOfSelfProfile(onComplete: () -> Unit) {
    val inboxUidList = selfProfile.value?.meetingRequestInbox
    val sentUidList = selfProfile.value?.meetingRequestSent
    if (inboxUidList != null && sentUidList != null) {
      val uidsMessageList = inboxUidList.toList()
      val uidsList = uidsMessageList.map { it.first }
      val messageList = uidsMessageList.map { it.second }
      getInboxUsers(uidsList) {
        _inboxItems.value = _inboxProfiles.value.filterNotNull().zip(messageList).toMap()
        getSentUsers(sentUidList) {
          _sentItems.value = _sentProfiles.value.filterNotNull()
          onComplete()
        }
      }
    }
  }

  /** function that fetches the profiles in the database from the local self profile */
  fun getChosenLocationsUsers() {
    val chosenLocationsUid = selfProfile.value?.meetingRequestChosenLocalisation
    if (chosenLocationsUid != null) {
      getChosenLocationsUsers(chosenLocationsUid)
    }
  }

  /**
   * remove a chosen locations (called when you already met a person after having decided of a
   * meeting)
   *
   * @param uid : uid of the user you have met
   */
  fun removeChosenLocalisation(uid: String) {
    updateProfile(
        _selfProfile.value?.copy(
            meetingRequestChosenLocalisation =
                _selfProfile.value?.meetingRequestChosenLocalisation?.filter { it.key != uid }
                    ?: emptyMap())!!,
        {},
        {})
  }

  /**
   * called when we have selected the meeting point after the request has been accepted. It adds a
   * new mapping in the chosen localisation and remove the user from the pending locations
   *
   * @param uid : uid with whom you confirmed the location
   * @param loc : contains the message and the localisation chosen,
   * @param onComplete : callback to avoid racing conditions
   * @param onFailure : callback to propagate errors
   */
  fun confirmMeetingRequest(
      uid: String,
      loc: Pair<String, Pair<Double, Double>>,
      onComplete: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    updateProfile(
        _selfProfile.value?.copy(
            meetingRequestChosenLocalisation =
                _selfProfile.value?.meetingRequestChosenLocalisation?.plus(uid to loc)
                    ?: emptyMap())!!,
        { onComplete() },
        { onFailure(it) })
  }

  /**
   * Fetches the current user's profile from the repository.
   *
   * @param onComplete : callback to avoid racing conditions
   */
  fun getSelfProfile(onComplete: () -> Unit) {
    _loadingSelf.value = true
    repository.getProfileByUid(
        auth.currentUser?.uid ?: "null",
        onSuccess = { profile ->
          _selfProfile.value = profile
          _loadingSelf.value = false
          onComplete()
        },
        onFailure = { e -> handleError(e) })
  }

  /** Get the profile of our current user */
  fun getSelfProfileValue(): Profile? {
    return selfProfile.value
  }

  /** Get the cancellation messages of the different profiles */
  fun getCancellationMessageProfile(): List<Profile> {
    return _cancellationMessageProfile.value
  }

  /** Get the profiles in messaging range */
  fun getUsersInMessagingRange(): List<Profile> {
    return messagingProfiles.value
  }

  /**
   * Converts an image URI to a Bitmap.
   *
   * @param context The context used to access the content resolver.
   * @param imageUri The URI of the image to be converted.
   * @return A Bitmap representing the image, or null if an error occurs.
   */
  private fun imageUriToBitmap(context: Context, imageUri: Uri): Bitmap? {
    return try {
      val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
      val bitmap = BitmapFactory.decodeStream(inputStream)
      inputStream?.close()
      bitmap
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  /**
   * Compresses an image if it is too large.
   *
   * @param bitmap The Bitmap to be compressed.
   * @param maxResolution The maximum resolution of the image. Default is 600.
   * @return A compressed Bitmap if the image is too large, or null if the image is already small
   *   enough.
   */
  private fun compressImageIfTooLarge(bitmap: Bitmap, maxResolution: Int = MAX_RESOLUTION): Bitmap {
    return if (bitmap.width > maxResolution) {
      val scale = maxResolution.toFloat() / bitmap.width
      Bitmap.createScaledBitmap(
          bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else {
      bitmap
    }
  }

  /**
   * Converts a Bitmap to a JPEG byte array.
   *
   * @param bitmap The Bitmap to be converted.
   * @param quality The quality of the JPEG compression (0-100). Default is 100.
   * @return A byte array representing the JPEG image, or null if an error occurs.
   */
  private fun bitmapToJpgByteArray(bitmap: Bitmap, quality: Int = DEFAULT_QUALITY): ByteArray? {
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
