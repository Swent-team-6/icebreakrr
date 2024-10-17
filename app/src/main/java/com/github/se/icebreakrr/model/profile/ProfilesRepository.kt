package com.github.se.icebreakrr.model.profile

import com.google.firebase.firestore.GeoPoint

interface ProfilesRepository {

  fun getNewProfileId(): String

  fun init(onSuccess: () -> Unit)

  fun getProfilesInRadius(
      center: GeoPoint,
      radiusInMeters: Double,
      onSuccess: (List<Profile>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun addNewProfile(profile: Profile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateProfile(profile: Profile, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getProfileByUid(uid: String, onSuccess: (Profile?) -> Unit, onFailure: (Exception) -> Unit)

  fun deleteProfileByUid(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
