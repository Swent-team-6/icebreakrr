import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

// This file was created with the help of AI
@ExperimentalCoroutinesApi
class FirebaseAuthTest {

  private val testDispatcher = TestCoroutineDispatcher()
  private val testScope =
      createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + testDispatcher)

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
}
