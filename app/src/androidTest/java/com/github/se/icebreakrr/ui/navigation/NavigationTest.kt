package com.github.se.icebreakrr.ui.navigation

// @RunWith(AndroidJUnit4::class)
// class NavigationTest {
//
//  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//  private lateinit var navigationActions: NavigationActions
//  private lateinit var profilesViewModel: ProfilesViewModel
//  private lateinit var mockProfileViewModel: MockProfileViewModel
//  private lateinit var tagsViewModel: TagsViewModel
//  private lateinit var mockProfilesRepository: ProfilesRepository
//  private lateinit var mockTagsRepository: TagsRepository
//
//  @Before
//  fun setup() {
//    // Initialize the mock ProfileViewModel
//    mockProfileViewModel = MockProfileViewModel()
//    navigationActions = Mockito.mock(NavigationActions::class.java)
//    mockProfilesRepository = Mockito.mock(ProfilesRepository::class.java)
//    mockTagsRepository = Mockito.mock(TagsRepository::class.java)
//
//    tagsViewModel = TagsViewModel(mockTagsRepository)
//
//    profilesViewModel =
//        ProfilesViewModel(mockProfilesRepository, ProfilePicRepositoryStorage(Firebase.storage))
//
//    // Creating a custom flag to disable the sign in to correctly test navigation
//    val intent =
//        Intent(composeTestRule.activity, MainActivity::class.java).apply {
//          putExtra("IS_TESTING", true) // Pass the testing flag via intent
//        }
//
//    ActivityScenario.launch<MainActivity>(intent)
//  }
//
//  @Test
//  fun testNavigationLoginAndBottomNav() {
//    // Assert that the login screen is shown on launch
//    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
//
//    // Simulate clicking the login button
//    composeTestRule.onNodeWithTag("loginButton").performClick()
//
//    // Check that the "Around You" screen is displayed after login
//    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
//
//    // Test navigation to the Settings screen
//    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
//    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
//
//    // Test navigation to the Notifications screen
//    composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()
//    composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
//
//    // Test navigation to the Notifications screen
//    composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()
//    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
//
//    // Test navigation to the Settings screen
//    composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
//    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
//
//    // Test navigation to the Notifications screen
//    composeTestRule.onNodeWithTag("logOutButton").performClick()
//    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
//  }
// }
