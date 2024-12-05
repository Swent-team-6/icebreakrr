package com.github.se.icebreakrr.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import java.time.Duration
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ProfilesRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockProfileQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockAuth: FirebaseAuth

  private lateinit var profilesRepositoryFirestore: ProfilesRepositoryFirestore

  private val profile =
      Profile(
          uid = "1",
          name = "John Doe",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Hello World",
          description = "Just a sample profile",
          tags = listOf("tag1", "tag2"),
          profilePictureUrl = "http://example.com/profile.jpg",
          location = GeoPoint(0.0, 0.0),
          geohash = "u4pruydqqvj")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    profilesRepositoryFirestore =
        ProfilesRepositoryFirestore(mockFirestore, FirebaseAuth.getInstance())

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  // TODO Try to mock Firestore to test the getProfileInRadius function

  @Test
  fun getNewProfileId_shouldReturnNewId() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val newId = profilesRepositoryFirestore.getNewProfileId()
    assertEquals("1", newId)
  }

  // Test when init is called without authenticated user
  @Test
  fun init_shouldNotCallOnSuccessWhenUserIsNotAuthenticated() {
    // Simulate no logged-in user
    `when`(mockAuth.currentUser).thenReturn(null)

    var onSuccessCalled = false

    profilesRepositoryFirestore.init { onSuccessCalled = true }

    // Ensure onSuccess is not called if the user is not authenticated
    assert(!onSuccessCalled)
  }

  @Test
  fun getProfilesInRadius_shouldCallFailureCallback_onError() {
    val mockQuery: Query = mock(Query::class.java)

    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(eq("geohash"), any()))
        .thenReturn(mockQuery)
    `when`(mockQuery.whereLessThanOrEqualTo(eq("geohash"), any())).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.getProfilesInRadius(
        center = GeoPoint(0.0, 0.0),
        radiusInMeters = 1000,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assertEquals("Test exception", exception.message) })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun addNewProfile_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.addNewProfile(profile, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun addNewProfile_shouldCallFailureCallback_onError() {
    `when`(mockDocumentReference.set(any()))
        .thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.addNewProfile(
        profile,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun updateProfile_shouldUpdateProfileInFirestore() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.updateProfile(
        profile = profile,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun updateProfile_shouldCallFailureCallback_onError() {
    `when`(mockDocumentReference.set(any()))
        .thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.updateProfile(
        profile = profile,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun getProfileByUid_shouldFetchProfile() {
    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("John Doe")
    `when`(mockDocumentSnapshot.getString("gender")).thenReturn("MALE")
    `when`(mockDocumentSnapshot.getTimestamp("birthDate")).thenReturn(Timestamp.now())
    `when`(mockDocumentSnapshot.getString("catchPhrase")).thenReturn("Hello World")
    `when`(mockDocumentSnapshot.getString("description")).thenReturn("Just a sample profile")
    `when`(mockDocumentSnapshot.get("tags")).thenReturn(listOf("tag1", "tag2"))
    `when`(mockDocumentSnapshot.getString("profilePictureUrl"))
        .thenReturn("http://example.com/profile.jpg")

    `when`(mockDocumentReference.get(Source.CACHE))
        .thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentReference.get(Source.SERVER))
        .thenReturn(Tasks.forResult(mockDocumentSnapshot))

    profilesRepositoryFirestore.getProfileByUid(
        uid = "1",
        onSuccess = { profile -> assert(profile?.name == "John Doe") },
        onFailure = { fail("Failure callback should not be called") })

    verify(mockDocumentReference).get(Source.CACHE)
  }

  @Test
  fun getProfileByUid_shouldCallFailureCallback_onError() {
    // Mock both get() variants to handle both network states
    `when`(mockDocumentReference.get(Source.CACHE))
        .thenReturn(Tasks.forException(Exception("Test exception")))
    `when`(mockDocumentReference.get(Source.SERVER))
        .thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.getProfileByUid(
        uid = "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun deleteProfileByUid_shouldDeleteProfileInFirestore() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.deleteProfileByUid(profile.uid, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteProfileByUid_shouldCallFailureCallback_onError() {
    `when`(mockDocumentReference.delete())
        .thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.deleteProfileByUid(
        uid = "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun checkConnectionPeriodically_onSuccess() {
    val mockQuery: Query = mock(Query::class.java)

    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(eq("geohash"), any()))
        .thenReturn(mockQuery)
    `when`(mockQuery.whereLessThanOrEqualTo(eq("geohash"), any())).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockProfileQuerySnapshot))

    `when`(mockProfileQuerySnapshot.documents).thenReturn(listOf())

    profilesRepositoryFirestore.isWaiting.value = true
    profilesRepositoryFirestore.waitingDone.value = true

    profilesRepositoryFirestore.checkConnectionPeriodically {}

    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(profilesRepositoryFirestore.isWaiting.value)
    assertFalse(profilesRepositoryFirestore.waitingDone.value)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun showsOfflineWhenTimerCompletesAndStillFailing() = runTest {
    val mockQuery: Query = mock(Query::class.java)

    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(eq("geohash"), any()))
        .thenReturn(mockQuery)
    `when`(mockQuery.whereLessThanOrEqualTo(eq("geohash"), any())).thenReturn(mockQuery)
    `when`(mockQuery.get())
        .thenReturn(
            Tasks.forException(
                FirebaseFirestoreException(
                    "Unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)))

    assertFalse(profilesRepositoryFirestore.isWaiting.value)
    assertFalse(profilesRepositoryFirestore.waitingDone.value)

    profilesRepositoryFirestore.getProfilesInRadius(
        GeoPoint(0.0, 0.0), 300, onSuccess = { fail("Should not succeed") }, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(profilesRepositoryFirestore.isWaiting.value)
    assertFalse(profilesRepositoryFirestore.waitingDone.value)

    advanceUntilIdle()

    assertTrue(profilesRepositoryFirestore.isWaiting.value)
    assertFalse(profilesRepositoryFirestore.waitingDone.value)
  }

  @Test
  fun handleConnectionFailure_successfulRetry() {
    val mockQuery: Query = mock(Query::class.java)

    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(eq("geohash"), any()))
        .thenReturn(mockQuery)
    `when`(mockQuery.whereLessThanOrEqualTo(eq("geohash"), any())).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockProfileQuerySnapshot))

    `when`(mockProfileQuerySnapshot.documents).thenReturn(listOf())

    var onFailureCalled = false
    profilesRepositoryFirestore.handleConnectionFailure { onFailureCalled = true }

    shadowOf(Looper.getMainLooper()).idle()

    shadowOf(Looper.getMainLooper())
        .idleFor(Duration.ofMillis(profilesRepositoryFirestore.connectionTimeOutMs))

    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(profilesRepositoryFirestore.isWaiting.value)
    assertFalse(profilesRepositoryFirestore.waitingDone.value)
    assertFalse(onFailureCalled)
  }

  @Test
  fun checkConnectionPeriodically_onFailure() {
    val mockQuery: Query = mock(Query::class.java)

    `when`(mockCollectionReference.whereGreaterThanOrEqualTo(eq("geohash"), any()))
        .thenReturn(mockQuery)
    `when`(mockQuery.whereLessThanOrEqualTo(eq("geohash"), any())).thenReturn(mockQuery)
    `when`(mockQuery.get())
        .thenReturn(
            Tasks.forException(
                FirebaseFirestoreException(
                    "Unavailable",
                    FirebaseFirestoreException.Code.UNAVAILABLE)))

    profilesRepositoryFirestore.checkConnectionPeriodically {}

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(profilesRepositoryFirestore.isWaiting.value)

    shadowOf(Looper.getMainLooper())
        .idleFor(Duration.ofMillis(profilesRepositoryFirestore.periodicTimeCheckWaitTime))

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(profilesRepositoryFirestore.isWaiting.value)
  }
}
