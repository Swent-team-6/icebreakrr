package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.model.profile.MockProfileViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AroundYouTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockProfileViewModel: MockProfileViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockProfileViewModel = MockProfileViewModel()

    `when`(navigationActions.currentRoute()).thenReturn(Route.AROUND_YOU)
    composeTestRule.setContent { AroundYouScreen(navigationActions, mockProfileViewModel) }
  }

  @Test
  fun displayTextWhenEmpty() {
    mockProfileViewModel.clearProfiles()
    composeTestRule.onNodeWithTag("emptyProfilePrompt").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
  }

  @Test
  fun navigationOnCardClick() {
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()
    verify(navigationActions).navigateTo(screen = Screen.PROFILE)
  }

  @Test
  fun navigationOnFabClick() {
    composeTestRule.onAllNodesWithTag("filterButton").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterButton").onFirst().performClick()
    verify(navigationActions).navigateTo(screen = Screen.FILTER)
  }
}
