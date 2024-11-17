package com.github.se.di.module

import com.github.se.icebreakrr.di.module.AuthStateListenerModule
import com.github.se.icebreakrr.di.module.FirebaseAuthModule
import com.github.se.icebreakrr.di.module.FirestoreModule
import com.github.se.icebreakrr.mock.getMockedProfiles
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any

/** FirebaseAuth object that Hilt will inject into the MainActivity when testing */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [FirebaseAuthModule::class])
object MockFirebaseAuthModule {

  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth {
    return provideMockFirebaseAuth() // Provide the mock instance for testing
  }

  private fun provideMockFirebaseAuth(): FirebaseAuth {
    val mockUser = mock(FirebaseUser::class.java)
    `when`(mockUser.uid).thenReturn("12345")
    `when`(mockUser.displayName).thenReturn("IceBreakrr end-to-end")
    `when`(mockUser.email).thenReturn("icebreakrr.team@gmail.com")
    `when`(mockUser.isEmailVerified).thenReturn(true)
    `when`(mockUser.isAnonymous).thenReturn(false)
    `when`(mockUser.isEmailVerified).thenReturn(true)

    val mockAuth = mock(FirebaseAuth::class.java)
    val mockListener = mock(AuthStateListener::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    doNothing().`when`(mockAuth).addAuthStateListener(mockListener)
    doNothing().`when`(mockAuth).removeAuthStateListener(mockListener)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as AuthStateListener
          listener.onAuthStateChanged(mockAuth)
        }
        .`when`(mockAuth)
        .addAuthStateListener(any())

    // Mock signInWithEmailAndPassword method
    // val mockTaskSignIn = mock(Task::class.java) as Task<AuthResult>
    // `when`(mockAuth.signInWithEmailAndPassword(anyString(),
    // anyString())).thenReturn(mockTaskSignIn)

    // Mock signOut method
    // doNothing().`when`(mockAuth).signOut()

    // Mock sendPasswordResetEmail method
    // val mockTaskResetPassword = mock(Task::class.java) as Task<Void>
    // `when`(mockAuth.sendPasswordResetEmail(anyString())).thenReturn(mockTaskResetPassword)

    // Mock createUserWithEmailAndPassword method
    // val mockTaskCreateUser = mock(Task::class.java) as Task<AuthResult>
    // `when`(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
    //    .thenReturn(mockTaskCreateUser)

    // Mock the signInAnonymously method
    // val mockTaskSignInAnonymously = mock(Task::class.java) as Task<AuthResult>
    // `when`(mockAuth.signInAnonymously()).thenReturn(mockTaskSignInAnonymously)
    return mockAuth
  }
}

