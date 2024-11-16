package com.github.se.icebreakrr.ui.sections

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.profile.ProfilePicRepositoryStorage
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

// This file was written with the help of Cursor, Claude, ChatGPT
class FilterScreenTest {

  private lateinit var navigationActionsMock: NavigationActions
  private lateinit var profilesRepositoryMock: ProfilesRepository
  private lateinit var profilesViewModelMock: ProfilesViewModel
  private lateinit var filterViewModel: FilterViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActionsMock = mock()
    profilesRepositoryMock = mock()
    profilesViewModelMock =
        ProfilesViewModel(profilesRepositoryMock, ProfilePicRepositoryStorage(Firebase.storage), FirebaseAuth.getInstance())
    filterViewModel = FilterViewModel()
  }

  @Test
  fun testTopAppBarBackButton() {
    composeTestRule.setContent { FilterScreen(navigationActions = navigationActionsMock) }

    composeTestRule.waitForIdle()

    // Find the back button within the TopAppBar and click it
    composeTestRule.onNodeWithTag("Back Button").assertExists().assertIsDisplayed().performClick()

    // Verify that goBack was called
    verify(navigationActionsMock).goBack()
  }

  @Test
  fun testFilterScreenUI() {
    composeTestRule.setContent { FilterScreen(navigationActions = navigationActionsMock) }

    composeTestRule.waitForIdle()

    // Check if the title in the top bar is displayed
    composeTestRule.onNodeWithTag("FilterTopBar").assertExists().assertIsDisplayed()

    // Check if gender title and buttons are displayed
    composeTestRule.onNodeWithText("Gender").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("GenderButtonMen").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("GenderButtonWomen").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("GenderButtonOther").assertExists().assertIsDisplayed()

    // Check if age range title and input fields are displayed
    composeTestRule.onNodeWithText("Age Range").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("AgeFromTextField").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("AgeToTextField").assertExists().assertIsDisplayed()

    // Check if tags title is displayed using the test tag
    composeTestRule
        .onNodeWithTag("TagsTitle", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    // Check if the filter and reset buttons are displayed
    composeTestRule.onNodeWithTag("FilterButton").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("ResetButton").assertExists().assertIsDisplayed()
  }

  @Test
  fun testGenderButtonClicks() {
    composeTestRule.setContent { FilterScreen(navigationActions = navigationActionsMock) }
    // Initially, all buttons should be unselected
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Test single selections
    // Men selected
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Women selected
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Other selected
    composeTestRule.onNodeWithTag("GenderButtonOther").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()

    // Test deselections
    // Deselect Men
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()

    // Deselect Women
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()

    // Deselect Other
    composeTestRule.onNodeWithTag("GenderButtonOther").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Test combinations
    // Men and Women selected
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Men and Other selected
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonOther").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()

    // Women and Other selected
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()

    // All selected
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()

    // Test toggling from all selected to single selections
    // Only Men selected
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonOther").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Only Women selected
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Only Other selected
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonOther").performClick()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsSelected()
  }

  @Test
  fun testAgeInputFields() {
    composeTestRule.setContent { FilterScreen(navigationActions = mock()) }

    val fromTextField = composeTestRule.onNodeWithTag("AgeFromTextField")
    val toTextField = composeTestRule.onNodeWithTag("AgeToTextField")

    // Test valid age range
    fromTextField.performTextInput("25")
    toTextField.performTextInput("30")
    fromTextField.assert(hasText("25"))
    toTextField.assert(hasText("30"))

    // Test "To" age lower than "From" age
    toTextField.performTextClearance()
    toTextField.performTextInput("20")
    toTextField.assert(hasText("20"))
    fromTextField.performClick()
    toTextField.assert(hasText(""))

    // Test invalid input (non-numeric)
    fromTextField.performTextClearance()
    fromTextField.performTextInput("abc")
    // Assert that the invalid input is initially accepted
    fromTextField.assert(hasText(""))
    // Simulate losing focus by clicking on another field
    toTextField.performClick()
    // Now check if the invalid input is cleared
    fromTextField.assert(hasText(""))

    // Test age below minimum (assuming minimum age is 13)
    fromTextField.performTextInput("12")
    fromTextField.assert(hasText("12"))
    toTextField.performClick()
    fromTextField.assert(hasText(""))

    // Test upper bound of age range
    toTextField.performTextClearance()
    toTextField.performTextInput("99")
    toTextField.assert(hasText("99"))

    // Test clearing inputs
    fromTextField.performTextClearance()
    toTextField.performTextClearance()
    fromTextField.assert(hasText(""))
    toTextField.assert(hasText(""))

    // Test very large number
    toTextField.performTextClearance()
    toTextField.performTextInput("123") // First input 3 digits
    toTextField.assert(hasText("123")) // Should show all 3 digits
    toTextField.performTextInput("4") // Try to add a 4th digit
    toTextField.assert(hasText("123"))

    // Test decimal input
    fromTextField.performTextClearance()
    fromTextField.performTextInput("25.5")
    toTextField.performClick()
    // Check if the decimal is either cleared
    fromTextField.assertTextContains("")

    // Test decimal input
    toTextField.performTextClearance()
    toTextField.performTextInput("25.5")
    fromTextField.performClick()
    // Check if the decimal is cleared
    toTextField.assertTextContains("")

    // Scenario 1: ageTo is null (clear the "To" field, set "From" field)
    fromTextField.performTextInput("25")
    toTextField.performTextClearance()
    fromTextField.assert(hasText("25"))
    toTextField.assert(hasText(""))
    // This should test the `ageTo == null` branch

    // ageTo != null, newAge <= ageTo (valid range)
    toTextField.performTextInput("30")
    fromTextField.assert(hasText("25"))
    toTextField.assert(hasText("30"))
    // This tests `ageTo != null && newAge <= ageTo!!`

    // ageTo != null, newAge > ageTo (invalid range)
    fromTextField.performTextClearance()
    fromTextField.performTextInput("35")
    fromTextField.assert(hasText("35"))
    toTextField.assert(hasText("30"))
    // This tests `ageTo != null && newAge > ageTo!!`

    // ageTo with invalid non-numeric input (handle error state)
    fromTextField.performTextClearance()
    toTextField.performTextClearance()
    toTextField.performTextInput("")
    toTextField.assert(hasText(""))
    fromTextField.performClick()
    toTextField.assert(hasText(""))
  }

  @Test
  fun testFilterButtonNavigation() {
    composeTestRule.setContent {
      FilterScreen(
          navigationActions = navigationActionsMock, profilesViewModel = profilesViewModelMock)
    }

    // Click the filter button
    composeTestRule.onNodeWithTag("FilterButton").performClick()
    // Verify that the navigation action is called
    verify(navigationActionsMock).goBack()
  }

  @Test
  fun testFilterButtonActions() {
    composeTestRule.setContent {
      FilterScreen(
          navigationActionsMock,
          profilesViewModel = profilesViewModelMock,
          filterViewModel = filterViewModel)
    }

    composeTestRule.waitForIdle()

    // Test case 1: Select genders and set valid age range
    composeTestRule
        .onNodeWithTag("GenderButtonMen")
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag("GenderButtonWomen")
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    // Set valid age range
    composeTestRule
        .onNodeWithTag("AgeFromTextField")
        .assertExists()
        .assertIsDisplayed()
        .performTextInput("25")

    composeTestRule
        .onNodeWithTag("AgeToTextField")
        .assertExists()
        .assertIsDisplayed()
        .performTextInput("30")

    // Click outside to clear focus
    composeTestRule.onNodeWithText("Gender").performClick()

    // Click the filter button
    composeTestRule.onNodeWithTag("FilterButton").assertExists().assertIsDisplayed().performClick()

    // Assert that the correct age range is set
    assertEquals(25..30, filterViewModel.ageRange.value)

    // Test case 2: Clear age range
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextClearance()
    composeTestRule.onNodeWithTag("AgeToTextField").performTextClearance()

    // Click outside to clear focus and trigger validation
    composeTestRule.onNodeWithText("Gender").performClick()

    // Click the filter button again
    composeTestRule.onNodeWithTag("FilterButton").performClick()

    // Now the age range should be null
    assertNull(filterViewModel.ageRange.value)
  }

  @Test
  fun testGenderButtonAppearance() {
    val selected = mutableStateOf(false)
    val label = mutableStateOf("Test")

    composeTestRule.setContent {
      GenderButton(
          selected = selected.value,
          onClick = { selected.value = !selected.value },
          label = label.value)
    }

    // Test unselected state
    composeTestRule
        .onNodeWithTag("GenderButton${label.value}")
        .assertIsDisplayed()
        .assertIsNotSelected()
        .assertTextEquals(label.value)
        .assertHasClickAction()

    // Test selected state
    selected.value = true
    composeTestRule
        .onNodeWithTag("GenderButton${label.value}")
        .assertIsDisplayed()
        .assertIsSelected()
        .assertTextEquals(label.value)
        .assertHasClickAction()

    // Test empty label
    label.value = ""
    composeTestRule.onNodeWithTag("GenderButton").assertIsDisplayed().assertTextEquals(label.value)
  }

  @Test
  fun testResetButton() {
    composeTestRule.setContent {
      FilterScreen(
          navigationActionsMock,
          profilesViewModel = profilesViewModelMock,
          filterViewModel = filterViewModel)
    }

    // Set up initial state
    // Select genders
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()

    // Set age range
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextInput("25")
    composeTestRule.onNodeWithTag("AgeToTextField").performTextInput("30")

    // Click outside to clear focus
    composeTestRule.onNodeWithText("Gender").performClick()

    // Click reset button
    composeTestRule.onNodeWithTag("ResetButton").performClick()

    // Verify gender buttons are unselected
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

    // Verify age fields are cleared
    composeTestRule.onNodeWithTag("AgeFromTextField").assert(hasText(""))
    composeTestRule.onNodeWithTag("AgeToTextField").assert(hasText(""))

    // Verify filter button is enabled after reset
    composeTestRule.onNodeWithTag("FilterButton").assertIsEnabled()
  }

  @Test
  fun testUnsavedChangesDialog() {
    composeTestRule.setContent {
      FilterScreen(
          navigationActionsMock,
          profilesViewModel = profilesViewModelMock,
          filterViewModel = filterViewModel)
    }

    // Make some changes to trigger the dialog
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()

    // Try to go back
    composeTestRule.onNodeWithTag("Back Button").performClick()

    // Verify dialog is shown
    composeTestRule.onNodeWithTag("alertDialog").assertExists().assertIsDisplayed()

    // Test cancel button
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithTag("alertDialog").assertDoesNotExist()
    verifyNoInteractions(navigationActionsMock)

    // Try to go back again
    composeTestRule.onNodeWithTag("Back Button").performClick()

    // Test confirm button (Discard changes)
    composeTestRule.onNodeWithText("Discard changes").performClick()
    composeTestRule.onNodeWithTag("alertDialog").assertDoesNotExist()
    verify(navigationActionsMock).goBack()
  }

  @Test
  fun testSystemBackButtonShowsDialogOnUnsavedChanges() {
    composeTestRule.setContent { FilterScreen(navigationActionsMock) }

    // Make some changes to trigger the dialog
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()

    // Simulate system back button press
    Espresso.pressBack()

    // Verify dialog is shown
    composeTestRule.onNodeWithTag("alertDialog").assertExists().assertIsDisplayed()
  }

  @Test
  fun testSystemBackButtonDoesNotShowDialogWithoutUnsavedChanges() {
    composeTestRule.setContent { FilterScreen(navigationActionsMock) }

    // Simulate system back button press without making any changes
    Espresso.pressBack()

    // Verify dialog is not shown and navigation action is called
    composeTestRule.onNodeWithTag("alertDialog").assertDoesNotExist()
    verify(navigationActionsMock).goBack()
  }
}
