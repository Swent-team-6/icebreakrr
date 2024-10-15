package com.github.se.icebreakrr.model.tags

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlin.Unit

class TagsRepository(private val db: FirebaseFirestore) {
  private val collectionPath = "Tags"

  /**
   * Function that get all the tags present in the firestore database
   *
   * @param onSuccess : when the operation is successful, returns the list of TagsCategory got from
   *   the database
   * @param onFailure : When the operation fails, log an error and returns the onFailure with the
   *   erorr
   */
  fun getAllTags(onSuccess: (List<TagsCategory>) -> Unit, onFailure: (Exception) -> Unit) {
    val categories: MutableList<TagsCategory> = mutableListOf()
    db.collection(collectionPath)
        .get()
        .addOnFailureListener { e: Exception ->
          Log.e("TagsRepository", "[getAllTags] Could not get the tags : $e")
          onFailure(e)
        }
        .addOnSuccessListener { docs: QuerySnapshot ->
          for (doc in docs.documents) {
            if (doc.exists()) {
              firestoreToTags(
                  doc,
                  { categories.add(it) },
                  {
                    Log.e(
                        "TagsRepository",
                        "[getAllTags] Could not fetch firebase to get the tags : $it")
                    onFailure(it)
                  })
            }
          }
          onSuccess(categories)
        }
  }

