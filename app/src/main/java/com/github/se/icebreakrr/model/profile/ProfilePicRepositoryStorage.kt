package com.github.se.icebreakrr.model.profile

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfilePicRepositoryStorage(private val firebaseStorage: FirebaseStorage) :
  ProfilePicRepository {
  private val MAX_SIZE = 4
  private val HEX_FF= 0xFF
  private val HEX_D8= 0xD8
  private val HEX_D9 = 0xD9


    /**
   * Retrieves a reference to the profile picture storage location for a given user.
   *
   * @param userId The ID of the user whose profile picture reference is to be retrieved.
   * @return A StorageReference pointing to the user's profile picture location in Firebase Storage.
   */
  private fun getProfilePictureReference(userId: String): StorageReference {
    return firebaseStorage.reference.child("profile_pictures/$userId.jpg")
  }

  /**
   * Checks if the given byte array represents a JPG image.
   *
   * @param imageData The byte array to check.
   * @return True if the byte array is a JPG image, false otherwise.
   */
  private fun isJpgImage(imageData: ByteArray): Boolean {
    return imageData.size >= MAX_SIZE &&
        imageData[0] == HEX_FF.toByte() &&
        imageData[1] == HEX_D8.toByte() &&
        imageData[imageData.size - 2] == HEX_FF.toByte() &&
        imageData[imageData.size - 1] == HEX_D9.toByte()
  }

  /**
   * Uploads a profile picture to Firebase Storage.
   *
   * @param userId The ID of the user whose profile picture is to be uploaded.
   * @param imageData The byte array of the jpg (!!!) image file to be uploaded.
   * @param onSuccess A callback function to be invoked with the URL of the uploaded file if the
   *   upload is successful.
   * @param onFailure A callback function to be invoked with the exception if the upload fails.
   */
  override fun uploadProfilePicture(
      userId: String,
      imageData: ByteArray,
      onSuccess: (url: String?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (!isJpgImage(imageData)) {
      onFailure(IllegalArgumentException("Only JPG images are allowed"))
      return
    }

    val storageRef = getProfilePictureReference(userId)
    storageRef
        .putBytes(imageData)
        .addOnSuccessListener {
          storageRef.downloadUrl
              .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
              .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Retrieves the profile picture URL of a user from Firebase Storage.
   *
   * @param userId The ID of the user whose profile picture is to be retrieved.
   * @param onSuccess A callback function to be invoked with the URL of the profile picture if the
   *   retrieval is successful.
   * @param onFailure A callback function to be invoked with the exception if the retrieval fails.
   */
  override fun getProfilePictureByUid(
      userId: String,
      onSuccess: (url: String?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val storageRef = getProfilePictureReference(userId)
    storageRef.downloadUrl
        .addOnSuccessListener { onSuccess(it.toString()) }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Deletes the profile picture of a user from Firebase Storage.
   *
   * @param userId The ID of the user whose profile picture is to be deleted.
   * @param onSuccess A callback function to be invoked if the deletion is successful.
   * @param onFailure A callback function to be invoked if the deletion fails, with the exception as
   *   a parameter.
   */
  override fun deleteProfilePicture(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val storageRef = getProfilePictureReference(userId)
    storageRef.delete().addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
  }
}
