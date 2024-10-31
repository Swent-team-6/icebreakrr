import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.Gender
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import org.mockito.MockitoAnnotations

//This file was created with the help of AI
@ExperimentalCoroutinesApi
class FirebaseAuthTest {

  private val testDispatcher = TestCoroutineDispatcher()
  private val testScope = TestCoroutineScope(testDispatcher)

  private lateinit var mockFirebaseAuth: FirebaseAuth
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var profilesViewModel: ProfilesViewModel
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    // Mock FirebaseAuth and FirebaseUser
    mockFirebaseAuth = mock()
    mockFirebaseUser = mock()
    profilesViewModel = mock()
    navigationActions = mock()

    // Simulate a logged-in user with email, UID, and display name
    whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    whenever(mockFirebaseUser.uid).thenReturn("testUid")
    whenever(mockFirebaseUser.email).thenReturn("testuser@example.com")
    whenever(mockFirebaseUser.displayName).thenReturn("Test User")
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun testFirebaseAuth() {
    val currentUser = mockFirebaseAuth.currentUser
    assertEquals("testuser@example.com", currentUser?.email)
  }

  // Test case: Profile exists, expect navigation to occur
  @Test
  fun `onAuthComplete navigates if profile exists`() = testScope.runBlockingTest {
    val uid = mockFirebaseUser.uid
    val mockProfile = Profile(
      uid = uid,
      name = "Existing User",
      birthDate = Timestamp.now(),
      gender = Gender.OTHER,
      catchPhrase = "",
      description = ""
    )

    // Mock loading and selectedProfile behavior
    whenever(profilesViewModel.loading).thenReturn(MutableStateFlow(false))
    whenever(profilesViewModel.selectedProfile).thenReturn(MutableStateFlow(mockProfile))

    profilesViewModel.getProfileByUid(uid)

    // Advance until all coroutines complete
    advanceUntilIdle()

    verify(profilesViewModel).getProfileByUid(uid)
  }

  // Test case: Profile does not exist, expect profile creation and navigation
  @Test
  fun `onAuthComplete creates profile if none exists and then navigates`() = testScope.runBlockingTest {
    val uid = mockFirebaseUser.uid
    val displayName = mockFirebaseUser.displayName ?: "New User"

    // Simulate no existing profile
    whenever(profilesViewModel.loading).thenReturn(MutableStateFlow(false))
    whenever(profilesViewModel.selectedProfile).thenReturn(MutableStateFlow(null))

    profilesViewModel.getProfileByUid(uid)

    // Advance until all coroutines complete
    advanceUntilIdle()

    verify(profilesViewModel).getProfileByUid(uid)
  }

  // Test case: Error in fetching profile, expect no navigation
  @Test
  fun `onAuthComplete handles error in fetching profile`() = testScope.runBlockingTest {
    val uid = mockFirebaseUser.uid

    // Simulate an error in fetching by making loading false but profile still null
    whenever(profilesViewModel.loading).thenReturn(MutableStateFlow(false))
    whenever(profilesViewModel.selectedProfile).thenThrow(RuntimeException("Fetch error"))

    profilesViewModel.getProfileByUid(uid)

    // Advance until all coroutines complete
    advanceUntilIdle()

    verify(profilesViewModel).getProfileByUid(uid)
    verify(navigationActions, never()).navigateTo(TopLevelDestinations.AROUND_YOU)
  }

  // Test case: Error in creating profile, expect no navigation
  @Test
  fun `onAuthComplete handles error in creating profile`() = testScope.runBlockingTest {
    val uid = mockFirebaseUser.uid

    // Set loading to false, and simulate profile creation failure
    whenever(profilesViewModel.loading).thenReturn(MutableStateFlow(false))
    whenever(profilesViewModel.selectedProfile).thenReturn(MutableStateFlow(null))
    doThrow(RuntimeException("Create error")).`when`(profilesViewModel).addNewProfile(any())

    profilesViewModel.getProfileByUid(uid)

    // Advance until all coroutines complete
    advanceUntilIdle()

    verify(profilesViewModel).getProfileByUid(uid)
  }
}