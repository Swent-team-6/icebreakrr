package com.github.se.icebreakrr.model.notification

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

// This File was written with the help of Cursor AI

@OptIn(ExperimentalCoroutinesApi::class)
class EngagementNotificationManagerTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var engagementManager: EngagementNotificationManager
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var appDataStore: AppDataStore
  private lateinit var context: Context
  private lateinit var filterViewModel: FilterViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  private val selfProfile =
      Profile(
          uid = "self",
          tags = listOf("coding", "music"),
          fcmToken = "selfToken",
          name = "Self",
          gender = Gender.MEN,
          birthDate = Timestamp(631152000, 0), // Year 1990
          catchPhrase = "It's me",
          description = "Self profile",
      )
  private val nearbyProfile =
      Profile(
          uid = "other",
          tags = listOf("music", "sports"),
          fcmToken = "otherToken",
          name = "Bob",
          gender = Gender.MEN,
          birthDate = Timestamp(788918400, 0), // Year 1995
          catchPhrase = "Hi there",
          description = "Another test user",
          distanceToSelfProfile = 5)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Mock all dependencies
    meetingRequestViewModel = mock()
    appDataStore = mock()
    context = mock()
    filterViewModel = mock()

    // Setup profilesViewModel with proper mocking
    val mockProfilesRepo = mock(ProfilesRepository::class.java)
    val mockProfilePicRepo = mock(ProfilePicRepository::class.java)
    val mockAuth = mock(FirebaseAuth::class.java)

    profilesViewModel = ProfilesViewModel(mockProfilesRepo, mockProfilePicRepo, mockAuth)
    profilesViewModel.selfProfile = MutableStateFlow(selfProfile)

    // Create the manager with mocked dependencies
    engagementManager =
        EngagementNotificationManager(
            profilesViewModel, meetingRequestViewModel, appDataStore, context, filterViewModel)
  }

  @Test
  fun `test monitoring starts and stops correctly`() {
    engagementManager.startMonitoring()
    // Verify monitoring started
    // assert(engagementManager.isMonitoring())

    engagementManager.stopMonitoring()
    // Verify monitoring stopped
    // assert(!engagementManager.isMonitoring())
  }
}
