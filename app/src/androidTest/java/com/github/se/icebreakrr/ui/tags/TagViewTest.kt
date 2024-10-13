package com.github.se.icebreakrr.ui.tags

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.model.tags.TagsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TagViewTest {
    private lateinit var profileTag : MutableState<List<Pair<String, Color>>>
    private lateinit var tagsViewModel : TagsViewModel
    private lateinit var stringQuery : MutableState<String>
    private lateinit var expanded : MutableState<Boolean>
    private lateinit var onClickMock : () -> Unit
    private lateinit var hasBeenClicked: MutableState<Boolean>

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {

        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this)

        profileTag = mutableStateOf(listOf(Pair("a", Color.Red), Pair("b", Color.Yellow)))
        tagsViewModel = mock()
        stringQuery = mutableStateOf("")
        expanded = mutableStateOf(false)
        onClickMock = mock<() -> Unit>()
        hasBeenClicked = mutableStateOf(false)

        //`when`(tagsViewModel.outputTags).thenReturn(mutableStateOf(listOf(Pair("Tennis", Color.Red))))
    }

    @Test
    fun displayTag() {
        composeTestRule.setContent { Tag("AndroidTest", Color.Red) }
        val node = composeTestRule.onNodeWithTag("testTag")
        node.assertIsDisplayed()
        node.assertTextEquals("#AndroidTest")
    }

    @Test
    fun displayClickTag() {
        composeTestRule.setContent { ClickTag("AndroidTest", Color.Red, {}) }
        composeTestRule.onNodeWithTag("clickTestTag").assertIsDisplayed()
        composeTestRule.onNodeWithTag("clickTestTag").assertTextEquals("#AndroidTest")
    }

    @Test
    fun testClickTag_onClickCalled() {
        composeTestRule.setContent {
            ClickTag("AndroidTest", Color.Red, onClickMock)
        }

        // Simulate a click on the node with the test tag
        composeTestRule.onNodeWithTag("clickTestTag").performClick()

        // Verify that the onClick function was called
        verify(onClickMock).invoke()
    }
}
