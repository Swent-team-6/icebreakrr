package com.github.se.icebreakrr.ui.sections

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

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
    profilesViewModelMock = ProfilesViewModel(profilesRepositoryMock)
    filterViewModel = FilterViewModel()
  }

  @Test
  fun testTopAppBarBackButton() {
    composeTestRule.setContent { FilterScreen(navigationActions = navigationActionsMock) }

    // Simulate a click on the "Back" button
    composeTestRule.onNodeWithTag("FilterTopBar").onChildAt(0).performClick()

    // Verify that goBack was called
    verify(navigationActionsMock).goBack()
  }

  @Test
  fun testFilterScreenUI() {
    composeTestRule.setContent { FilterScreen(navigationActions = navigationActionsMock) }

    // Check if the title in the top bar is displayed
    composeTestRule.onNodeWithTag("FilterTopBar").assertIsDisplayed()

    // Check if gender title and buttons are displayed
    composeTestRule.onNodeWithTag("GenderTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderButtonMen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GenderButtonOther").assertIsDisplayed()

    // Check if age range title and input fields are displayed
    composeTestRule.onNodeWithTag("AgeRangeTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AgeFromTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AgeToTextField").assertIsDisplayed()

    // Check if tags title is displayed
    composeTestRule.onNodeWithTag("TagsTitle").assertIsDisplayed()

    // Check if the filter button is displayed
    composeTestRule.onNodeWithTag("FilterButton").assertIsDisplayed()
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
    fromTextField.assert(hasText("abc"))
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
    toTextField.performTextInput("1000")
    toTextField.assert(hasText("1000"))

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
    toTextField.performTextInput("abc")
    toTextField.assert(hasText("abc"))
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

    // Test case 1: Select genders and set valid age range
    composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
    composeTestRule.onNodeWithTag("GenderButtonWomen").performClick()

    // Set valid age range
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextInput("25")
    composeTestRule.onNodeWithTag("AgeToTextField").performTextInput("30")

    // Click the filter button
    composeTestRule.onNodeWithTag("FilterButton").performClick()

    // Assert that the correct age range is set
    assertEquals(25..30, filterViewModel.ageRange.value)

    // Test case 2: No age range (both fields empty)
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextClearance()
    composeTestRule.onNodeWithTag("AgeToTextField").performTextClearance()

    composeTestRule.onNodeWithTag("FilterButton").performClick()

    // Assert that the age range is set to null
    assertNull(filterViewModel.ageRange.value)

    // Test case 3: Only "From" age set
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextInput("25")
    composeTestRule.onNodeWithTag("AgeToTextField").performTextClearance()

    composeTestRule.onNodeWithTag("FilterButton").performClick()

    // Assert that the age range is set to (25..Int.MAX_VALUE)
    assertEquals(25..Int.MAX_VALUE, filterViewModel.ageRange.value)

    // Test case 4: Only "To" age set
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextClearance()
    composeTestRule.onNodeWithTag("AgeToTextField").performTextInput("30")

    composeTestRule.onNodeWithTag("FilterButton").performClick()

    // Assert that the age range is set to (0..30)
    assertEquals(0..30, filterViewModel.ageRange.value)

    // Test case 5: Invalid age range (ageFrom > ageTo)
    composeTestRule.onNodeWithTag("AgeFromTextField").performTextInput("35")
    composeTestRule.onNodeWithTag("AgeToTextField").performTextInput("30")

    composeTestRule.onNodeWithTag("FilterButton").performClick()

    // Assert that the age range is set to null due to error
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
}
