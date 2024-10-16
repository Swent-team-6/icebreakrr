package com.github.se.icebreakrr.model.tags

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TagsViewModel(private val repository: TagsRepository): ViewModel() {
    //observable variable containing all the tags from the firestore
    private val allTags_ = MutableStateFlow<List<TagsCategory>>(emptyList())
    val allTags: StateFlow<List<TagsCategory>> = allTags_

    //observable variable containing the queries in Edit profile or Filter
    private val query_ = MutableStateFlow("")
    val query: StateFlow<String> = query_

    //observable variable linked to the query. It is the tags showed in the dropdown
    //of the edit and the filter
    private var tagsSuggestions_ = MutableStateFlow(emptyList<String>())
    val tagsSuggestions: StateFlow<List<String>> = tagsSuggestions_

    //observable variable containing tags that you can see in the Around you
    private var tagsFiltered_ = MutableStateFlow(emptyList<String>())
    val tagsFiltered: StateFlow<List<String>> = tagsFiltered_

    //observable variable containing the tags from which you want to filter
    private var selectedFilterTags_ = MutableStateFlow(emptyList<String>())
    val selectedFilterTags: StateFlow<List<String>> = selectedFilterTags_

    init { getTags() }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TagsViewModel(TagsRepository(Firebase.firestore)) as T
                }
            }
    }

    /**
     * Utility function called at the instantiation of the view model. Don't need to
     * be called later.
     */
    private fun getTags(){
        repository.getAllTags(
            onSuccess = {allTags_.value = it},
            onFailure = { Log.e("TagsViewModel", "[getAllTags] failed to get the tags : $it") }
        )
    }

    /**
     * private method that gives you the TagsCategory of a certain tag
     * @param tag : string that represents the tag
     * @return the TagsCategory corresponding or TagsCategory("", "0xFFFFFFFF", emptyList())
     */
    private fun TagToCategory(tag: String): TagsCategory{
        allTags_.value.forEach{tagsCategory: TagsCategory ->
            if (tagsCategory.subtags.contains(tag)){
                return tagsCategory
            }
        }
        return TagsCategory()
    }

    /**
     * Function used to get the color of a specific tag
     * @param tag : string representing the name of the tag
     * @return the Color of this tag or Color("0xFFFFFFFF") if this tag is not
     * present in the firebase
     */
    fun TagToColor(tag: String): Color{ return Color(TagToCategory(tag).color.toLong(16)) }

    /**
     * function used in the Edit Profile and Filter in the Text field.
     * It is used to search new tags to be added to the profile or new filter that you
     * add to your Filter.
     * @param inputQuery : String that the user inputs on the textField
     * @param selectedTags : comes from the viewModel of the Profile. List of
     * tags (String) that the user have already selected
     */
    fun setQuery(inputQuery: String, selectedTags: List<String>){
        query_.value = inputQuery
        val tempTagSuggestion: MutableList<String> = mutableListOf()
        allTags_.value.forEach {tagCategory: TagsCategory ->
            tagCategory.subtags.forEach { tag: String ->
                if (tag.contains(query_.value)){
                    tempTagSuggestion.add(tag)
                }
            }
        }
        tagsSuggestions_.value = tempTagSuggestion.filter{tag: String ->
            !selectedTags.contains(tag)
        }
    }

    /**
     * Function to call when you save changes in the filter UI. Change
     * observable variable tagsFiltered to contain all the tags that
     * you can view.
     */
    fun applyFilters(){
        val tempTagsFiltered: MutableList<String> = mutableListOf()
        selectedFilterTags_.value.forEach { tag: String ->
            //if the tag is a category tag :
            if (enumValues<CategoryString>().any { it.name == tag}){
                //check if another tag is in this category :
                var countThisTag = true
                val tagCategory = TagToCategory(tag)
                selectedFilterTags_.value.forEach{subtag: String ->
                    if (tagCategory.subtags.contains(subtag)){
                        countThisTag = false
                    }
                }
                //if a subtag of the category is also in the list :
                if (countThisTag){
                    tempTagsFiltered.addAll(tagCategory.subtags)
                }
            }else {
                tempTagsFiltered.add(tag)
            }
        }
        tagsFiltered_.value = tempTagsFiltered
    }

    /**
     * function used in the filter UI when you select a new tag in the
     * dropdown menu
     * @param tag : tag to add
     */
    fun addFilter(tag: String){ selectedFilterTags_.value += tag }

    /**
     * function used in the filter UI when you remove a tag in the filters
     * @param : tag to remove
     */
    fun removeFilter(tag: String){selectedFilterTags_.value = selectedFilterTags_.value.filter { it != tag }}
}
