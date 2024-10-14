package com.github.se.icebreakrr.ui.tags

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TagViewTest {
  private lateinit var selectedTag: MutableState<List<Pair<String, Color>>>
  private lateinit var outputTag: MutableState<List<Pair<String, Color>>>
  private lateinit var stringQuery: MutableState<String>
  private lateinit var expanded: MutableState<Boolean>
  private lateinit var onClickMock: () -> Unit
  private lateinit var tagSelectorOnClickMock: (String) -> Unit
  private lateinit var tagSelectorOnClickDropDownMenu: (String) -> Unit
  private lateinit var tagSelectorOnStringChanged: (String) -> Unit

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {

    selectedTag =
        mutableStateOf(
            listOf(
                Pair("salsa", Color.Red),
                Pair("pizza", Color.Red),
                Pair("coca-cola", Color.Red),
                Pair("pepsi", Color.Red),
                Pair("fanta", Color.Red)))
    outputTag =
        mutableStateOf(
            listOf(
                Pair("pesto", Color.Green),
                Pair("broccoli", Color.Green),
                Pair("beans", Color.Green),
                Pair("peanut", Color.Green),
                Pair("butter", Color.Green)))
    stringQuery = mutableStateOf("")
    expanded = mutableStateOf(false)
    onClickMock = mock<() -> Unit>()
    tagSelectorOnClickMock = mock<(String) -> Unit>()
    tagSelectorOnStringChanged = mock<(String) -> Unit>()
    tagSelectorOnClickDropDownMenu = mock<(String) -> Unit>()
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
    composeTestRule.onNodeWithTag("clickTestTag").performClick()
    verify(onClickMock).invoke()
  }

  @Test
  fun testRowOfTags() {
    composeTestRule.setContent { RowOfTags(selectedTag.value, TagStyle()) }
    composeTestRule.onAllNodesWithTag("testTag").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertTextEquals("...")
    composeTestRule.onNodeWithTag("testExtendTag").performClick()
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(5)
  }

  @Test
  fun testRowOfClickableTags() {
    composeTestRule.setContent {
      RowOfClickTags(selectedTag.value, TagStyle()) { s -> tagSelectorOnClickMock(s) }
    }
    composeTestRule.onAllNodesWithTag("clickTestTag").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertTextEquals("...")
    composeTestRule.onNodeWithTag("testExtendTag").performClick()
    composeTestRule.onAllNodesWithTag("clickTestTag").assertCountEquals(5)
    composeTestRule.onAllNodesWithText("#salsa x").onFirst().performClick()
    verify(tagSelectorOnClickMock).invoke("salsa")
  }

  @Test
  fun testTagSelector() {
    val textColor = Color.White
    val textSize = 16.sp
    val userInput = "input"

    composeTestRule.setContent {
      TagSelector(
          selectedTag,
          outputTag,
          stringQuery,
          expanded,
          tagSelectorOnClickMock,
          tagSelectorOnClickDropDownMenu,
          tagSelectorOnStringChanged,
          textColor,
          textSize,
          200.dp,
          100.dp
      )
    }
    composeTestRule.onAllNodesWithTag("clickTestTag").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithText("#salsa x").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithText("#salsa x").onFirst().performClick()
    verify(tagSelectorOnClickMock).invoke("salsa")

    composeTestRule.onNodeWithTag("inputTagSelector").performTextClearance()
    composeTestRule.onNodeWithTag("inputTagSelector").performTextInput(userInput)
    verify(tagSelectorOnStringChanged).invoke(userInput)
    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithTag("testTag", useUnmergedTree = true).onFirst().assertExists()
    composeTestRule
        .onAllNodesWithTag("testTag", useUnmergedTree = true)
        .onFirst()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithText("#pesto").onFirst().assertIsDisplayed()

    composeTestRule.onAllNodesWithTag("tagSelectorDropDownMenuItem").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithText("#pesto").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithText("#pesto").onFirst().performClick()
    verify(tagSelectorOnClickDropDownMenu).invoke("pesto")

    composeTestRule.onNodeWithTag("sizeTagSelector").assertWidthIsEqualTo(200.dp).assertHeightIsEqualTo(100.dp)


  }
}
