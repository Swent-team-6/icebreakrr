package com.github.se.icebreakrr.ui.sections.shared

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class TagDisplayTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displayTags_whenLessThanThreeTags() {
        val tags = listOf("Tag1", "Tag2")

        composeTestRule.setContent {
            TagDisplay(tags = tags, isSettings = false)
        }

        // Check if both tags are displayed
        tags.forEach { tag ->
            composeTestRule.onNodeWithText("#$tag").assertIsDisplayed()
        }
    }

    @Test
    fun displayTwoTags_whenMoreThanThreeTags() {
        val tags = listOf("Tag1", "Tag2", "Tag3", "Tag4")

        composeTestRule.setContent {
            TagDisplay(tags = tags, isSettings = false)
        }

        // Check if only the first two tags are displayed
        composeTestRule.onNodeWithText("#Tag1").assertIsDisplayed()
        composeTestRule.onNodeWithText("#Tag2").assertIsDisplayed()

        // Check if the ellipsis is displayed
        composeTestRule.onNodeWithText("...").assertIsDisplayed()
    }

    @Test
    fun displayAllTags_whenThreeOrLessTags() {
        val tags = listOf("Tag1", "Tag2", "Tag3")

        composeTestRule.setContent {
            TagDisplay(tags = tags, isSettings = false)
        }

        // Check if all tags are displayed
        tags.forEach { tag ->
            composeTestRule.onNodeWithText("#$tag").assertIsDisplayed()
        }

        // Ensure ellipsis is not displayed
        composeTestRule.onNodeWithText("...").assertDoesNotExist()
    }

    @Test
    fun displayTapToPreview_whenInSettings() {
        composeTestRule.setContent {
            TagDisplay(tags = emptyList(), isSettings = true)
        }

        // Check if the "Tap to preview profile" text is displayed
        composeTestRule.onNodeWithText("Tap to preview profile").assertIsDisplayed()
    }
}