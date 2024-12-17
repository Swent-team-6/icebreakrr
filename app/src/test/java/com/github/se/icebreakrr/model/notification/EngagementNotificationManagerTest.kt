package com.github.se.icebreakrr.model.notification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilePicRepository
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.utils.IPermissionManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

// This File was written with the help of CursorAI

@OptIn(ExperimentalCoroutinesApi::class)
class EngagementNotificationManagerTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var engagementManager: EngagementNotificationManager
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var appDataStore: AppDataStore
  private lateinit var filterViewModel: FilterViewModel
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var mockProfilesRepo: ProfilesRepository
  private lateinit var permissionManager: IPermissionManager

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
          location = GeoPoint(0.0, 0.0))

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
          distanceToSelfProfile = 5,
          location = GeoPoint(0.0, 0.1))

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())

    // create and configure the mock
    appDataStore = mock()
    mockProfilesRepo = mock(ProfilesRepository::class.java)
    meetingRequestViewModel = mock()
    filterViewModel = mock()
    tagsViewModel = mock()

    // Mock the required DataStore methods with correct property name
    whenever(appDataStore.lastNotificationTimes).thenReturn(MutableStateFlow(mapOf<String, Long>()))
    whenever(appDataStore.isDiscoverable).thenReturn(MutableStateFlow(true))

    // Setup profilesViewModel
    val mockProfilePicRepo = mock(ProfilePicRepository::class.java)
    val mockAuth = mock(FirebaseAuth::class.java)
    permissionManager = mock(IPermissionManager::class.java)
    profilesViewModel = spy(ProfilesViewModel(mockProfilesRepo, mockProfilePicRepo, mockAuth))
    profilesViewModel.selfProfile = MutableStateFlow(selfProfile)

    // Setup filter values
    whenever(filterViewModel.selectedRadius).thenReturn(MutableStateFlow(5000))
    whenever(filterViewModel.selectedGenders)
        .thenReturn(MutableStateFlow(listOf(Gender.MEN, Gender.WOMEN)))
    whenever(filterViewModel.ageRange).thenReturn(MutableStateFlow(18..99))
    whenever(tagsViewModel.filteredTags).thenReturn(MutableStateFlow(listOf("music")))

    // Create manager instance
    engagementManager =
        EngagementNotificationManager(
            profilesViewModel,
            meetingRequestViewModel,
            appDataStore,
            filterViewModel,
            tagsViewModel,
            permissionManager)
  }

  @Test
  fun `test monitoring starts and stops correctly`() {
    engagementManager.startMonitoring()
    assert(engagementManager.isMonitoring())

    engagementManager.stopMonitoring()
    assert(!engagementManager.isMonitoring())
  }

  @Test
  fun `test checkNearbyUsersForCommonTags with valid self profile`() = runTest {
    val isDiscoverableFlow = MutableStateFlow(true)
    whenever(appDataStore.isDiscoverable).thenReturn(isDiscoverableFlow)

    // Mock getSelfProfile callback behavior
    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
        }
        .whenever(profilesViewModel)
        .getSelfProfile(any())

    // Mock all the required flows
    whenever(profilesViewModel.selfProfile).thenReturn(MutableStateFlow(selfProfile))
    whenever(filterViewModel.selectedRadius).thenReturn(MutableStateFlow(5000))
    whenever(filterViewModel.selectedGenders)
        .thenReturn(MutableStateFlow(listOf(Gender.MEN, Gender.WOMEN)))
    whenever(filterViewModel.ageRange).thenReturn(MutableStateFlow(18..99))
    whenever(tagsViewModel.filteredTags).thenReturn(MutableStateFlow(listOf("music")))

    // Mock the filtered profiles result
    whenever(profilesViewModel.filteredProfiles).thenReturn(MutableStateFlow(listOf(nearbyProfile)))

    engagementManager.checkNearbyUsersForCommonTags()

    verify(profilesViewModel)
        .getFilteredProfilesInRadius(
            center = selfProfile.location!!,
            radiusInMeters = 5000,
            genders = listOf(Gender.MEN, Gender.WOMEN),
            ageRange = 18..99,
            tags = listOf("music"))
  }

  @Test
  fun `test checkNearbyUsersForCommonTags when user is not discoverable`() = runTest {
    val isDiscoverableFlow = MutableStateFlow(false)
    whenever(appDataStore.isDiscoverable).thenReturn(isDiscoverableFlow)

    engagementManager.checkNearbyUsersForCommonTags()

    verify(profilesViewModel, never())
        .getFilteredProfilesInRadius(
            center = any(), radiusInMeters = any(), genders = any(), ageRange = any(), tags = any())
  }

  @Test
  fun `test processNearbyProfiles sends notification for matching tags`() = runTest {
    val nearbyProfileWithCommonTag =
        nearbyProfile.copy(tags = listOf("music", "sports"), fcmToken = "testToken123")

    engagementManager.processNearbyProfiles(selfProfile, listOf(nearbyProfileWithCommonTag))

    verify(meetingRequestViewModel)
        .engagementNotification(
            targetToken = "testToken123", tag = "music" // First common tag
            )
  }

  @Test
  fun `test processNearbyProfiles respects notification cooldown`() = runTest {
    val nearbyProfileWithCommonTag =
        nearbyProfile.copy(tags = listOf("music", "sports"), fcmToken = "testToken123")

    engagementManager.processNearbyProfiles(selfProfile, listOf(nearbyProfileWithCommonTag))

    engagementManager.processNearbyProfiles(selfProfile, listOf(nearbyProfileWithCommonTag))

    verify(meetingRequestViewModel, times(1)).engagementNotification(any(), any())
  }

  @Test
  fun `test processNearbyProfiles handles null FCM token`() = runTest {
    val profileWithNullToken = nearbyProfile.copy(tags = listOf("music", "sports"), fcmToken = null)

    engagementManager.processNearbyProfiles(selfProfile, listOf(profileWithNullToken))

    verify(meetingRequestViewModel).engagementNotification(targetToken = "null", tag = "music")
  }

  @Test
  fun `test checkNearbyUsersForCommonTags with null self location`() = runTest {
    val isDiscoverableFlow = MutableStateFlow(true)
    whenever(appDataStore.isDiscoverable).thenReturn(isDiscoverableFlow)

    val profileWithNullLocation = selfProfile.copy(location = null)
    whenever(profilesViewModel.selfProfile).thenReturn(MutableStateFlow(profileWithNullLocation))

    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
        }
        .whenever(profilesViewModel)
        .getSelfProfile(any())

    engagementManager.checkNearbyUsersForCommonTags()

    verify(profilesViewModel, never())
        .getFilteredProfilesInRadius(
            center = any(), radiusInMeters = any(), genders = any(), ageRange = any(), tags = any())
  }

  @Test
  fun `test checkNearbyUsersForCommonTags processes profiles after delay`() = runTest {
    val isDiscoverableFlow = MutableStateFlow(true)
    whenever(appDataStore.isDiscoverable).thenReturn(isDiscoverableFlow)

    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
        }
        .whenever(profilesViewModel)
        .getSelfProfile(any())

    whenever(profilesViewModel.selfProfile).thenReturn(MutableStateFlow(selfProfile))
    whenever(profilesViewModel.filteredProfiles).thenReturn(MutableStateFlow(listOf(nearbyProfile)))

    engagementManager.checkNearbyUsersForCommonTags()
    advanceTimeBy(GET_FILTERED_WAIT_DELAY + 100) // Wait for the delay to complete

    verify(meetingRequestViewModel)
        .engagementNotification(targetToken = nearbyProfile.fcmToken!!, tag = "music")
  }
}
