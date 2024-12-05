package com.github.se.icebreakrr.model.ai

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull

@OptIn(ExperimentalCoroutinesApi::class)
class AiViewModelTest {

  @get:Rule
  val instantExecutorRule = InstantTaskExecutorRule() // Ensure LiveData works on the main thread

  private val testDispatcher = StandardTestDispatcher()

  @Mock private lateinit var aiRepository: AiRepository

  @Mock private lateinit var profilesRepository: ProfilesRepository

  @Mock private lateinit var profilePicRepository: ProfilePicRepository

  @Mock private lateinit var auth: FirebaseAuth

  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var aiViewModel: AiViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    // Create a real instance of ProfilesViewModel
    profilesViewModel = ProfilesViewModel(profilesRepository, profilePicRepository, auth)

    // Initialize AiViewModel
    aiViewModel = AiViewModel(aiRepository, profilesViewModel)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // Reset main dispatcher after tests
  }

  @Test
  fun `findDiscussionStarter sets UI state to Loading and then Success after API call`() = runTest {
    // Mock a delayed API response
    val mockResponse = "Hello, this is your icebreaker!"
    `when`(aiRepository.generateResponse(anyOrNull())).thenReturn(mockResponse)

    // Trigger the method to test
    aiViewModel.findDiscussionStarter()

    // Assert UI state is Loading initially
    assertEquals(AiViewModel.UiState.Loading, aiViewModel.uiState.value)

    // Advance time to complete the API call
    advanceUntilIdle()

    // Assert UI state is Success with the mock response
    assertEquals(AiViewModel.UiState.Success(mockResponse), aiViewModel.uiState.value)
  }

  @Test
  fun `observeProfiles updates user prompt correctly`() = runTest {
    // Advance coroutine to collect changes
    advanceUntilIdle()

    // Expected prompt format (the profiles will be in their initial states)
    val expectedPrompt =
        """
        Here are the details of two profiles:
        
        selfProfile:
        Profile not available.


        selectedProfile:
        Profile not available.
        
    """
            .trimIndent()

    // Collect the current value of userPrompt
    val actualPrompt = aiViewModel.userPrompt.value

    // Assert that the userPrompt matches the expected prompt
    assertEquals(expectedPrompt, actualPrompt)
  }
}
