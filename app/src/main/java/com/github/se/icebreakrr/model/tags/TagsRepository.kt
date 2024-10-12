package com.github.se.icebreakrr.model.tags;

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.Unit;

class TagsRepository(private val db: FirebaseFirestore) {
    private val collectionPath = "Tags"
    fun getAllTags(onSuccess: (List<TagsCategory>) -> Unit, onFailure: (Exception) -> Unit){
        val categories: MutableList<TagsCategory> = mutableListOf()
        db.collection(collectionPath).get()
            .addOnFailureListener{
                Log.e("TagsRepository", "[getAllTags] Could not get the tags : $it")
                onFailure(it)
            }.addOnSuccessListener { docs ->
                for (doc in docs.documents){
                    if (doc.exists()){
                        firestoreToTags(doc, {categories.add(it)}, {
                            Log.e("TagsRepository", "[getAllTags] Could not fetch firebase to get the tags : $it")
                            onFailure(it)
                        })
                    }
                }
                onSuccess(categories)
            }
    }

    private fun firestoreToTags(doc: DocumentSnapshot, onSuccess: (TagsCategory) -> Unit, onFailure: (Exception) -> Unit){
        try{
            val uid = doc.getLong("uid") ?: 0
            val name = doc.getString("name") ?: ""
            val color = doc.getString("color") ?: "#00000000"
            val subtags = doc.get("subtags") as? List<String> ?: emptyList()
            onSuccess(TagsCategory(uid, name, color, subtags))
        }catch(e: Exception){
            Log.e("TagsRepository", "[firestoreToTags] Failure in the deserialization of json objects : $e")
            onFailure(e)
        }
    }
}
