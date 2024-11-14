package com.github.se.icebreakrr.utils

import java.util.Calendar
import org.junit.Assert.*
import org.junit.Test

class DateUtilsTest {

  @Test
  fun isAgeValid_returnsTrue_forAgeOver13() {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -14)
    val birthDate = calendar.time

    val result = DateUtils.isAgeValid(birthDate)

    assertTrue(result)
  }

  @Test
  fun isAgeValid_returnsFalse_forAgeUnder13() {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -12)
    val birthDate = calendar.time

    val result = DateUtils.isAgeValid(birthDate)

    assertFalse(result)
  }

  @Test
  fun isAgeValid_returnsTrue_forExactly13() {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -13)
    val birthDate = calendar.time

    val result = DateUtils.isAgeValid(birthDate)

    assertTrue(result)
  }

  @Test
  fun formatDate_returnsCorrectFormat() {
    val calendar = Calendar.getInstance()
    calendar.set(2000, Calendar.DECEMBER, 31)
    val date = calendar.time

    val result = DateUtils.formatDate(date)

    assertEquals("31/12/2000", result)
  }

  @Test
  fun formatDate_handlesSingleDigitDayAndMonth() {
    val calendar = Calendar.getInstance()
    calendar.set(2000, Calendar.JANUARY, 1)
    val date = calendar.time

    val result = DateUtils.formatDate(date)

    assertEquals("01/01/2000", result)
  }

  @Test
  fun formatDate_handlesLeapYear() {
    val calendar = Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 29) }
    val date = calendar.time

    val result = DateUtils.formatDate(date)

    assertEquals("29/02/2024", result)
  }

  @Test
  fun isAgeValid_handlesFutureDates() {
    val calendar =
        Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, 1) // Set to tomorrow
        }
    val futureDate = calendar.time

    val result = DateUtils.isAgeValid(futureDate)

    assertFalse(result)
  }
}
