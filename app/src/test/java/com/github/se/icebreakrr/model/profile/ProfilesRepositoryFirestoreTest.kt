package com.github.se.icebreakrr.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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

  @Test
  fun getNewProfileId_shouldReturnNewId() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val newId = profilesRepositoryFirestore.getNewProfileId()
    assert(newId == "1")
  }

  // TODO need to change for the final implementation of getProfilesInRadius
  @Test
  fun getProfilesInRadius_shouldFetchAllProfiles() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockProfileQuerySnapshot))

    // Simulate that the QuerySnapshot contains an empty list of documents
    `when`(mockProfileQuerySnapshot.documents).thenReturn(listOf())

    profilesRepositoryFirestore.getProfilesInRadius(
        center = GeoPoint(0.0, 0.0), // Parameters are currently unused
        radiusInMeters = 1000.0,
        onSuccess = {
          // Do nothing; just verifying interactions
        },
        onFailure = { fail("Failure callback should not be called") })

    // Ensure asynchronous tasks complete
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that 'documents' field was accessed after Firestore query
    verify(mockProfileQuerySnapshot).documents
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
  fun deleteProfileByUid_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    profilesRepositoryFirestore.deleteProfileByUid("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).delete()
  }
}
