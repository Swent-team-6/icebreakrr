package com.github.se.icebreakrr.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ProfilePicRepositoryStorageTest {

  @Mock private lateinit var mockFirebaseStorage: FirebaseStorage
  @Mock private lateinit var mockStorageReference: StorageReference

  private lateinit var profilePicRepositoryStorage: ProfilePicRepositoryStorage

  private val userId = "testUserId"
  private val imageData = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    profilePicRepositoryStorage = ProfilePicRepositoryStorage(mockFirebaseStorage)

    `when`(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
    `when`(mockStorageReference.child(anyString())).thenReturn(mockStorageReference)
  }

  @Test
  fun uploadProfilePicture_shouldUploadImage() {
    val mockUploadTask = mock(UploadTask::class.java)
    `when`(mockStorageReference.putBytes(any())).thenReturn(mockUploadTask)
    `when`(mockUploadTask.addOnSuccessListener(any())).thenReturn(mockUploadTask)
    `when`(mockUploadTask.addOnFailureListener(any())).thenReturn(mockUploadTask)
    `when`(mockStorageReference.downloadUrl).thenReturn(Tasks.forResult(mock()))

    profilePicRepositoryStorage.uploadProfilePicture(
        userId = userId,
        imageData = imageData,
        onSuccess = { url -> assert(url != null) },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockStorageReference).putBytes(imageData)
  }

  @Test
  fun uploadProfilePicture_shouldCallFailureCallback_onInvalidImage() {
    val invalidImageData = byteArrayOf(0x00, 0x01, 0x02, 0x03)

    profilePicRepositoryStorage.uploadProfilePicture(
        userId = userId,
        imageData = invalidImageData,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception is IllegalArgumentException) })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun getProfilePictureByUid_shouldReturnUrl() {
    `when`(mockStorageReference.downloadUrl).thenReturn(Tasks.forResult(mock()))

    profilePicRepositoryStorage.getProfilePictureByUid(
        userId = userId,
        onSuccess = { url -> assert(url != null) },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockStorageReference).downloadUrl
  }

  @Test
  fun deleteProfilePicture_shouldDeleteImage() {
    `when`(mockStorageReference.delete()).thenReturn(Tasks.forResult(null))

    profilePicRepositoryStorage.deleteProfilePicture(
        userId = userId,
        onSuccess = { /* Success */},
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockStorageReference).delete()
  }

  @Test
  fun deleteProfilePicture_shouldCallFailureCallback_onError() {
    `when`(mockStorageReference.delete())
        .thenReturn(Tasks.forException(Exception("Test exception")))

    profilePicRepositoryStorage.deleteProfilePicture(
        userId = userId,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }
}
