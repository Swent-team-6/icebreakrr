package com.github.se.icebreakrr.model.profile

import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileTest {

  @Test
  fun calculateAge_shouldReturnCorrectAge() {
    // Setup: create a birth date 25 years ago from today
    val calendar = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
    val birthDate = Timestamp(calendar.time)

    // Create a profile with the birthDate 25 years ago
    val profile =
        Profile(
            uid = "1",
            name = "John Doe",
            gender = Gender.MEN,
            birthDate = birthDate,
            catchPhrase = "Hello World",
            description = "Just a sample profile")

    // Act: calculate the age
    val age = profile.calculateAge()

    // Assert: the age should be 25
    assertEquals(25, age)
  }

  @Test
  fun calculateAge_shouldAccountForBirthdaysThatHaventHappenedYetThisYear() {
    // Setup: create a birth date where the birthday is later in the year
    val calendar =
        Calendar.getInstance().apply {
          set(Calendar.MONTH, get(Calendar.MONTH) + 1) // Set birth month to next month
          add(Calendar.YEAR, -25) // 25 years ago
        }
    val birthDate = Timestamp(calendar.time)

    // Create a profile with the birthDate 25 years ago, but birthday hasn't occurred this year
    val profile =
        Profile(
            uid = "2",
            name = "Jane Doe",
            gender = Gender.WOMEN,
            birthDate = birthDate,
            catchPhrase = "Catchphrase",
            description = "Another profile")

    // Act: calculate the age
    val age = profile.calculateAge()

    // Assert: the age should be 24 because the birthday hasn't occurred yet this year
    assertEquals(24, age)
  }

  @Test
  fun calculateAge_shouldReturnZeroForNewborn() {
    // Setup: create a birth date for today
    val calendar = Calendar.getInstance()
    val birthDate = Timestamp(calendar.time)

    // Create a profile with a birthDate set to today
    val profile =
        Profile(
            uid = "3",
            name = "Baby Doe",
            gender = Gender.OTHER,
            birthDate = birthDate,
            catchPhrase = "Newborn",
            description = "Newborn profile")

    // Act: calculate the age
    val age = profile.calculateAge()

    // Assert: the age should be 0
    assertEquals(0, age)
  }

  @Test
  fun genderEnum_shouldReturnCorrectDisplayName() {
    // Assert Gender display names
    assertEquals("Women", Gender.WOMEN.displayName)
    assertEquals("Men", Gender.MEN.displayName)
    assertEquals("Other", Gender.OTHER.displayName)
  }

  @Test
  fun profile_shouldHandleNullProfilePictureUrl() {
    // Create a profile without a profile picture URL
    val profile =
        Profile(
            uid = "4",
            name = "NoPic Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(),
            catchPhrase = "No Picture",
            description = "Profile without picture",
            profilePictureUrl = null)

    // Assert: profilePictureUrl should be null
    assertEquals(null, profile.profilePictureUrl)
  }

  @Test
  fun profile_shouldHaveDefaultEmptyTagsList() {
    // Create a profile without providing tags
    val profile =
        Profile(
            uid = "5",
            name = "EmptyTags Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(),
            catchPhrase = "No Tags",
            description = "Profile with empty tags")

    // Assert: tags should be an empty list
    assertEquals(emptyList<String>(), profile.tags)
  }

  @Test
  fun profile_shouldAllowCustomTags() {
    // Create a profile with custom tags
    val profile =
        Profile(
            uid = "6",
            name = "Tagged Doe",
            gender = Gender.MEN,
            birthDate = Timestamp.now(),
            catchPhrase = "With Tags",
            description = "Profile with custom tags",
            tags = listOf("Tag1", "Tag2", "Tag3"))

    // Assert: the tags list should contain the custom tags
    assertEquals(listOf("Tag1", "Tag2", "Tag3"), profile.tags)
  }
}
