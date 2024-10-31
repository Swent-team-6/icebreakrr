package com.github.se.icebreakrr.ui.tags

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
  private lateinit var selectedTag: List<Pair<String, Color>>
  private lateinit var outputTag: List<Pair<String, Color>>
  private lateinit var stringQuery: String
  private lateinit var expanded: MutableState<Boolean>
  private lateinit var onClickMock: () -> Unit
  private lateinit var tagSelectorOnClickMock: (String) -> Unit
  private lateinit var tagSelectorOnClickDropDownMenu: (String) -> Unit
  private lateinit var tagSelectorOnStringChanged: (String) -> Unit

  private lateinit var onClickMock1: () -> Unit
  private lateinit var onClickMock2: () -> Unit
  private lateinit var onClickMock3: () -> Unit

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {

    selectedTag =
        listOf(
            Pair("salsa", Color.Red),
            Pair("pizza", Color.Red),
            Pair("coca-cola", Color.Red),
            Pair("pepsi", Color.Red),
            Pair("fanta", Color.Red))
    outputTag =
        listOf(
            Pair("pesto", Color.Green),
            Pair("broccoli", Color.Green),
            Pair("beans", Color.Green),
            Pair("peanut", Color.Green),
            Pair("butter", Color.Green))
    stringQuery = ""
    expanded = mutableStateOf(false)
    onClickMock = mock<() -> Unit>()
    tagSelectorOnClickMock = mock<(String) -> Unit>()
    tagSelectorOnStringChanged = mock<(String) -> Unit>()
    tagSelectorOnClickDropDownMenu = mock<(String) -> Unit>()
    onClickMock1 = mock<() -> Unit>()
    onClickMock2 = mock<() -> Unit>()
    onClickMock3 = mock<() -> Unit>()
  }

  @Test
  fun displayTag() {
    composeTestRule.setContent { Tag("AndroidTest", TagStyle()) }
    val node = composeTestRule.onNodeWithTag("testTag")
    node.assertIsDisplayed()
    node.assertTextEquals("#AndroidTest")
  }

  @Test
  fun displayTagTextColor() {
    composeTestRule.setContent { Tag("AndroidTest", TagStyle(textColor = Color.White)) }
    val node = composeTestRule.onNodeWithTag("testTag")
    node.assertIsDisplayed()
    node.assertTextEquals("#AndroidTest")
  }

  @Test
  fun displayTagBackgroundColor() {
    composeTestRule.setContent { Tag("AndroidTest", TagStyle(backGroundColor = Color.White)) }
    val node = composeTestRule.onNodeWithTag("testTag")
    node.assertIsDisplayed()
    node.assertTextEquals("#AndroidTest")
  }

  @Test
  fun testTag_withDifferentTagStyles() {
    val tagStyle1 = TagStyle(Color.Red, Color.Yellow, 14.sp)
    val tagStyle2 = TagStyle(Color.Blue, Color.Green, 20.sp)
    val tagStyle3 = TagStyle(Color.Black, Color.White, 16.sp)

    // Test with different tag styles to cover different visual combinations
    composeTestRule.setContent {
      Column {
        Tag("Sample1", tagStyle1)
        Tag("Sample2", tagStyle2)
        Tag("Sample3", tagStyle3)
      }
    }

    // Assert that all tags are displayed with the correct text
    composeTestRule.onAllNodesWithText("#Sample1").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#Sample2").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#Sample3").assertCountEquals(1)
  }

  @Test
  fun testTag_withDifferentTexts() {
    val tagStyle = TagStyle(Color.Magenta, Color.Cyan, 12.sp)

    composeTestRule.setContent {
      Column {
        Tag("NormalText", tagStyle)
        Tag("", tagStyle)
        Tag("1234", tagStyle)
        Tag("Special_Char$", tagStyle)
      }
    }

    // Verify that all the text nodes are displayed correctly
    composeTestRule.onNodeWithText("#NormalText").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("#")
        .assertIsDisplayed() // For empty string, it should still show "#"
    composeTestRule.onNodeWithText("#1234").assertIsDisplayed()
    composeTestRule.onNodeWithText("#Special_Char$").assertIsDisplayed()
  }

  @Test
  fun testTag_modifierChecks() {
    val tagStyle = TagStyle(Color.Gray, Color.LightGray, 18.sp)

    composeTestRule.setContent { Tag("TestModifier", tagStyle) }

    // Check if the tag has the correct background color and padding applied
    val node = composeTestRule.onNodeWithTag("testTag")
    node.assertIsDisplayed()

    // Optionally, you could use a custom matcher to verify the padding and background shape,
    // but that would require additional custom assertions.
  }

  @Test
  fun displayClickTag() {
    composeTestRule.setContent { ClickTag("AndroidTest", TagStyle(), {}) }
    composeTestRule.onNodeWithTag("clickTestTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("clickTestTag").assertTextEquals("#AndroidTest x")
  }

  @Test
  fun testClickTagOnClickCalled() {
    composeTestRule.setContent { ClickTag("AndroidTest", TagStyle(), onClickMock) }
    composeTestRule.onNodeWithTag("clickTestTag").performClick()
    verify(onClickMock).invoke()
  }

  @Test
  fun testClickTag_withDifferentTagStyles() {
    val tagStyle1 = TagStyle(Color.Yellow, Color.Black, 12.sp)
    val tagStyle2 = TagStyle(Color.Green, Color.Blue, 18.sp)
    val tagStyle3 = TagStyle(Color.Cyan, Color.Magenta, 14.sp)

    composeTestRule.setContent {
      Column {
        ClickTag("Tag1", tagStyle1, onClickMock1)
        ClickTag("Tag2", tagStyle2, onClickMock2)
        ClickTag("Tag3", tagStyle3, onClickMock3)
      }
    }

    // Assert that all tags are displayed correctly
    composeTestRule.onAllNodesWithText("#Tag1 x").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#Tag2 x").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#Tag3 x").assertCountEquals(1)

    composeTestRule.onAllNodesWithText("#Tag1 x").onFirst().performClick()
    verify(onClickMock1).invoke()
    composeTestRule.onAllNodesWithText("#Tag2 x").onFirst().performClick()
    verify(onClickMock2).invoke()
    composeTestRule.onAllNodesWithText("#Tag3 x").onFirst().performClick()
    verify(onClickMock3).invoke()
  }

  @Test
  fun testClickTag_withDifferentTexts() {
    val tagStyle = TagStyle(Color.Gray, Color.LightGray, 16.sp)

    composeTestRule.setContent {
      Column {
        ClickTag("Normal", tagStyle, {})
        ClickTag("", tagStyle, {}) // Test with empty text
        ClickTag("1234", tagStyle, {}) // Test with numeric text
        ClickTag("Special@Tag", tagStyle, {}) // Test with special characters
      }
    }

    // Verify that all the text nodes are displayed correctly
    composeTestRule.onNodeWithText("#Normal x").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("# x")
        .assertIsDisplayed() // For empty string, it should still show "# x"
    composeTestRule.onNodeWithText("#1234 x").assertIsDisplayed()
    composeTestRule.onNodeWithText("#Special@Tag x").assertIsDisplayed()
  }

  @Test
  fun testExtendedTag() {
    composeTestRule.setContent { ExtendTag(TagStyle(), onClickMock) }
    composeTestRule.onNodeWithTag("testExtendTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertTextEquals("...")
    composeTestRule.onNodeWithTag("testExtendTag").performClick()
    verify(onClickMock).invoke()
  }

  @Test
  fun testExtendTag_withDifferentStyles() {
    val tagStyle1 = TagStyle(Color.Yellow, Color.Black, 12.sp)
    val tagStyle2 = TagStyle(Color.Green, Color.Blue, 18.sp)
    val tagStyle3 = TagStyle(Color.Cyan, Color.Magenta, 14.sp)

    composeTestRule.setContent {
      Column {
        ExtendTag(tagStyle1, {})
        ExtendTag(tagStyle2, {})
        ExtendTag(tagStyle3, {})
      }
    }

    // Assert that all ExtendTag nodes are displayed correctly
    composeTestRule.onAllNodesWithTag("testExtendTag").assertCountEquals(3)
  }

  @Test
  fun testExtendTag_withDifferentTextStyles() {
    val tagStyle = TagStyle(Color.Gray, Color.LightGray, 16.sp)

    composeTestRule.setContent { ExtendTag(tagStyle) {} }

    // Verify that the text is displayed with "..."
    composeTestRule.onNodeWithText("...").assertIsDisplayed()

    // Verify that the text color and other properties are applied
    composeTestRule.onNodeWithText("...").assertExists().assertHasClickAction()
  }

  @Test
  fun testRowOfTags1() {
    composeTestRule.setContent { RowOfTags(selectedTag, TagStyle()) }
    composeTestRule.onAllNodesWithTag("testTag").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertTextEquals("...")
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(3)
    composeTestRule.onNodeWithTag("testExtendTag").performClick()
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(5)
  }

  @Test
  fun testRowOfTagsEmpty() {
    val emptyList = mutableStateOf(listOf<Pair<String, Color>>())
    composeTestRule.setContent { RowOfTags(emptyList.value, TagStyle()) }
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(0)
  }

  @Test
  fun testRowOfTagsInitiallyCollapsed() {
    val tagList =
        listOf(
            Pair("salsa", Color.Red),
            Pair("pizza", Color.Red),
            Pair("coca-cola", Color.Red),
            Pair("pepsi", Color.Red),
            Pair("fanta", Color.Red))

    composeTestRule.setContent {
      RowOfTags(l = tagList, tagStyle = TagStyle(Color.Black, Color.White, 16.sp))
    }

    // Assert that only the first 3 tags are displayed
    composeTestRule.onAllNodesWithText("#salsa").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#pizza").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#coca-cola").assertCountEquals(1)

    // Assert that "Extend" button is displayed
    composeTestRule.onNodeWithText("...").assertIsDisplayed()

    // Assert that other tags are not displayed initially
    composeTestRule.onAllNodesWithText("#pepsi").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("#fanta").assertCountEquals(0)
  }

  @Test
  fun testRowOfTagsExpanded() {
    val tagList =
        listOf(
            Pair("salsa", Color.Red),
            Pair("pizza", Color.Red),
            Pair("coca-cola", Color.Red),
            Pair("pepsi", Color.Red),
            Pair("fanta", Color.Red))

    composeTestRule.setContent {
      RowOfTags(l = tagList, tagStyle = TagStyle(Color.Black, Color.White, 16.sp))
    }

    // Simulate clicking the "Extend" button
    composeTestRule.onNodeWithText("...").performClick()

    // Assert that all tags are displayed after expanding
    composeTestRule.onAllNodesWithText("#salsa").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#pizza").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#coca-cola").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#pepsi").assertCountEquals(1)
    composeTestRule.onAllNodesWithText("#fanta").assertCountEquals(1)
  }

  @Test
  fun testRowOfTagsWithThreeOrLessElements() {
    val tagList = mutableStateOf(listOf<Pair<String, Color>>())
    composeTestRule.setContent {
      RowOfTags(l = tagList.value, tagStyle = TagStyle(Color.Black, Color.White, 16.sp))
    }
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(0)
    tagList.value += Pair("salsa", Color.Red)
    composeTestRule.onNodeWithTag("testExtendTag").assertDoesNotExist()

    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(1)
    composeTestRule.onNodeWithTag("testTag").assertTextEquals("#salsa")
    composeTestRule.onAllNodesWithTag("testExtendTag").assertCountEquals(0)

    tagList.value += Pair("pizza", Color.Red)
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(2)
    composeTestRule.onNodeWithTag("testExtendTag").assertDoesNotExist()

    tagList.value += Pair("pepe", Color.Red)
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(3)
    composeTestRule.onNodeWithTag("testExtendTag").assertIsDisplayed()
  }

  @Test
  fun testRowOfClickableTagsInitiallyCollapsed() {
    val tagList =
        listOf(
            Pair("salsa", Color.Red),
            Pair("pizza", Color.Red),
            Pair("coca-cola", Color.Red),
            Pair("pepsi", Color.Red),
            Pair("fanta", Color.Red))
    composeTestRule.setContent {
      RowOfClickTags(tagList, TagStyle()) { s -> tagSelectorOnClickMock(s) }
    }
    composeTestRule.onAllNodesWithTag("clickTestTag").onFirst().assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertIsDisplayed()
    composeTestRule.onNodeWithTag("testExtendTag").assertTextEquals("...")
    composeTestRule.onAllNodesWithTag("clickTestTag").assertCountEquals(3)
    composeTestRule.onNodeWithTag("testExtendTag").performClick()
    composeTestRule.onAllNodesWithTag("clickTestTag").assertCountEquals(5)
    composeTestRule.onAllNodesWithText("#salsa x").onFirst().performClick()
    verify(tagSelectorOnClickMock).invoke("salsa")
  }

  @Test
  fun testRowOfClickTagsEmpty() {
    val emptyList = mutableStateOf(listOf<Pair<String, Color>>())
    composeTestRule.setContent {
      RowOfClickTags(emptyList.value, TagStyle(), tagSelectorOnClickMock)
    }
    composeTestRule.onAllNodesWithTag("testTag").assertCountEquals(0)
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
          300.dp,
          300.dp)
    }
    composeTestRule.onAllNodesWithText("Tags").onFirst().assertIsDisplayed()

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

    composeTestRule
        .onNodeWithTag("sizeTagSelector")
        .assertWidthIsEqualTo(300.dp)
        .assertHeightIsEqualTo(300.dp)
  }

  @Test
  fun testTagSelectorEmptyList() {
    val textColor = Color.White
    val textSize = 16.sp
    val userInput = "input"

    val emptyList = listOf<Pair<String, Color>>()

    composeTestRule.setContent {
      TagSelector(
          emptyList,
          emptyList,
          stringQuery,
          expanded,
          tagSelectorOnClickMock,
          tagSelectorOnClickDropDownMenu,
          tagSelectorOnStringChanged,
          textColor,
          textSize,
          300.dp,
          300.dp)
    }
    composeTestRule.onAllNodesWithText("Tags").onFirst().assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("clickTestTag").assertCountEquals(0)
    composeTestRule.onNodeWithTag("inputTagSelector").performTextClearance()
    composeTestRule.onNodeWithTag("inputTagSelector").performTextInput(userInput)
    verify(tagSelectorOnStringChanged).invoke(userInput)
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithTag("tagSelectorDropDownMenuItem").assertCountEquals(0)
  }

  @Test
  fun testTagSelectorTooSmall() {
    val textColor = Color.White
    val textSize = 16.sp

    val emptyList = listOf<Pair<String, Color>>()

    composeTestRule.setContent {
      TagSelector(
          emptyList,
          emptyList,
          stringQuery,
          expanded,
          tagSelectorOnClickMock,
          tagSelectorOnClickDropDownMenu,
          tagSelectorOnStringChanged,
          textColor,
          textSize,
          0.dp,
          0.dp)
    }
    composeTestRule.onAllNodesWithText("Tags").onFirst().assertIsNotDisplayed()
    composeTestRule.onAllNodesWithTag("clickTestTag").assertCountEquals(0)
  }
}