  /**
   * Function to add a tag to firebase in a category
   *
   * @param onFailure : could happen when the document is retrieved, when we convert document into
   *   TagsCategory, when the tag already exists in this category or if the category doesn't exist
   * @param category : category in which you want to add a tag
   * @param name : name of the new tag that you want to add
   */
  fun addTag(onFailure: (Exception) -> Unit, category: CategoryString, name: String) {
    val docRef = db.collection(collectionPath).document(category.name)
    docRef
        .get()
        .addOnFailureListener { exception: Exception ->
          Log.e("TagsRepository", "[addTag] Could not retrieve the document: $exception")
          onFailure(exception)
        }
        .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
          if (documentSnapshot.exists()) {
            var tagsCategory = TagsCategory("", "#FFFFFFFF", listOf())
            firestoreToTags(
                documentSnapshot,
                { tagCategoryCallback -> tagsCategory = tagCategoryCallback },
                { e ->
                  Log.e("TagsRepository", "[addTag] error while converting firebase to Tags $e")
                  onFailure(e)
                })

            tagsCategory.let { category: TagsCategory ->
              if (!category.subtags.contains(name)) {
                val updatedSubtags = category.subtags.toMutableList()
                updatedSubtags.add(name)

                docRef
                    .update("subtags", updatedSubtags)
                    .addOnFailureListener { exception: Exception ->
                      Log.e("TagsRepository", "[addTag] Could not update the document: $exception")
                      onFailure(exception)
                    }
                    .addOnSuccessListener {}
              } else {
                Log.d(
                    "TagsRepository",
                    "[addTag] Tag '$name' already exists in category '${category.name}'")
              }
            }
          } else {
            Log.d(
                "TagsRepository", "[addTag] Document does not exist for category: ${category.name}")
            onFailure(Exception("Document does not exist"))
          }
        }
  }

  /**
   * Function that adds a new category to the firestore (as a document) based on a name and a list
   * of subtags. If the category on firebase already exists, add elements of the subcateries that
   * are not in the category already present in the firebase to the category on firebase. Else it
   * just create a new category with the subtags in it. IMPORTANT : the name of the new category
   * must be put manually after the execution of the function into the enum CategoryString in
   * model/tags/Tags.kt
   *
   * @param onFailure : callback function called if there is an error
   * @param categoryName : name of the new category
   * @param subcategories : list of subcategories to have under the name category
   */
  fun addCategory(
      onFailure: (Exception) -> Unit,
      categoryName: String,
      subcategories: List<String>,
      color: String
  ) {
    // check if the category is already in the database
    if (CategoryString.values().any { it.name.equals(categoryName, ignoreCase = true) }) {
      db.collection(collectionPath)
          .document(categoryName)
          .get()
          .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
            var tagsCategory = TagsCategory("", "#FFFFFFFF", listOf())
            firestoreToTags(
                documentSnapshot,
                { tagCategoryCallback -> tagsCategory = tagCategoryCallback },
                { e: Exception ->
                  Log.e(
                      "TagsRepository",
                      "[addCategory] error while converting firebase to Tags : $e")
                  onFailure(e)
                })
            val subtags = tagsCategory.subtags.toMutableList()
            subtags.addAll(subcategories.filter { it !in subtags })
            db.collection(collectionPath)
                .document(categoryName)
                .update("subtags", subtags.toList())
                .addOnSuccessListener {}
                .addOnFailureListener { exception: Exception ->
                  Log.e("TagsRepository", "[addCategory] Could not update the document: $exception")
                  onFailure(exception)
                }
          }
          .addOnFailureListener { e: Exception ->
            Log.e("TagsRepository", "[addCategory] fail to get the document :  $e")
            onFailure(e)
          }
    } else {
      db.collection(collectionPath)
          .document(categoryName)
          .set(TagsCategory(categoryName, color, subcategories))
          .addOnSuccessListener {}
          .addOnFailureListener { e: Exception ->
            Log.e("TagsRepository", "[addCategory] could not set the new category :  $e")
            onFailure(e)
          }
    }
  }

  /**
   * use this function to delete a tag in a certain category. If the tag isn't in the category, or
   * the category doesn't exist, the method does nothing.
   *
   * @param onFailure : happens if the tags can't be fetch from the database or if the update of the
   *   database fails
   * @param name : name of the tag to delete
   * @param category : category in which the tag you want to suppress is
   */
  fun deleteTag(onFailure: (Exception) -> Unit, name: String, category: CategoryString) {
    var tagList = emptyList<TagsCategory>()
    getAllTags(
        { tagList = it }, { Log.e("TagsRepository", "[deleteTag] error while getting tags : $it") })
    val tagCategory =
        try {
          tagList.filter { it.name.equals(category.name) }.get(0)
        } catch (e: Exception) {
          null
        }
    if (tagCategory != null && tagCategory.subtags.contains(name)) {
      db.collection(collectionPath)
          .document(category.name)
          .update("subtags", tagCategory.subtags.filter { !it.equals(name) })
          .addOnSuccessListener {}
          .addOnFailureListener { e: Exception ->
            Log.e("TagsRepository", "[deleteTag] Failed to update the document : $e")
            onFailure(e)
          }
    }
    Log.d(
        "TagsRepository",
        "[deleteTag] the tag wasn't in the database or the category doesn't exist : $name")
  }

  /**
   * Use this function to delete a category. The function does nothing if the category doesn't
   * exist. IMPORTANT : once this method is used (and successful) you need to manually remove the
   * category you decided to suppress in CategoryString located in model/tags/Tags
   *
   * @param onFailure : this method can fails if there is an error while fetching the tags in the
   *   database or on the deletion of the category
   * @param category : category you want to suppress
   */
  fun deleteCategory(onFailure: (Exception) -> Unit, category: CategoryString) {
    var tagList = emptyList<TagsCategory>()
    getAllTags(
        { tagList = it },
        { Log.e("TagsRepository", "[deleteCategory] error while getting tags : $it") })
    val tagCategory =
        try {
          tagList.filter { it.name.equals(category.name) }.get(0)
        } catch (e: Exception) {
          null
        }
    if (tagCategory != null) {
      db.collection(collectionPath)
          .document(category.name)
          .delete()
          .addOnSuccessListener {}
          .addOnFailureListener { e: Exception ->
            Log.e("TagsRepository", "[deleteCategory] failed to delete the category : $e")
            onFailure(e)
          }
    } else {
      Log.d("TagsRepository", "[deleteCategory] category doesn't exist")
    }
  }

  /**
   * Private function that converts items from the firebase to a TagsCategory
   *
   * @param onSuccess : deserialize the tag category and returns it in the onSuccess
   * @param onFailure : logs an error and returns the error in the onFailure
   */
  @Suppress("UNCHECKED_CAST")
  private fun firestoreToTags(
      doc: DocumentSnapshot,
      onSuccess: (TagsCategory) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      val name = doc.getString("name") ?: ""
      val color = doc.getString("color") ?: "0x00000000"
      val subtags = doc.get("subtags") as? List<String> ?: emptyList()
      onSuccess(TagsCategory(name, color, subtags))
    } catch (e: Exception) {
      Log.e(
          "TagsRepository", "[firestoreToTags] Failure in the deserialization of json objects : $e")
      onFailure(e)
    }
  }
}
