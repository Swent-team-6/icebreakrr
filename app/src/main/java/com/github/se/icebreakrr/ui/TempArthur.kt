package com.github.se.icebreakrr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Profile(
    val uid: String,
    val name: String,
    val gender: Gender,
    val birthDate: Timestamp,
    val catchPhrase: String,
    val description: String,
    val tags: List<String>,
    val profilePictureUrl: String? = null
) {
  companion object {
    fun getMockedProfiles(): List<Profile> {
      return listOf(
          Profile(
              uid = "1",
              name = "Alice Inwonderland",
              gender = Gender.WOMAN,
              birthDate = Timestamp.now(),
              catchPhrase = "So much to see, so little time",
              description = "I am a software engineer who loves to travel and meet new people.",
              tags = listOf("travel", "software", "music"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
          Profile(
              uid = "2",
              name = "Bob Marley",
              gender = Gender.MAN,
              birthDate = Timestamp.now(),
              catchPhrase = "Why am I always so hungry?",
              description = "I am a foodie who loves to try new restaurants and cuisines.",
              tags = listOf("food", "cooking", "travel"),
              profilePictureUrl = null),
      )
    }
  }
}

enum class Gender(val displayName: String) {
  WOMAN("Woman"),
  MAN("Man"),
  OTHER("Other")
}

class ProfileViewModel : ViewModel() {
  private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
  val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

  private val _selectedProfile = MutableStateFlow<Profile?>(null)
  val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

  init {
    getProfiles()
  }

  private fun getProfiles() {
    _profiles.value = Profile.getMockedProfiles()
  }

  // create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel() as T
          }
        }
  }
}
