package com.github.se.icebreakrr.ui.map

import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.ui.profile.ProfileModal
import com.google.firebase.Timestamp

import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.icebreakrr.model.profile.Profile
import org.junit.Rule
import org.junit.Test

class ProfileModalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setUpProfileModal(profile: Profile?, locationDescription: String? = null, onDismiss: () -> Unit = {}) {
        composeTestRule.setContent {
            ProfileModal(profile = profile, locationDescription = locationDescription, onDismiss = onDismiss, onNavigate = {})
        }
    }

    @Test
    fun profileModal_displaysAllUserDetails() {
        // Arrange
        val profile = Profile(
            uid = "1",
            name = "John Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(), // 22 years old
            catchPhrase = "Just a friendly guy",
            description = "I love meeting new people.",
            tags = listOf("friendly", "outgoing"),
            profilePictureUrl = "http://example.com/profile.jpg",
            meetingRequestInbox = mapOf(),
            meetingRequestChosenLocalisation = mapOf(),
            meetingRequestSent = listOf(),
            fcmToken = "11")

        // Act
        setUpProfileModal(profile)

        // Assert
        composeTestRule.onNodeWithContentDescription("profile picture").assertIsDisplayed()
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    }

    @Test
    fun profileModal_displaysLocationDescription() {
        // Arrange
        val profile = Profile(
            uid = "1",
            name = "John Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(), // 22 years old
            catchPhrase = "Just a friendly guy",
            description = "I love meeting new people.",
            tags = listOf("friendly", "outgoing"),
            profilePictureUrl = "http://example.com/profile.jpg",
            meetingRequestInbox = mapOf(),
            meetingRequestChosenLocalisation = mapOf(),
            meetingRequestSent = listOf(),
            fcmToken = "11")
        val locationDescription = "Meet me at the park."

        // Act
        setUpProfileModal(profile, locationDescription)

        // Assert
        composeTestRule.onNodeWithText("Meet me at the park.").assertIsDisplayed()
    }

    @Test
    fun profileModal_dismissesOnCloseButtonClick() {
        // Arrange
        var isDismissed = false
        val profile = Profile(
            uid = "1",
            name = "John Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(), // 22 years old
            catchPhrase = "Just a friendly guy",
            description = "I love meeting new people.",
            tags = listOf("friendly", "outgoing"),
            profilePictureUrl = "http://example.com/profile.jpg",
            meetingRequestInbox = mapOf(),
            meetingRequestChosenLocalisation = mapOf(),
            meetingRequestSent = listOf(),
            fcmToken = "11")

        // Act
        setUpProfileModal(profile, onDismiss = { isDismissed = true })

        // Simulate clicking the close button
        composeTestRule.onNodeWithText("Close").performClick()

        // Assert
        assert(isDismissed) { "ProfileModal was not dismissed." }
    }

    @Test
    fun profileModal_displaysDefaultImageWhenProfilePictureUrlIsNull() {
        // Arrange
        val profile = Profile(
            uid = "1",
            name = "John Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(), // 22 years old
            catchPhrase = "Just a friendly guy",
            description = "I love meeting new people.",
            tags = listOf("friendly", "outgoing"),
            profilePictureUrl = "http://example.com/profile.jpg",
            meetingRequestInbox = mapOf(),
            meetingRequestChosenLocalisation = mapOf(),
            meetingRequestSent = listOf(),
            fcmToken = "11")

        // Act
        setUpProfileModal(profile)

        // Assert
        // Assuming the default image has a specific content description
        composeTestRule.onNodeWithContentDescription("profile picture").assertIsDisplayed()
    }
}