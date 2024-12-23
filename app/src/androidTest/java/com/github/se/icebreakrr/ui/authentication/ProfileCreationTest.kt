package com.github.se.icebreakrr.ui.authentication

import ProfileCreationScreen
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.mock.MockProfileViewModel
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ProfileCreationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var tagsViewModel: TagsViewModel
  private lateinit var fakeProfilesViewModel: MockProfileViewModel
  private lateinit var mockProfilesRepository: ProfilesRepository
  private lateinit var mockTagsRepository: TagsRepository

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockProfilesRepository = mock(ProfilesRepository::class.java)

    tagsViewModel =
        TagsViewModel(
            TagsRepository(mock(FirebaseFirestore::class.java), mock(FirebaseAuth::class.java)))
    fakeProfilesViewModel = MockProfileViewModel()
  }

  @Test
  fun testProfileCreationScreenUI() {
    composeTestRule.setContent {
      ProfileCreationScreen(tagsViewModel, fakeProfilesViewModel, navigationActions)
    }

    composeTestRule.onNodeWithTag("profileCreationContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fullName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("birthdate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("genderSelection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("catchphrase").assertIsDisplayed()
    composeTestRule.onNodeWithTag("description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sizeTagSelector").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
  }

  @Test
  fun testProfileCreationInputs() {
    composeTestRule.setContent {
      ProfileCreationScreen(tagsViewModel, fakeProfilesViewModel, navigationActions)
    }

    // Test text input fields
    composeTestRule.onNodeWithTag("fullName").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("fullName").assertTextContains("John Doe")

    composeTestRule.onNodeWithTag("catchphrase").performTextInput("Hello World!")
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains("Hello World!")

    // Test text clearance
    composeTestRule.onNodeWithTag("catchphrase").performTextClearance()
    composeTestRule.onNodeWithTag("catchphrase").assertTextContains("")

    // Test description with long text
    val longText = "A".repeat(1000)
    composeTestRule.onNodeWithTag("description").performTextInput(longText)
    composeTestRule.onNodeWithTag("description").assertTextContains(longText.take(1000))

    // Test description clearance
    composeTestRule.onNodeWithTag("description").performTextClearance()
    composeTestRule.onNodeWithTag("description").assertTextContains("")

    // Test normal description
    composeTestRule.onNodeWithTag("description").performTextInput("This is my description")
    composeTestRule.onNodeWithTag("description").assertTextContains("This is my description")
  }

  @Test
  fun testInvalidProfileCreation() {
    composeTestRule.setContent {
      ProfileCreationScreen(tagsViewModel, fakeProfilesViewModel, navigationActions)
    }

    // Try to confirm without filling required fields
    composeTestRule.onNodeWithTag("confirmButton").performClick()

    // Verify that navigation didn't occur
    verify(navigationActions, Mockito.never()).navigateTo(TopLevelDestinations.AROUND_YOU)
  }

  @Test
  fun testGenderSelection() {
    composeTestRule.setContent {
      ProfileCreationScreen(tagsViewModel, fakeProfilesViewModel, navigationActions)
    }

    // Test each gender option
    composeTestRule.onNodeWithTag("genderSelection").performClick()
    composeTestRule.onNodeWithText("Men").performClick()

    composeTestRule.onNodeWithTag("genderSelection").performClick()
    composeTestRule.onNodeWithText("Women").performClick()

    composeTestRule.onNodeWithTag("genderSelection").performClick()
    composeTestRule.onNodeWithText("Other").performClick()
  }

  @Test
  fun testProfileCreationTitle() {
    composeTestRule.setContent {
      ProfileCreationScreen(tagsViewModel, fakeProfilesViewModel, navigationActions)
    }

    composeTestRule
        .onNodeWithTag("profileCreationTitle")
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Create Profile")
  }

  @Test
  fun testBackButtoDoesNothing() {
    composeTestRule.setContent {
      ProfileCreationScreen(tagsViewModel, fakeProfilesViewModel, navigationActions)
    }

    Espresso.pressBack()

    // Verify that navigation didn't occur
    verify(navigationActions, Mockito.never()).navigateTo(TopLevelDestinations.AROUND_YOU)
  }
}
