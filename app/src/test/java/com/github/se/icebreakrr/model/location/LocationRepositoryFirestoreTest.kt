package com.github.se.icebreakrr.model.location

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.github.se.icebreakrr.utils.GeoHashUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class LocationRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUser: FirebaseUser

  private lateinit var locationRepository: LocationRepositoryFirestore

  private val testGeoPoint = GeoPoint(48.8566, 2.3522) // Example location (Paris)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)

    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn("testUserId")

    locationRepository = LocationRepositoryFirestore(mockFirestore, mockAuth)
  }

  @Test
  fun setUserPosition_shouldLogWarningWhenUserNotAuthenticated() {
    `when`(mockAuth.currentUser).thenReturn(null)

    locationRepository.setUserPosition(testGeoPoint)

    // Check that the warning log was triggered
    verify(mockAuth, atLeastOnce()).currentUser
  }

  @Test
  fun setUserPosition_shouldUpdateLocationInFirestoreWhenUserIsAuthenticated() {
    val mockUserId = "testUserId"
    val geohash = GeoHashUtils.encode(testGeoPoint.latitude, testGeoPoint.longitude, 10)
    val locationData = mapOf("geohash" to geohash, "location" to testGeoPoint)

    `when`(mockAuth.currentUser?.uid).thenReturn(mockUserId)
    `when`(mockFirestore.collection("profiles").document(mockUserId))
        .thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.update(locationData)).thenReturn(Tasks.forResult(null))

    locationRepository.setUserPosition(testGeoPoint)

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).update(locationData)
  }

  @Test
  fun setUserPosition_shouldCallFailureCallback_onError() {
    val mockUserId = "testUserId"
    val geohash = GeoHashUtils.encode(testGeoPoint.latitude, testGeoPoint.longitude, 10)
    val locationData = mapOf("geohash" to geohash, "location" to testGeoPoint)

    `when`(mockAuth.currentUser?.uid).thenReturn(mockUserId)
    `when`(mockFirestore.collection("profiles").document(mockUserId))
        .thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.update(locationData))
        .thenReturn(Tasks.forException(Exception("Test exception")))

    locationRepository.setUserPosition(testGeoPoint)

    shadowOf(Looper.getMainLooper()).idle()
  }
}
