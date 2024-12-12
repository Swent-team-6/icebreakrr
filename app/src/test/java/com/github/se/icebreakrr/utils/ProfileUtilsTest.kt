package com.github.se.icebreakrr.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileUtilsTest {

  @Test
  fun testCropUsername_shorterThanMaxLength() {
    val username = "JohnDoe"
    val maxLength = 10
    val result = cropUsername(username, maxLength)
    assertEquals("JohnDoe", result) // Expect the same username
  }

  @Test
  fun testCropUsername_exactlyMaxLength() {
    val username = "JohnDoe"
    val maxLength = 8
    val result = cropUsername(username, maxLength)
    assertEquals("JohnDoe", result) // Expect the same username
  }

  @Test
  fun testCropUsername_longerThanMaxLength() {
    val username = "JohnathanDoe"
    val maxLength = 10
    val result = cropUsername(username, maxLength)
    assertEquals("JohnathanD...", result) // Expect cropped username with ellipsis
  }

  @Test
  fun testCropUsername_emptyUsername() {
    val username = ""
    val maxLength = 5
    val result = cropUsername(username, maxLength)
    assertEquals("", result) // Expect empty string
  }

  @Test
  fun testCropUsername_maxLengthZero() {
    val username = "JohnDoe"
    val maxLength = 0
    val result = cropUsername(username, maxLength)
    assertEquals("...", result) // Expect only ellipsis
  }

  @Test
  fun testCropUsername_withNegativeMaxLength() {
    val username = "JohnDoe"
    val maxLength = -5
    val result = cropUsername(username, maxLength)
    assertEquals("JohnDoe", result) // Expect the same username
  }

  @Test
  fun testCropUsername_withSpecialCharacters() {
    val username = "John@Doe!"
    val maxLength = 8
    val result = cropUsername(username, maxLength)
    assertEquals("John@Doe...", result) // Expect cropped username with ellipsis
  }

  @Test
  fun testCropUsername_withWhitespace() {
    val username = "John Doe"
    val maxLength = 7
    val result = cropUsername(username, maxLength)
    assertEquals("John Do...", result) // Expect cropped username with ellipsis
  }

  @Test
  fun testCropUsername_withVeryLargeUsername() {
    val username = "A".repeat(1000) // Very large username
    val maxLength = 10
    val result = cropUsername(username, maxLength)
    assertEquals("A".repeat(10) + "...", result) // Expect cropped username with ellipsis
  }

  @Test
  fun testCropUsername_withVeryLargeMaxLength() {
    val username = "JohnDoe"
    val maxLength = 1000 // Very large maxLength
    val result = cropUsername(username, maxLength)
    assertEquals("JohnDoe", result) // Expect the same username
  }
}
