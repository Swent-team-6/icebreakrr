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
  return listOf(
      Profile(
          uid = "1",
          name = "Alice Inwonderland",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "So much to see, so little time",
          description = "I am a software engineer who loves to travel and meet new people.",
          tags = listOf("travel", "software", "music"),
          profilePictureUrl = null),
      Profile(
          uid = "2",
          name = "Bob Marley",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Why am I always so hungry?",
          description = "I am a foodie who loves to try new restaurants and cuisines.",
          tags = listOf("food", "cooking", "travel"),
          profilePictureUrl = null),
      Profile(
          uid = "3",
          name = "Charlie Brown",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Good grief!",
          description = "I am a cartoon character who loves to fly kites.",
          tags = listOf("cartoon", "kites", "dog"),
          profilePictureUrl = null),
      Profile(
          uid = "4",
          name = "Diana Prince",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Truth and justice!",
          description = "I am a superhero who fights for justice.",
          tags = listOf("superhero", "justice", "truth"),
          profilePictureUrl = null),
      Profile(
          uid = "5",
          name = "Eve Polastri",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "I am not a psychopath.",
          description = "I am a detective who hunts down assassins.",
          tags = listOf("detective", "thriller", "mystery"),
          profilePictureUrl = null),
      Profile(
          uid = "6",
          name = "Frank Underwood",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Power is everything.",
          description = "I am a politician who will do anything for power.",
          tags = listOf("politics", "power", "drama"),
          profilePictureUrl = null),
      Profile(
          uid = "7",
          name = "Gordon Freeman",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Science rules!",
          description = "I am a scientist who fights aliens.",
          tags = listOf("science", "aliens", "action"),
          profilePictureUrl = null),
      Profile(
          uid = "8",
          name = "Hannah Montana",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Best of both worlds.",
          description = "I am a pop star with a secret identity.",
          tags = listOf("music", "pop", "secret"),
          profilePictureUrl = null),
      Profile(
          uid = "9",
          name = "Indiana Jones",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "It belongs in a museum!",
          description = "I am an archaeologist who loves adventure.",
          tags = listOf("adventure", "archaeology", "history"),
          profilePictureUrl = null),
      Profile(
          uid = "10",
          name = "Jessica Jones",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "I am not a hero.",
          description = "I am a private investigator with super strength.",
          tags = listOf("investigator", "superhero", "strength"),
          profilePictureUrl = null),
      Profile(
          uid = "11",
          name = "Kermit the Frog",
          gender = Gender.MEN,
          birthDate = Timestamp.now(),
          catchPhrase = "It's not easy being green.",
          description = "I am a frog who loves to sing and entertain.",
          tags = listOf("entertainment", "singing", "frog"),
          profilePictureUrl = null),
      Profile(
          uid = "12",
          name = "Lara Croft",
          gender = Gender.WOMEN,
          birthDate = Timestamp.now(),
          catchPhrase = "Adventure awaits.",
          description = "I am an adventurer and archaeologist.",
          tags = listOf("adventure", "archaeology", "action"),
          profilePictureUrl = null))
}
