package com.github.se.icebreakrr.model.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class MockProfileViewModel : ViewModel() {
  private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
  val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

  private val _selectedProfile = MutableStateFlow<Profile?>(null)
  val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

  init {
    getProfiles()
  }

  fun getProfiles() {
    _profiles.value = Profile.getMockedProfiles()
  }

  fun clearProfiles() {
    _profiles.value = emptyList()
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MockProfileViewModel() as T
          }
        }
  }
}

fun Profile.Companion.getMockedProfiles(): List<Profile> {
  val uids = List(12) { "$it" }
  val names =
      listOf(
          "Alice Inwonderland",
          "Bob Marley",
          "Charlie Brown",
          "Diana Prince",
          "Eve Polastri",
          "Frank Underwood",
          "Gordon Freeman",
          "Hannah Montana",
          "Indiana Jones",
          "Jessica Jones",
          "Kermit the Frog",
          "Lara Croft")
  val genders =
      listOf(
          Gender.WOMEN,
          Gender.MEN,
          Gender.MEN,
          Gender.WOMEN,
          Gender.WOMEN,
          Gender.MEN,
          Gender.MEN,
          Gender.WOMEN,
          Gender.MEN,
          Gender.WOMEN,
          Gender.MEN,
          Gender.WOMEN)
  val catchPhrases =
      listOf(
          "So much to see, so little time",
          "Why am I always so hungry?",
          "Good grief!",
          "Truth and justice!",
          "I am not a psychopath.",
          "Power is everything.",
          "Science rules!",
          "Best of both worlds.",
          "It belongs in a museum!",
          "I am not a hero.",
          "It's not easy being green.",
          "Adventure awaits.")
  val descriptions =
      listOf(
          "I am a software engineer who loves to travel and meet new people.",
          "I am a foodie who loves to try new restaurants and cuisines.",
          "I am a cartoon character who loves to fly kites.",
          "I am a superhero who fights for justice.",
          "I am a detective who hunts down assassins.",
          "I am a politician who will do anything for power.",
          "I am a scientist who fights aliens.",
          "I am a pop star with a secret identity.",
          "I am an archaeologist who loves adventure.",
          "I am a private investigator with super strength.",
          "I am a frog who loves to sing and entertain.",
          "I am an adventurer and archaeologist.")
  val tagsList =
      listOf(
          listOf("travel", "software", "music"),
          listOf("food", "cooking", "travel"),
          listOf("cartoon", "kites", "dog"),
          listOf("superhero", "justice", "truth"),
          listOf("detective", "thriller", "mystery"),
          listOf("politics", "power", "drama"),
          listOf("science", "aliens", "action"),
          listOf("music", "pop", "secret"),
          listOf("adventure", "archaeology", "history"),
          listOf("investigator", "superhero", "strength"),
          listOf("entertainment", "singing", "frog"),
          listOf("adventure", "archaeology", "action"))

  val profiles = mutableListOf<Profile>()
  for (i in uids.indices) {
    profiles.add(
        Profile(
            uid = uids[i],
            name = names[i],
            gender = genders[i],
            birthDate = Timestamp.now(),
            catchPhrase = catchPhrases[i],
            description = descriptions[i],
            tags = tagsList[i],
            profilePictureUrl = null))
  }
  return profiles
}
