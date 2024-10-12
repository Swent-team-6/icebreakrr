package com.github.se.icebreakrr.model.tags

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TagsViewModel() {
  private val allTags_ = MutableStateFlow<List<TagsCategory>>(emptyList())
  var allTags: StateFlow<List<TagsCategory>> = allTags_.asStateFlow()

  // Output tags are the tags given after the user entered a string in the TagSelector, those are
  // modified each time a user enters a new letter
  private val outputTags_ = MutableStateFlow<List<Pair<String, Color>>>(emptyList())
  var outputTags: StateFlow<List<Pair<String, Color>>> = outputTags_.asStateFlow()

  fun getTagsWithName(query: String) {
    outputTags_.update { l ->
      listOf(
          Pair("tennis", Color.Red),
          Pair("football", Color.Red),
          Pair("Ping-Pong", Color.Red),
          Pair("Photography", Color.Yellow))
    }
  }

  fun getAllTags() {
    allTags_.update { l ->
      listOf(TagsCategory(0, "sport", "FF0000", listOf("tennis, ping-pong, handball")))
    }
  }
}
