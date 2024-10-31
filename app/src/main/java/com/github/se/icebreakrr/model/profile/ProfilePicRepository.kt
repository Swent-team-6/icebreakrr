package com.github.se.icebreakrr.model.profile

interface ProfilePicRepository {
  fun uploadProfilePicture(
      userId: String,
      imageData: ByteArray,
      onSuccess: (url: String?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getProfilePictureByUid(
      userId: String,
      onSuccess: (url: String?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteProfilePicture(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
