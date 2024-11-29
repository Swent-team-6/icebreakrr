package com.github.se.icebreakrr.model.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SortViewModel(private val profilesViewModel: ProfilesViewModel) : ViewModel() {

  // StateFlow to hold the currently selected sort option. Defaults to sorting by distance.
  private val _selectedSortOption = MutableStateFlow(SortOption.DISTANCE)
  val selectedSortOption: StateFlow<SortOption> = _selectedSortOption

  companion object {
    /**
     * Factory method to create a SortViewModel instance.
     *
     * @param profilesViewModel The ProfilesViewModel instance required for the sorting logic.
     * @return A ViewModelProvider.Factory that produces SortViewModel instances.
     */
    fun createFactory(profilesViewModel: ProfilesViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SortViewModel::class.java)) {
              return SortViewModel(profilesViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
  }

  /**
   * Updates the selected sort option.
   *
   * @param sortOption The new sort option to apply.
   */
  fun updateSortOption(sortOption: SortOption) {
    _selectedSortOption.value = sortOption
  }

  /**
   * Sorts a list of profiles by distance in ascending order.
   *
   * @param profiles The list of profiles to sort.
   * @return The sorted list of profiles.
   */
  fun sortByDistance(profiles: List<Profile>): List<Profile> {
    return profiles.sortedBy { it.distanceToSelfProfile }
  }

  /**
   * Sorts a list of profiles by age in ascending order.
   *
   * @param profiles The list of profiles to sort.
   * @return The sorted list of profiles.
   */
  fun sortByAge(profiles: List<Profile>): List<Profile> {
    return profiles.sortedBy { it.calculateAge() }
  }

  /**
   * Sorts a list of profiles by the number of tags they have in common with the user's profile, in
   * descending order.
   *
   * @param profiles The list of profiles to sort.
   * @return The sorted list of profiles, with profiles having more tags in common appearing first.
   */
  fun sortByCommonTags(profiles: List<Profile>): List<Profile> {
    val selfProfile = profilesViewModel.selfProfile.value ?: return profiles
    return profiles.sortedByDescending { profile ->
      profile.tags.intersect(selfProfile.tags.toSet()).size
    }
  }
}

/** Enum representing the different sorting options available. */
enum class SortOption {
  AGE,
  DISTANCE,
  COMMON_TAGS
}
