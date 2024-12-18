package com.github.se.di.module

import android.util.Log
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
import com.google.firebase.firestore.Source
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
import org.mockito.kotlin.anyOrNull

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
    val mockAuth = mock(FirebaseAuth::class.java)
    val mockListener = mock(AuthStateListener::class.java)
    `when`(mockUser.uid).thenReturn("12345")
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    doNothing().`when`(mockAuth).addAuthStateListener(mockListener)
    doNothing().`when`(mockAuth).removeAuthStateListener(mockListener)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as AuthStateListener
          listener.onAuthStateChanged(mockAuth)
        }
        .`when`(mockAuth)
        .addAuthStateListener(any())
    return mockAuth
  }
}

/** FirebaseFirestore object that Hilt will inject into the MainActivity when testing */
@Suppress("UNCHECKED_CAST")
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [FirestoreModule::class])
object MockFirebaseFirestoreModule {
  private const val NUMBER_PROFILE = 12
  private const val TRAVEL_TAG_COLOR = "0xffccf4b4"
  private const val TECHNOLOGY_TAG_COLOR = "0xffedf4a5"
  private const val ART_TAG_COLOR = "0xffe69de8"
  private const val FOOD_TAG_COLOR = "0xfff2c479"
  private const val SPORT_TAG_COLOR = "0xfffb939b"
  private const val ANIMALS_TAG_COLOR = "0xffd4b4eb"
  private const val ACTIVITY_TAG_COLOR = "0xffccd0cc"
  private const val OTHER_TAG_COLOR = "0xff8077e4"
  private val TRAVEL_LIST = listOf("Travel", "Adventure")
  private val TECHNOLOGY_LIST = listOf("Technology", "Software")
  private val ART_LIST =
      listOf("Art", "Music", "Cartoon", "Thriller", "Drama", "Action", "Pop", "Singing")
  private val FOOD_LIST = listOf("Food", "Cooking")
  private val SPORT_LIST = listOf("Sport", "Kites")
  private val ANIMALS_LIST = listOf("Animals", "Dog", "Aliens", "Frog")
  private val ACTIVITY_LIST =
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
          "Entertainment")
  private val OTHER_LIST = listOf("Other", "Truth", "Mystery", "Power", "Secret", "Strength")

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
    val profilesDocumentSnapshots = List(NUMBER_PROFILE) { mock(DocumentSnapshot::class.java) }
    val profilesDocumentReference = List(NUMBER_PROFILE) { mock(DocumentReference::class.java) }
    val profilesTaskDocumentSnapshot =
        List(NUMBER_PROFILE) { mock(Task::class.java) as Task<DocumentSnapshot> }
    val profiles = Profile.Companion.getMockedProfiles()
    // set the profilesDocumentSnapshot to return the mock profiles informations :
    for (i in 0 until NUMBER_PROFILE) {
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
    `when`(mockProfilesQuery.get(Source.SERVER)).thenReturn(mockTaskQuerySnapshotProfiles)
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
    `when`(mockMyDocumentSnapshot.get("meetingRequestInbox"))
        .thenReturn(myProfile.meetingRequestInbox)
    `when`(mockMyDocumentSnapshot.get("meetingRequestChosenLocalisation"))
        .thenReturn(myProfile.meetingRequestChosenLocalisation)

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
          `when`(mockMyDocumentSnapshot.get("meetingRequestSent"))
              .thenReturn(myProfile.meetingRequestSent)
          `when`(mockMyDocumentSnapshot.get("hasBlocked")).thenReturn(myProfile.hasBlocked)
          `when`(mockMyDocumentSnapshot.get("hasAlreadyMet")).thenReturn(myProfile.hasAlreadyMet)
          `when`(mockMyDocumentSnapshot.get("reports")).thenReturn(myProfile.reports)
          `when`(mockMyDocumentSnapshot.get("meetingRequestInbox"))
              .thenReturn(myProfile.meetingRequestInbox)
          `when`(mockMyDocumentSnapshot.get("meetingRequestChosenLocalisation"))
              .thenReturn(myProfile.meetingRequestChosenLocalisation)
          Log.d("TESTEST", "[TestModule] update my profile ${myProfile}")
          mockMyTask
        }
        .`when`(mockMyDocumentReference)
        .set(any())
    doAnswer { invocation ->
          val onComplete = invocation.arguments[0] as OnCompleteListener<Void>
          onComplete.onComplete(mockMyTask)
          mockMyTask
        }
        .`when`(mockMyTask)
        .addOnCompleteListener(anyOrNull())
    `when`(mockMyTask.isSuccessful).thenReturn(true)

    // actions when get profile by uid of around you :
    for (i in 0 until NUMBER_PROFILE) {
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

      // when we update one of these profiles :
      val profileTask = mock(Task::class.java) as Task<Void>
      doAnswer { invocation ->
            val newProfile = invocation.arguments[0] as Profile
            `when`(profilesDocumentSnapshots[i].id).thenReturn(newProfile.uid)
            `when`(profilesDocumentSnapshots[i].getString("name")).thenReturn(newProfile.name)
            `when`(profilesDocumentSnapshots[i].getString("gender"))
                .thenReturn(newProfile.gender.toString())
            `when`(profilesDocumentSnapshots[i].getTimestamp("birthDate"))
                .thenReturn(newProfile.birthDate)
            `when`(profilesDocumentSnapshots[i].getString("catchPhrase"))
                .thenReturn(newProfile.catchPhrase)
            `when`(profilesDocumentSnapshots[i].getString("description"))
                .thenReturn(newProfile.description)
            `when`(profilesDocumentSnapshots[i].get("tags")).thenReturn(newProfile.tags)
            `when`(profilesDocumentSnapshots[i].getString("profilePictureUrl"))
                .thenReturn(newProfile.profilePictureUrl)
            `when`(profilesDocumentSnapshots[i].getString("fcmToken"))
                .thenReturn(newProfile.fcmToken)
            `when`(profilesDocumentSnapshots[i].getGeoPoint("location"))
                .thenReturn(globalMockGeopoint)
            `when`(profilesDocumentSnapshots[i].get("reports")).thenReturn(newProfile.reports)
            profileTask
          }
          .`when`(profilesDocumentReference[i])
          .set(any())
      `when`(profileTask.isSuccessful).thenReturn(true)
    }

    // get sent users :
    val mockUser2Query = mock(Query::class.java)
    val mockUser2TaskQuerySnapshot = mock(Task::class.java) as Task<QuerySnapshot>
    val mockUser2QuerySnapshot = mock(QuerySnapshot::class.java)
    `when`(mockProfilesCollectionReference.whereIn("uid", listOf("2"))).thenReturn(mockUser2Query)
    `when`(mockUser2Query.get()).thenReturn(mockUser2TaskQuerySnapshot)
    `when`(mockUser2TaskQuerySnapshot.isSuccessful).thenReturn(true)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
          listener.onSuccess(mockUser2QuerySnapshot)
          mockUser2TaskQuerySnapshot
        }
        .`when`(mockUser2TaskQuerySnapshot)
        .addOnSuccessListener(anyOrNull())
    `when`(mockUser2QuerySnapshot.documents).thenReturn(mutableListOf(profilesDocumentSnapshots[0]))

    // get blocked users :
    val mockAliceQuery = mock(Query::class.java)
    val mockAliceTaskQuerySnapshot = mock(Task::class.java) as Task<QuerySnapshot>
    val mockAliceQuerySnapshot = mock(QuerySnapshot::class.java)
    `when`(mockProfilesCollectionReference.whereIn("uid", listOf("0"))).thenReturn(mockAliceQuery)
    `when`(mockAliceQuery.get()).thenReturn(mockAliceTaskQuerySnapshot)
    `when`(mockAliceTaskQuerySnapshot.isSuccessful).thenReturn(true)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
          listener.onSuccess(mockAliceQuerySnapshot)
          mockAliceTaskQuerySnapshot
        }
        .`when`(mockAliceTaskQuerySnapshot)
        .addOnSuccessListener(anyOrNull())
    `when`(mockAliceQuerySnapshot.documents).thenReturn(mutableListOf(profilesDocumentSnapshots[0]))

    // actions for setUserPosition :
    val myMockLocationTask = mock(Task::class.java) as Task<Void>
    `when`(mockMyDocumentReference.update(anyOrNull())).thenReturn(myMockLocationTask)
    `when`(myMockLocationTask.isSuccessful).thenReturn(true)
    doAnswer { invocation ->
          val listener = invocation.arguments[0] as OnSuccessListener<Void>
          listener.onSuccess(null)
          myMockLocationTask
        }
        .`when`(myMockLocationTask)
        .addOnSuccessListener(any())

    // mock firebasefirestore for TagsRepository :
    val mockCollectionReferenceTags = mock(CollectionReference::class.java)
    val mockTaskQuerySnapshotTags = mock(Task::class.java) as Task<QuerySnapshot>
    val mockQuerySnapshotTags = mock(QuerySnapshot::class.java)
    val tagNames =
        listOf("Travel", "Technology", "Art", "Food", "Sport", "Animals", "Activity", "Other")
    val tagColors =
        listOf(
            TRAVEL_TAG_COLOR,
            TECHNOLOGY_TAG_COLOR,
            ART_TAG_COLOR,
            FOOD_TAG_COLOR,
            SPORT_TAG_COLOR,
            ANIMALS_TAG_COLOR,
            ACTIVITY_TAG_COLOR,
            OTHER_TAG_COLOR)
    val tagSubtags =
        listOf(
            TRAVEL_LIST,
            TECHNOLOGY_LIST,
            ART_LIST,
            FOOD_LIST,
            SPORT_LIST,
            ANIMALS_LIST,
            ACTIVITY_LIST,
            OTHER_LIST)
    val allTags = List(tagSubtags.size) { mock(DocumentSnapshot::class.java) }

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
