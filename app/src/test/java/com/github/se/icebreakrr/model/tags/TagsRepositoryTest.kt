package com.github.se.icebreakrr.model.profile.tags

import android.util.Log
import com.github.se.icebreakrr.model.tags.CategoryString
import com.github.se.icebreakrr.model.tags.TagsCategory
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
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
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagsRepositoryTest {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var repository: TagsRepository

  @Before
  fun setUp() {
    firestore = mock(FirebaseFirestore::class.java)
    repository = spy(TagsRepository(firestore, FirebaseAuth.getInstance()))
  }

  @Test
  fun getAllTagsOnSuccessTest() {
    val repoAllTags: List<TagsCategory> =
        listOf(
            TagsCategory("Sport", "0xFFFF0000", listOf("Tennis", "Basketball", "PingPong")),
            TagsCategory("Music", "0xFF0000FF", listOf("Rock", "Tech", "Classical")))

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

    `when`(doc1.getString("name")).thenReturn("Sport")
    `when`(doc1.getString("color")).thenReturn("0xFFFF0000")
    `when`(doc1.get("subtags")).thenReturn(listOf("Tennis", "Basketball", "PingPong"))

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
  fun addCategoryOnSuccessAlreadyExistsTest() {
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
    `when`(documentReference.update(eq("subtags"), eq(listOf("Kayak", "Trail"))))
        .thenReturn(updateTask)
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
  fun addCategoryOnSuccessDontExistTest() {
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

    `when`(task.addOnFailureListener(any())).thenAnswer { invocation -> task }

    repository.addCategory({}, "Chaton", listOf("Kayak", "Trail"), "#FFFFFFFF")

    assertEquals(true, success)
  }

  @Test
  fun deleteTagSuccessfulTest() {
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val updateTask: Task<Void> = mock(Task::class.java) as Task<Void>
    val spyRepository = repository // Spy on the real repository
    var success = false

    // Mock Firestore collection and document retrieval
    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)

    // Mock getAllTags behavior
    doAnswer { invocation: InvocationOnMock ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(listOf(TagsCategory("Sport", "#FFFFFFFF", listOf("Football", "Basketball"))))
          null
        }
        .`when`(spyRepository)
        .getAllTags(anyOrNull(), anyOrNull())

    // Mock update behavior in Firestore
    `when`(documentReference.update(eq("subtags"), eq(listOf("Football")))).thenReturn(updateTask)
    `when`(updateTask.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      success = true
      updateTask
    }
    `when`(updateTask.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      updateTask
    }

    spyRepository.deleteTag({}, "Basketball", CategoryString.Sport)

    assertTrue(success)
  }

  @Test
  fun deleteTagFailureTest() {
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val updateTask: Task<Void> = mock(Task::class.java) as Task<Void>
    val spyRepository = repository
    var failureCalled = false
    val exception = Exception("Firestore update failed")

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)

    doAnswer { invocation: InvocationOnMock ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(listOf(TagsCategory("Sport", "#FFFFFFFF", listOf("Football", "Basketball"))))
        }
        .`when`(spyRepository)
        .getAllTags(anyOrNull(), anyOrNull())

    `when`(documentReference.update(eq("subtags"), eq(listOf("Football")))).thenReturn(updateTask)
    `when`(updateTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      updateTask
    }
    `when`(updateTask.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      updateTask
    }

    spyRepository.deleteTag(
        {
          failureCalled = true
          assertEquals(exception.message, it.message)
        },
        "Basketball",
        CategoryString.Sport)

    assertTrue(failureCalled)
  }

  @Test
  fun deleteCategorySuccessTest() {
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val deleteTask: Task<Void> = mock(Task::class.java) as Task<Void>
    val spyRepository = repository
    var successCalled = false

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)

    doAnswer { invocation: InvocationOnMock ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(listOf(TagsCategory("Sport", "#FFFFFFFF", listOf("Football", "Basketball"))))
        }
        .`when`(spyRepository)
        .getAllTags(anyOrNull(), anyOrNull())

    `when`(documentReference.delete()).thenReturn(deleteTask)
    `when`(deleteTask.addOnSuccessListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      successCalled = true
      deleteTask
    }
    `when`(deleteTask.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      deleteTask
    }

    spyRepository.deleteCategory({}, CategoryString.Sport)

    assertTrue(successCalled)
  }

  @Test
  fun deleteCategoryFailureTest() {
    val collectionReference = mock(CollectionReference::class.java)
    val documentReference = mock(DocumentReference::class.java)
    val deleteTask: Task<Void> = mock(Task::class.java) as Task<Void>
    val spyRepository = repository
    var failureCalled = false
    val exception = Exception("Firestore delete failed")

    `when`(firestore.collection("Tags")).thenReturn(collectionReference)
    `when`(collectionReference.document("Sport")).thenReturn(documentReference)

    doAnswer { invocation: InvocationOnMock ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(listOf(TagsCategory("Sport", "#FFFFFFFF", listOf("Football", "Basketball"))))
        }
        .`when`(spyRepository)
        .getAllTags(anyOrNull(), anyOrNull())

    `when`(documentReference.delete()).thenReturn(deleteTask)
    `when`(deleteTask.addOnSuccessListener(any())).thenReturn(deleteTask)
    `when`(deleteTask.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      deleteTask
    }

    spyRepository.deleteCategory(
        {
          failureCalled = true
          assertEquals(exception.message, it.message)
        },
        CategoryString.Sport)

    assertTrue(failureCalled)
  }

  @Test
  fun getAllTagsOnSuccessMissingBranchTest() {
    val repoAllTags: List<TagsCategory> =
        listOf(
            TagsCategory("", "0x00000000", emptyList()),
            TagsCategory("", "0xFF0000FF", emptyList()))

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

    `when`(doc1.getString("name")).thenReturn(null)
    `when`(doc1.getString("color")).thenReturn(null)
    `when`(doc1.get("subtags")).thenReturn(null)

    `when`(doc2.getString("name")).thenReturn(null)
    `when`(doc2.getString("color")).thenReturn("0xFF0000FF")
    `when`(doc2.get("subtags")).thenReturn(null)

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
}
