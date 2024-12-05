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
}
