package com.github.se.model.tags

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.icebreakrr.model.tags.CategoryString
import com.github.se.icebreakrr.model.tags.TagsCategory
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagsRepositoryTest {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var repository: TagsRepository

  @Before
  fun setUp() {
    firestore = mock(FirebaseFirestore::class.java)
    repository = TagsRepository(firestore)
  }

  @Test
  fun getAllTagsOnSuccessTest() {
    val repoAllTags: List<TagsCategory> =
        listOf(
            TagsCategory(1, "Sport", "0xFFFF0000", listOf("Tennis", "Basketball", "PingPong")),
            TagsCategory(2, "Music", "0xFF0000FF", listOf("Rock", "Tech", "Classical")))

    val collectionReference = mock(CollectionReference::class.java)
    val task = mock(Task::class.java) as Task<QuerySnapshot>
    val querySnapshot = mock(QuerySnapshot::class.java)
    val doc1 = mock(DocumentSnapshot::class.java)
    val doc2 = mock(DocumentSnapshot::class.java)
    val documents = listOf(doc1, doc2)

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.get()).thenReturn(task)
    `when`(querySnapshot.documents).thenReturn(documents)
    `when`(doc1.exists()).thenReturn(true)
    `when`(doc2.exists()).thenReturn(true)

    `when`(doc1.getLong("uid")).thenReturn(1)
    `when`(doc1.getString("name")).thenReturn("Sport")
    `when`(doc1.getString("color")).thenReturn("0xFFFF0000")
    `when`(doc1.get("subtags")).thenReturn(listOf("Tennis", "Basketball", "PingPong"))

    `when`(doc2.getLong("uid")).thenReturn(2)
    `when`(doc2.getString("name")).thenReturn("Music")
    `when`(doc2.getString("color")).thenReturn("0xFF0000FF")
    `when`(doc2.get("subtags")).thenReturn(listOf("Rock", "Tech", "Classical"))

    `when`(task.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      listener.onSuccess(querySnapshot)
      task
    }
    `when`(task.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock -> task }

    var allTags: List<TagsCategory>? = null
    repository.getAllTags(onSuccess = { allTags = it }, onFailure = {})
    assertEquals(repoAllTags, allTags)
  }

  @Test
  fun getAllTagsOnFailureTest() {
    val collectionReference = mock(CollectionReference::class.java)
    val task: Task<QuerySnapshot> = mock(Task::class.java) as Task<QuerySnapshot>
    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.get()).thenReturn(task)

    `when`(task.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Test Exception"))
      task
    }

    var exception: Exception? = null
    repository.getAllTags(onSuccess = {}, onFailure = { exception = it })

    // Assert
    assertEquals("Test Exception", exception?.message)
  }

  @Test
  fun addTagTestOnSuccess() {
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task1: Task<DocumentSnapshot> = mock(Task::class.java) as Task<DocumentSnapshot>
    val updateTask: Task<Void> = mock(Task::class.java) as Task<Void>
    val documentSnapshot = mock(DocumentSnapshot::class.java)
    var success = false

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)
    `when`(documentReference.get()).thenReturn(task1)
    `when`(task1.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(documentSnapshot)
      task1
    }
    `when`(task1.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock -> task1 }
    `when`(documentSnapshot.exists()).thenReturn(true)

    `when`(documentSnapshot.getLong("uid")).thenReturn(0)
    `when`(documentSnapshot.getString("name")).thenReturn("Sport")
    `when`(documentSnapshot.getString("color")).thenReturn("#0xFFFFFFFF")
    `when`(documentSnapshot.get("subtags")).thenReturn(listOf<String>())
    `when`(documentReference.update(eq("subtags"), anyList<String>())).thenReturn(updateTask)
    `when`(updateTask.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      success = true
      updateTask
    }
    `when`(updateTask.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      updateTask
    }
    Log.d("TagsRepositoryTest", "${CategoryString.Sport}")
    repository.addTag({}, CategoryString.Sport, "manger")
    assertEquals(true, success)
  }

  @Test
  fun addTagFailureCategoryDoesntExistTest() {
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task1: Task<DocumentSnapshot> = mock(Task::class.java) as Task<DocumentSnapshot>
    val documentSnapshot = mock(DocumentSnapshot::class.java)
    var exception = Exception()

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)
    `when`(documentReference.get()).thenReturn(task1)
    `when`(task1.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(documentSnapshot)
      task1
    }
    `when`(task1.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock -> task1 }
    `when`(documentSnapshot.exists()).thenReturn(false)

    repository.addTag({ exception = it }, CategoryString.Sport, "manger")
    assertEquals("Document does not exist", exception.message)
  }

  @Test
  fun addCategoryOnSuccessAlreadyExistsTest(){
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task: Task<DocumentSnapshot> = mock(Task::class.java) as Task<DocumentSnapshot>
    val documentSnapshot = mock(DocumentSnapshot::class.java)
    val updateTask: Task<Void> = mock(Task::class.java) as Task<Void>
    var success = false

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)
    `when`(documentReference.get()).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(documentSnapshot)
      task
    }
    `when`(documentSnapshot.getLong(any())).thenReturn(0)
    `when`(documentSnapshot.getString("name")).thenReturn("Sport")
    `when`(documentSnapshot.getString("color")).thenReturn("#0xFFFFFFFF")
    `when`(documentSnapshot.get("subtags")).thenReturn(listOf("Kayak"))
    `when`(documentReference.update(eq("subtags"), eq(listOf("Kayak", "Trail")))).thenReturn(updateTask)
    `when`(updateTask.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      success = true
      updateTask
    }
    `when`(updateTask.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      updateTask
    }

    repository.addCategory({}, "Sport", listOf("Kayak", "Trail"), "#FFFFFFFF")

    assertEquals(true, success)
  }

  @Test
  fun addCategoryOnSuccessDontExistTest(){
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val task: Task<Void> = mock(Task::class.java) as Task<Void>
    var success = false

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Chaton")).thenReturn(documentReference)
    `when`(documentReference.set(any(TagsCategory::class.java))).thenReturn(task)
    `when`(task.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      success = true
      task
    }

    `when`(task.addOnFailureListener(any())).thenAnswer { invocation ->
      task
    }

    repository.addCategory({}, "Chaton", listOf("Kayak", "Trail"), "#FFFFFFFF")

    assertEquals(true, success)
  }
  @Test
  fun addCategory(){
    FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)
    val repo = TagsRepository(FirebaseFirestore.getInstance())
    repo.addCategory({}, "Sport",
      listOf("Football",
        "Basketball",
        "Sport",
        "Tennis",
        "PingPong",
        "Running",
        "Trail",
        "Cycling",
        "Racing",
        "Climbing",
        "Fitness",
        "Yoga",
        "Rugby",
        "Cricket",
        "Volleyball",
        "Hockey",
        "Handball",
        "WaterPolo",
        "Ultimate",
        "Badminton",
        "Squash",
        "Boxing",
        "Judo",
        "Karate",
        "Taekwondo",
        "MMA",
        "Kickboxing",
        "Swimming",
        "Surf",
        "Sailing",
        "Diving",
        "Kayaking",
        "Canoeing",
        "Windsurfing",
        "Athletism",
        "Ski",
        "Snowboard",
        "Karting",
        "Skateboarding",
        "Parkour",
        "Golf",
        "Bowling",
        "HorseRacing",
        "Polo",
        "CrossFit",
        "Camping",
        "Hiking",
        "Fitness"
      ), Color.Green.toString()
    )
  }
}
