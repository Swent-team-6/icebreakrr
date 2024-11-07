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
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
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
          profilePictureUrl = "http://example.com/profile.jpg")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    profilesRepositoryFirestore = ProfilesRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  // Test for getNewProfileId()
  @Test
  fun getNewProfileId_shouldReturnNewId() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val newId = profilesRepositoryFirestore.getNewProfileId()
    assert(newId == "1")
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
  fun getProfilesInRadius_shouldFetchAllProfiles() {
    `when`(mockCollectionReference.get(any<com.google.firebase.firestore.Source>()))
        .thenReturn(Tasks.forResult(mockProfileQuerySnapshot))

    // Simulate that the QuerySnapshot contains an empty list of documents
    `when`(mockProfileQuerySnapshot.documents).thenReturn(listOf())

    profilesRepositoryFirestore.getProfilesInRadius(
        center = GeoPoint(0.0, 0.0), // Parameters are currently unused
        radiusInMeters = 1000.0,
        onSuccess = {
          // Do nothing; just verifying interactions
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    // Verify that 'documents' field was accessed after Firestore query
    verify(mockProfileQuerySnapshot).documents
  }

  @Test
  fun getProfilesInRadius_shouldCallFailureCallback_onError() {
    `when`(mockCollectionReference.get(any<com.google.firebase.firestore.Source>()))
        .thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.getProfilesInRadius(
        center = GeoPoint(0.0, 0.0),
        radiusInMeters = 1000.0,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun addNewProfile_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.addNewProfile(profile, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    // Ensure Firestore collection method was called to reference the "profiles" collection
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
  fun updateProfile_shouldCallFirestoreUpdate() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.updateProfile(
        profile = profile,
        onSuccess = {
          // Do nothing; we're just verifying that Firestore is called correctly
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle() // Ensure asynchronous tasks complete

    // Verify that Firestore 'set()' method is called to update the profile
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

    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    profilesRepositoryFirestore.getProfileByUid(
        uid = "1",
        onSuccess = { profile -> assert(profile?.name == "John Doe") },
        onFailure = { fail("Failure callback should not be called") })

    verify(mockDocumentReference).get()
  }

  @Test
  fun getProfileByUid_shouldCallFailureCallback_onError() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forException(Exception("Test exception")))

    profilesRepositoryFirestore.getProfileByUid(
        uid = "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { exception -> assert(exception.message == "Test exception") })

    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun deleteProfileByUid_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.deleteProfileByUid("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

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
  fun getProfilesInRadius_shouldConvertDocumentsToProfiles() {
    // Mock valid DocumentSnapshots
    val validDocument1: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    val validDocument2: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    val invalidDocument: DocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Set up the first valid document
    `when`(validDocument1.id).thenReturn("1")
    `when`(validDocument1.getString("name")).thenReturn("John Doe")
    `when`(validDocument1.getString("gender")).thenReturn("MEN")
    `when`(validDocument1.getTimestamp("birthDate")).thenReturn(Timestamp.now())
    `when`(validDocument1.getString("catchPhrase")).thenReturn("Hello World")
    `when`(validDocument1.getString("description")).thenReturn("Sample description")
    `when`(validDocument1.get("tags")).thenReturn(listOf("tag1", "tag2"))
    `when`(validDocument1.getString("profilePictureUrl"))
        .thenReturn("http://example.com/profile.jpg")

    // Set up the second valid document
    `when`(validDocument2.id).thenReturn("2")
    `when`(validDocument2.getString("name")).thenReturn("Jane Doe")
    `when`(validDocument2.getString("gender")).thenReturn("WOMEN")
    `when`(validDocument2.getTimestamp("birthDate")).thenReturn(Timestamp.now())
    `when`(validDocument2.getString("catchPhrase")).thenReturn("Greetings")
    `when`(validDocument2.getString("description")).thenReturn("Another sample profile")
    `when`(validDocument2.get("tags")).thenReturn(listOf("tag3", "tag4"))
    `when`(validDocument2.getString("profilePictureUrl")).thenReturn("http://example.com/jane.jpg")

    // Set up an invalid document (missing required fields)
    `when`(invalidDocument.id).thenReturn("3")
    `when`(invalidDocument.getString("name")).thenReturn(null) // Name is missing
    `when`(invalidDocument.getString("gender")).thenReturn("UNKNOWN") // Invalid gender
    `when`(invalidDocument.getTimestamp("birthDate")).thenReturn(Timestamp.now())
    `when`(invalidDocument.getString("catchPhrase")).thenReturn("Oops")
    `when`(invalidDocument.getString("description")).thenReturn("Invalid profile")
    `when`(invalidDocument.get("tags")).thenReturn(listOf(""))
    `when`(invalidDocument.getString("profilePictureUrl")).thenReturn(null) // Nullable field

    // Mock the QuerySnapshot
    `when`(mockProfileQuerySnapshot.documents)
        .thenReturn(listOf(validDocument1, validDocument2, invalidDocument))
    `when`(mockCollectionReference.get(any<com.google.firebase.firestore.Source>()))
        .thenReturn(Tasks.forResult(mockProfileQuerySnapshot))

    profilesRepositoryFirestore.getProfilesInRadius(
        center = GeoPoint(0.0, 0.0),
        radiusInMeters = 1000.0,
        onSuccess = { profiles ->
          // Assert that the profiles were converted correctly
          assertEquals(2, profiles.size) // Ensure only valid profiles are returned
          assertEquals("John Doe", profiles[0].name)
          assertEquals("Jane Doe", profiles[1].name)
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
  }
}
