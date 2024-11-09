package com.github.se.icebreakrr.ui.authentication

// This file contains code snippets from the bootcamp
// and has been highly inspired from it

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class LoginTest : TestCase() {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions
  private lateinit var profileViewModel: ProfilesViewModel
  private lateinit var filterViewModel: FilterViewModel
  private lateinit var mockTagsRepository: TagsRepository
  private lateinit var tagsViewModel: TagsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    profileViewModel = mock(ProfilesViewModel::class.java)
    filterViewModel = FilterViewModel()
    mockTagsRepository = mock(TagsRepository::class.java)
    tagsViewModel = TagsViewModel(mockTagsRepository)
  }

  @Test
  fun titleAndButtonAreCorrectlyDisplayed() {
    composeTestRule.setContent {
      SignInScreen(
          profileViewModel,
          navigationActions,
          filterViewModel = filterViewModel,
          tagsViewModel = tagsViewModel)
    }

    composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("IceBreakrr")

    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
  }
}
