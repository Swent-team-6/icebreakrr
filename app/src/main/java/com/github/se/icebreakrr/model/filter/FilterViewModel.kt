package com.github.se.icebreakrr.model.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.ui.sections.DEFAULT_RADIUS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// This file was written with the help of CursorAI
open class FilterViewModel : ViewModel() {

  private val _selectedGenders = MutableStateFlow<List<Gender>>(emptyList())
  val selectedGenders: StateFlow<List<Gender>> = _selectedGenders

  private val _ageRange = MutableStateFlow<IntRange?>(null)
  val ageRange: StateFlow<IntRange?> = _ageRange

  private val _selectedRadius = MutableStateFlow(DEFAULT_RADIUS)
  val selectedRadius: StateFlow<Int> = _selectedRadius

  private val _filteredTags = MutableStateFlow<List<String>>(emptyList())
  val filteredTags: StateFlow<List<String>> = _filteredTags

  /**
   * Sets the selected genders.
   *
   * @param genders The list of selected genders.
   */
  fun setGenders(genders: List<Gender>) {
    _selectedGenders.value = genders
  }

  /**
   * Sets the age range.
   *
   * @param range The age range to set.
   */
  fun setAgeRange(range: IntRange?) {
    _ageRange.value = range
  }

  /**
   * Sets the selected radius.
   *
   * @param radius The radius to set in meters.
   */
  fun setSelectedRadius(radius: Int) {
    _selectedRadius.value = radius
  }

  /**
   * Sets the list of filtered tags.
   *
   * @param tags List of tags to save.
   */
  fun setFilteredTags(tags: List<String>) {
    _filteredTags.value = tags
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FilterViewModel() as T
          }
        }
  }
}
