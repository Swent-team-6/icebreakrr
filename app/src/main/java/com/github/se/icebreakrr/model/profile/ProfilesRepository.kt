package com.github.se.icebreakrr.model.profile

import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow

interface ProfilesRepository {

  val connectionTimeOutMs: Long
  val periodicTimeCheckWaitTime: Long
  val isWaiting: MutableStateFlow<Boolean>
  val waitingDone: MutableStateFlow<Boolean>

  fun checkConnectionPeriodically(onFailure: (Exception) -> Unit)

  fun getNewProfileId(): String

  fun updateIsWaiting(waiting: Boolean)

  fun updateWaitingDone(waiting: Boolean)

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
