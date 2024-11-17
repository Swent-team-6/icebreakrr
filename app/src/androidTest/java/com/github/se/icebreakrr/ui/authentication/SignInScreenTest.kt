package com.github.se.icebreakrr.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SignInScreenTest {
  private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val tempFolder = TemporaryFolder()

  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions
  private lateinit var profileViewModel: ProfilesViewModel
  private lateinit var filterViewModel: FilterViewModel
  private lateinit var mockTagsRepository: TagsRepository
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var appDataStore: AppDataStore
  private lateinit var meetingRequestViewModel: MeetingRequestViewModel
  private lateinit var functions: FirebaseFunctions
  private lateinit var ourUid: String

  @Before
  fun setUp() {
    // Set up real DataStore with test scope
    testDataStore =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempFolder.newFolder(), "test_preferences.preferences_pb") })
    appDataStore = AppDataStore(testDataStore)

    // Set up other mocks
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    profileViewModel = mock(ProfilesViewModel::class.java)
    filterViewModel = FilterViewModel()
    tagsViewModel =
        TagsViewModel(
            TagsRepository(mock(FirebaseFirestore::class.java), mock(FirebaseAuth::class.java)))
    functions = mock(FirebaseFunctions::class.java)
    ourUid = "UserId1"
    meetingRequestViewModel =
        MeetingRequestViewModel(profileViewModel, functions, ourUid, "My name")
  }

  @Test
  fun titleAndButtonAreCorrectlyDisplayed() = runTest {
    composeTestRule.setContent {
      SignInScreen(
          profileViewModel,
          meetingRequestViewModel,
          navigationActions,
          filterViewModel = filterViewModel,
          tagsViewModel =
              viewModel(
                  factory =
                      TagsViewModel.Companion.Factory(
                          FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())),
          appDataStore = appDataStore)
    }

    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("IceBreakrr")
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
  }
}
