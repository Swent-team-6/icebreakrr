package com.github.se.icebreakrr.model.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiRepository: AiRepository,
    private val profilesViewModel: ProfilesViewModel
) : ViewModel() {

  sealed class UiState {
    object Idle : UiState() // Initial state

    object Loading : UiState() // API call in progress

    data class Success(val data: String) : UiState() // Successful API response

    data class Error(val message: String) : UiState() // Error occurred
  }

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  val uiState: StateFlow<UiState>
    get() = _uiState

  private val systemPrompt =
      """
        I will provide metadata for two users: "selfProfile" and "selectedProfile." Based on their profiles, 
        generate a fun, engaging, and friendly icebreaker sentence that selfProfile can use to start a 
        conversation with selectedProfile. Ensure the icebreaker is personalized, highlights a shared interest 
        or unique trait, and sets a positive tone for a friendly chat.
    """
          .trimIndent()

  private val _userPrompt = MutableStateFlow("")
  val userPrompt: StateFlow<String>
    get() = _userPrompt

  init {
    observeProfiles() // Start observing profile changes
  }

  /**
   * Initiates an API call to generate a discussion starter. Updates the UI state based on the
   * result.
   */
  fun findDiscussionStarter() {
    _uiState.value = UiState.Loading

    viewModelScope.launch {
      try {
        // Prepare the AI request
        val prompt = userPrompt.value
        val request = AiRequest(systemPrompt = systemPrompt, userPrompt = prompt)
        // Call the repository and update state with the result
        val response = aiRepository.generateResponse(request)
        _uiState.value = UiState.Success(response)
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Sorry, an error occurred") // Handle errors
      }
    }
  }

  /**
   * Observes changes in profiles and dynamically generates the user prompt. Combines data from
   * `selfProfile` and `selectedProfile`.
   */
  private fun observeProfiles() {
    viewModelScope.launch {
      profilesViewModel.selfProfile
          .combine(profilesViewModel.selectedProfile) { selfProfile, selectedProfile ->
            // Combine profiles into a single prompt string
            buildString {
              append("Here are the details of two profiles:\n\n")
              append("selfProfile:\n")
              appendProfileDetails(selfProfile)
              append("\n\nselectedProfile:\n")
              appendProfileDetails(selectedProfile)
            }
          }
          .collect { prompt ->
            _userPrompt.value = prompt // Update the user prompt
          }
    }
  }

  /**
   * Appends the details of a profile to the string builder.
   *
   * @param profile The profile whose details are appended.
   */
  private fun StringBuilder.appendProfileDetails(profile: Profile?) {
    if (profile == null) {
      append("Profile not available.\n")
      return
    }
    append("Name: ${profile.name}\n")
    append("Gender: ${profile.gender}\n")
    append("Birth Date: ${profile.birthDate.toDate()}\n")
    append("Catchphrase: ${profile.catchPhrase}\n")
    append("Description: ${profile.description}\n")
    append("Tags: ${profile.tags.joinToString(", ")}\n")
  }

  companion object {
    /**
     * Provides a factory for creating the ViewModel with required dependencies.
     *
     * @param apiKey The API key for authenticating AI requests.
     * @param profilesViewModel The ViewModel managing profile data.
     * @return A [ViewModelProvider.Factory] for creating [AiViewModel].
     */
    fun provideFactory(
        apiKey: String,
        profilesViewModel: ProfilesViewModel
    ): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          if (modelClass.isAssignableFrom(AiViewModel::class.java)) {
            val aiRepository = AiRepositoryChatGPT(apiKey) // Create the repository
            return AiViewModel(aiRepository, profilesViewModel) as T
          }
          throw IllegalArgumentException("Unknown ViewModel class")
        }
      }
    }
  }
}
