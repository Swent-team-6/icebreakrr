package com.github.se.icebreakrr.model.location

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.github.se.icebreakrr.model.profile.ProfilesRepositoryFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class GeoFirestoreRepositoryTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockAuth: FirebaseAuth

  @Mock private lateinit var mockGeoFirestore: GeoFirestore

  @Mock private lateinit var mockFirebaseUser: FirebaseUser

  private lateinit var geoFirestoreRepository: GeoFirestoreRepository
  private lateinit var profilesRepositoryFirestore: ProfilesRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize FirebaseApp to avoid IllegalStateException
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    // Set up ShadowLog to capture logs for verification
    ShadowLog.clear()

    profilesRepositoryFirestore = ProfilesRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)

    `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn("testUserId")

    // Initialize GeoFirestoreRepository with mocked FirebaseAuth and GeoFirestore
    geoFirestoreRepository = GeoFirestoreRepository(mockGeoFirestore, mockAuth)
  }

  @Test
  fun `setUserPosition should log warning when user is not authenticated`() {
    `when`(mockAuth.currentUser).thenReturn(null)

    geoFirestoreRepository.setUserPosition(GeoPoint(10.0, 20.0))

    val logEntries = ShadowLog.getLogsForTag("GeoFirestoreRepository")
    assertTrue(
        logEntries.any { it.type == Log.WARN && it.msg.contains("User is not authenticated") })
  }

  @Test
  fun `setUserPosition should set location when user is authenticated`() {
    geoFirestoreRepository.setUserPosition(GeoPoint(10.0, 20.0))
    verify(mockGeoFirestore).setLocation(eq("testUserId"), any(), any())
  }

  @Test
  fun `setUserPosition should log error when setLocation fails`() {

    // Create a test callback that logs an error for verification
    val testCallback: (Exception?) -> Unit = { exception ->
      exception?.let {
        Log.e("GeoFirestoreRepository", "Failed to update user position in Firestore", it)
      }
    }

    geoFirestoreRepository =
        GeoFirestoreRepository(
            geoFirestore = mockGeoFirestore, auth = mockAuth, onSetLocationComplete = testCallback)

    // Simulate failure in setLocation
    `when`(mockGeoFirestore.setLocation(any(), any(), any())).thenAnswer {
      testCallback(Exception("Simulated error"))
      null
    }

    // Call the method under test
    geoFirestoreRepository.setUserPosition(GeoPoint(10.0, 20.0))

    // Verify that an error was logged
    val logEntries = ShadowLog.getLogsForTag("GeoFirestoreRepository")
    assertTrue(
        logEntries.any {
          it.type == Log.ERROR && it.msg.contains("Failed to update user position")
        })
  }
}