/** FirebaseFirestore object that Hilt will inject into the MainActivity when testing */
@Suppress("UNCHECKED_CAST")
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [FirestoreModule::class])
object MockFirebaseFirestoreModule {

  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return provideMockFirebasefirestore() // Provide the mock instance for testing
  }

  private fun provideMockFirebasefirestore(): FirebaseFirestore {
    val globalMockGeopoint = mock(GeoPoint::class.java)
    `when`(globalMockGeopoint.latitude).thenReturn(46.51827)
    `when`(globalMockGeopoint.longitude).thenReturn(6.619265)
    // mock firebasefirestore for porfileRepositoryFirestore
    var myProfile =
        Profile(
            "12345",
            "IceBreakrr end-to-end",
            Gender.MEN,
            Timestamp.now(),
            "",
            "",
            emptyList(),
            null,
            null)
    val profilesDocumentSnapshots =
        listOf(
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java))
    val profilesDocumentReference =
        listOf(
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java),
            mock(DocumentReference::class.java))
    val profilesTaskDocumentSnapshot =
        listOf(
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>,
            mock(Task::class.java) as Task<DocumentSnapshot>)
    val profiles = Profile.Companion.getMockedProfiles()
    // set the profilesDocumentSnapshot to return the mock profiles informations :
    for (i in 0 until 12) {
      `when`(profilesDocumentSnapshots[i].id).thenReturn(profiles[i].uid)
      `when`(profilesDocumentSnapshots[i].getString("name")).thenReturn(profiles[i].name)
      `when`(profilesDocumentSnapshots[i].getString("gender"))
          .thenReturn(profiles[i].gender.toString())
      `when`(profilesDocumentSnapshots[i].getTimestamp("birthDate"))
          .thenReturn(profiles[i].birthDate)
      `when`(profilesDocumentSnapshots[i].getString("catchPhrase"))
          .thenReturn(profiles[i].catchPhrase)
      `when`(profilesDocumentSnapshots[i].getString("description"))
          .thenReturn(profiles[i].description)
      `when`(profilesDocumentSnapshots[i].get("tags")).thenReturn(profiles[i].tags)
      `when`(profilesDocumentSnapshots[i].getString("profilePictureUrl"))
          .thenReturn(profiles[i].profilePictureUrl)
      `when`(profilesDocumentSnapshots[i].getString("fcmToken")).thenReturn(profiles[i].fcmToken)
      `when`(profilesDocumentSnapshots[i].getGeoPoint("location")).thenReturn(globalMockGeopoint)
    }

    val mockFirestore = mock(FirebaseFirestore::class.java)
    val mockProfilesCollectionReference = mock(CollectionReference::class.java)

    // actions in getProfilesByRadius :
    val mockTaskQuerySnapshotProfiles = mock(Task::class.java) as Task<QuerySnapshot>
    val mockQuerySnapshotProfiles = mock(QuerySnapshot::class.java)
    val mockProfilesQuery = mock(Query::class.java)
    `when`(mockFirestore.collection("profiles")).thenReturn(mockProfilesCollectionReference)
    `when`(mockProfilesCollectionReference.whereGreaterThanOrEqualTo(anyString(), anyString()))
        .thenReturn(mockProfilesQuery)
    `when`(mockProfilesQuery.whereLessThanOrEqualTo(anyString(), anyString()))
        .thenReturn(mockProfilesQuery)
    `when`(mockProfilesQuery.get()).thenReturn(mockTaskQuerySnapshotProfiles)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
          listener.onSuccess(mockQuerySnapshotProfiles)
          mockTaskQuerySnapshotProfiles
        }
        .`when`(mockTaskQuerySnapshotProfiles)
        .addOnSuccessListener(any())
    `when`(mockQuerySnapshotProfiles.documents).thenReturn(profilesDocumentSnapshots)
    `when`(mockTaskQuerySnapshotProfiles.isSuccessful).thenReturn(true)

    // actions when getProfileByUid when uid is our uid :
    val mockMyTaskDocumentSnapshot = mock(Task::class.java) as Task<DocumentSnapshot>
    val mockMyDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val mockMyDocumentReference = mock(DocumentReference::class.java)
    `when`(mockProfilesCollectionReference.document("12345")).thenReturn(mockMyDocumentReference)
    `when`(mockMyDocumentReference.get(any())).thenReturn(mockMyTaskDocumentSnapshot)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnCompleteListener<DocumentSnapshot>
          listener.onComplete(mockMyTaskDocumentSnapshot)
          mockMyTaskDocumentSnapshot
        }
        .`when`(mockMyTaskDocumentSnapshot)
        .addOnCompleteListener(any())
    // populates my profile's information
    `when`(mockMyTaskDocumentSnapshot.isSuccessful).thenReturn(true)
    `when`(mockMyTaskDocumentSnapshot.result).thenReturn(mockMyDocumentSnapshot)
    `when`(mockMyDocumentSnapshot.id).thenReturn(myProfile.uid)
    `when`(mockMyDocumentSnapshot.getString("name")).thenReturn(myProfile.name)
    `when`(mockMyDocumentSnapshot.getString("gender")).thenReturn(myProfile.gender.toString())
    `when`(mockMyDocumentSnapshot.getTimestamp("birthDate")).thenReturn(myProfile.birthDate)
    `when`(mockMyDocumentSnapshot.getString("catchPhrase")).thenReturn(myProfile.catchPhrase)
    `when`(mockMyDocumentSnapshot.getString("description")).thenReturn(myProfile.description)
    `when`(mockMyDocumentSnapshot.get("tags")).thenReturn(myProfile.tags)
    `when`(mockMyDocumentSnapshot.getString("profilePictureUrl"))
        .thenReturn(myProfile.profilePictureUrl)
    `when`(mockMyDocumentSnapshot.getString("fcmToken")).thenReturn(myProfile.fcmToken)
    `when`(mockMyDocumentSnapshot.getGeoPoint("location")).thenReturn(globalMockGeopoint)

    // actions in updateProfile when uid is our uid :
    val mockMyTask = mock(Task::class.java) as Task<Void>
    doAnswer { invocation ->
          val capturedProfile = invocation.arguments[0] as Profile
          myProfile = capturedProfile
          // update behavior of mock when we get profile after changing it :
          `when`(mockMyDocumentSnapshot.id).thenReturn(myProfile.uid)
          `when`(mockMyDocumentSnapshot.getString("name")).thenReturn(myProfile.name)
          `when`(mockMyDocumentSnapshot.getString("gender")).thenReturn(myProfile.gender.toString())
          `when`(mockMyDocumentSnapshot.getTimestamp("birthDate")).thenReturn(myProfile.birthDate)
          `when`(mockMyDocumentSnapshot.getString("catchPhrase")).thenReturn(myProfile.catchPhrase)
          `when`(mockMyDocumentSnapshot.getString("description")).thenReturn(myProfile.description)
          `when`(mockMyDocumentSnapshot.get("tags")).thenReturn(myProfile.tags)
          `when`(mockMyDocumentSnapshot.getString("profilePictureUrl"))
              .thenReturn(myProfile.profilePictureUrl)
          `when`(mockMyDocumentSnapshot.getString("fcmToken")).thenReturn(myProfile.fcmToken)
          `when`(mockMyDocumentSnapshot.getGeoPoint("location")).thenReturn(globalMockGeopoint)
          mockMyTask
        }
        .`when`(mockMyDocumentReference)
        .set(any())
    `when`(mockMyTask.isSuccessful).thenReturn(true)

    // actions when get profile by uid of around you :
    for (i in 0 until 12) {
      `when`(mockProfilesCollectionReference.document(i.toString()))
          .thenReturn(profilesDocumentReference[i])
      `when`(profilesDocumentReference[i].get(any())).thenReturn(profilesTaskDocumentSnapshot[i])
      doAnswer { invocation ->
            val listener = invocation.arguments[0] as OnCompleteListener<DocumentSnapshot>
            listener.onComplete(profilesTaskDocumentSnapshot[i])
            profilesTaskDocumentSnapshot[i]
          }
          .`when`(profilesTaskDocumentSnapshot[i])
          .addOnCompleteListener(any())
      `when`(profilesTaskDocumentSnapshot[i].isSuccessful).thenReturn(true)
      `when`(profilesTaskDocumentSnapshot[i].result).thenReturn(profilesDocumentSnapshots[i])
    }

    // mock firebasefirestore for TagsRepository :
    val mockCollectionReferenceTags = mock(CollectionReference::class.java)
    val mockTaskQuerySnapshotTags = mock(Task::class.java) as Task<QuerySnapshot>
    val mockQuerySnapshotTags = mock(QuerySnapshot::class.java)
    val tagNames =
        listOf("Travel", "Technology", "Art", "Food", "Sport", "Animals", "Activity", "Other")
    val tagColors =
        listOf(
            "0xffccf4b4",
            "0xffedf4a5",
            "0xffe69de8",
            "0xfff2c479",
            "0xfffb939b",
            "0xffd4b4eb",
            "0xffccd0cc",
            "0xff8077e4")
    val tagSubtags =
        listOf(
            listOf("Travel", "Adventure"),
            listOf("Technology", "Software"),
            listOf("Art", "Music", "Cartoon", "Thriller", "Drama", "Action", "Pop", "Singing"),
            listOf("Food", "Cooking"),
            listOf("Sport", "Kites"),
            listOf("Animals", "Dog", "Aliens", "Frog"),
            listOf(
                "Activity",
                "Cooking",
                "Superhero",
                "Justice",
                "Detective",
                "Politics",
                "Science",
                "Archaeology",
                "History",
                "Investigator",
                "Entertainment"),
            listOf("Other", "Truth", "Mystery", "Power", "Secret", "Strength"))
    val allTags =
        listOf(
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java),
            mock(DocumentSnapshot::class.java))
    `when`(mockFirestore.collection("Tags")).thenReturn(mockCollectionReferenceTags)
    `when`(mockCollectionReferenceTags.get()).thenReturn(mockTaskQuerySnapshotTags)
    `when`(mockTaskQuerySnapshotTags.isSuccessful).thenReturn(true)
    `when`(mockQuerySnapshotTags.documents).thenReturn(allTags)
    `when`(mockTaskQuerySnapshotTags.addOnSuccessListener(ArgumentMatchers.any())).thenAnswer {
        invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      listener.onSuccess(mockQuerySnapshotTags)
      mockTaskQuerySnapshotTags
    }
    `when`(mockTaskQuerySnapshotTags.addOnFailureListener(ArgumentMatchers.any())).thenAnswer {
        invocation: InvocationOnMock ->
      mockTaskQuerySnapshotTags
    }

    var i = 0
    for (mock in allTags) {
      `when`(allTags[i].getString("name")).thenReturn(tagNames[i])
      `when`(allTags[i].getString("color")).thenReturn(tagColors[i])
      `when`(allTags[i].get("subtags")).thenReturn(tagSubtags[i])
      `when`(mock.exists()).thenReturn(true)
      i++
    }

    return mockFirestore
  }
}

/** AuthStateListener object that Hilt will inject into the MainActivity when testing */
@Module
@TestInstallIn(
    components = [SingletonComponent::class], replaces = [AuthStateListenerModule::class])
object MockAuthStateListenerModule {

  @Provides
  @Singleton
  fun provideAuthStateListener(): AuthStateListener {
    return mock(AuthStateListener::class.java)
  }
}
