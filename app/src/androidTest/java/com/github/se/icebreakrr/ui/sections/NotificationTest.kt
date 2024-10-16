package com.github.se.icebreakrr.ui.sections

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.icebreakrr.MainActivity
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
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
    private lateinit var navFilterMock:() -> Unit
    private lateinit var navSettingsMock:() -> Unit
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        navigationActionsMock = mock()
        onClickMock = mock()
        navFilterMock = mock()
        navSettingsMock = mock()
        whenever(navigationActionsMock.navigateTo(Screen.FILTER)).then { navFilterMock }
        whenever(navigationActionsMock.navigateTo(Screen.SETTINGS)).then { navSettingsMock }

    }

    @Test
    fun aroundYouScreenIsDisplayedOnLaunch() {
        composeTestRule.setContent { NotificationScreen(navigationActionsMock) }
        composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
    }

    @Test
    fun allIsDisplayed() {
        composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    }

    @Test
    fun navigationWorks() {

    }
}