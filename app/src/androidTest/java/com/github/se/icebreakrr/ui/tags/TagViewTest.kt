package com.github.se.icebreakrr.ui.tags

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.sp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TagViewTest {
  private lateinit var profileTag: MutableState<List<Pair<String, Color>>>
  private lateinit var outputTag: MutableState<List<Pair<String, Color>>>
  private lateinit var stringQuery: MutableState<String>
  private lateinit var expanded: MutableState<Boolean>
  private lateinit var onClickMock: () -> Unit
  private lateinit var tagSelectorOnClickMock: (String) -> Unit
  private lateinit var tagSelectorOnStringChanged: (String) -> Unit

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {

    profileTag = mutableStateOf(listOf(Pair("salsa", Color.Red)))
    outputTag = mutableStateOf(listOf(Pair("pesto", Color.Green)))
    stringQuery = mutableStateOf("")
    expanded = mutableStateOf(false)
    onClickMock = mock<() -> Unit>()
    tagSelectorOnClickMock = mock<(String) -> Unit>()
    tagSelectorOnStringChanged = mock<(String) -> Unit>()

    // `when`(tagsViewModel.outputTags).thenReturn(mutableStateOf(listOf(Pair("Tennis",
    // Color.Red))))
  }

  @Test
  fun displayTag() {
    composeTestRule.setContent { Tag("AndroidTest", TagStyle()) }
    val node = composeTestRule.onNodeWithTag("testTag")
    node.assertIsDisplayed()
    node.assertTextEquals("#AndroidTest")
  }

  @Test
  fun displayClickTag() {
    composeTestRule.setContent { ClickTag("AndroidTest", TagStyle(), {}) }
    composeTestRule.onNodeWithTag("clickTestTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("clickTestTag").assertTextEquals("#AndroidTest x")
  }

  @Test
  fun testClickTag_onClickCalled() {
    composeTestRule.setContent { ClickTag("AndroidTest", TagStyle(), onClickMock) }

    // Simulate a click on the node with the test tag
    composeTestRule.onNodeWithTag("clickTestTag").performClick()

    // Verify that the onClick function was called
    verify(onClickMock).invoke()
  }

  @Test
  fun testTagSelector() {
    val textColor = Color.White
    val textSize = 16.sp
    val userInput = "input"

    composeTestRule.setContent {
      TagSelector(
          profileTag,
          outputTag,
          stringQuery,
          expanded,
          tagSelectorOnClickMock,
          tagSelectorOnStringChanged,
          textColor,
          textSize)
    }

    composeTestRule.onNodeWithTag("clickTestTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("clickTestTag").assertTextEquals("#salsa x")
    composeTestRule.onNodeWithTag("inputTagSelector").performTextClearance()
    composeTestRule.onNodeWithTag("inputTagSelector").performTextInput(userInput)
    verify(tagSelectorOnStringChanged).invoke(userInput)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("testTag", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("testTag", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("testTag", useUnmergedTree = true).assertTextEquals("#pesto")
  }
}
