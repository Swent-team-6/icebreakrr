package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.MainActivity
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.Tag
import com.github.se.icebreakrr.ui.tags.TagStyle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NotificationTest {
    private lateinit var navigationActionsMock: NavigationActions
    private lateinit var onClickMock: () -> Unit
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        navigationActionsMock = mock()
        onClickMock = mock()
        //whenever(navigationActionsMock.navigateTo())
    }

    @Test
    fun aroundYouScreenIsDisplayedOnLaunch() {
        composeTestRule.setContent { NotificationScreen(navigationActionsMock) }
        composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationMenuIsDisplayed() {
        composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    }

    @Test
    fun navigationWorks() {
        composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()
        composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()
        composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
    }
}