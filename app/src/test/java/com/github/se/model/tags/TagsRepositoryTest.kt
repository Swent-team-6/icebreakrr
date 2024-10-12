package com.github.se.model.tags

import androidx.compose.ui.graphics.Color
import com.github.se.icebreakrr.model.tags.TagsCategory
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.any
import org.mockito.invocation.InvocationOnMock

@RunWith(RobolectricTestRunner::class)
class TagsRepositoryTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: TagsRepository

    @Before
    fun setUp(){
        firestore = mock(FirebaseFirestore::class.java)
        repository = TagsRepository(firestore)
    }

    @Test
    fun getAllTagsOnSuccess(){
        val repoAllTags: List<TagsCategory> = listOf(TagsCategory(1, "Sport", "0xFFFF0000", listOf("Tennis", "Basketball", "PingPong")),
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
        `when`(task.addOnFailureListener(any())).thenAnswer { invocation: InvocationOnMock ->
            task
        }

        var allTags: List<TagsCategory>? = null
        repository.getAllTags(
            onSuccess = {allTags = it},
            onFailure = {}
        )
        assertEquals(repoAllTags, allTags)
    }
    @Test
    fun getAllTagsOnFailure(){
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
        repository.getAllTags(
            onSuccess = {},
            onFailure = { exception = it }
        )

        // Assert
        assertEquals("Test Exception", exception?.message)
    }
}