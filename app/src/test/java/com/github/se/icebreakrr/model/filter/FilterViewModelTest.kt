package com.github.se.icebreakrr.model.filter

import com.github.se.icebreakrr.model.profile.Gender
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// This file was written with the help of CursorAI
class FilterViewModelTest {
  private lateinit var viewModel: FilterViewModel

  @Before
  fun setUp() {
    viewModel = FilterViewModel()
  }

  @Test
  fun testInitialState() {
    // Check that all initial states are empty/null
    assertEquals(emptyList<Gender>(), viewModel.selectedGenders.value)
    assertEquals(null, viewModel.ageRange.value)
    assertEquals(emptyList<String>(), viewModel.filteredTags.value)
  }

  @Test
  fun testSetGenders() {
    // Test setting single gender
    val singleGender = listOf(Gender.MEN)
    viewModel.setGenders(singleGender)
    assertEquals(singleGender, viewModel.selectedGenders.value)

    // Test setting multiple genders
    val multipleGenders = listOf(Gender.MEN, Gender.WOMEN)
    viewModel.setGenders(multipleGenders)
    assertEquals(multipleGenders, viewModel.selectedGenders.value)

    // Test setting empty list
    viewModel.setGenders(emptyList())
    assertEquals(emptyList<Gender>(), viewModel.selectedGenders.value)

    // Test setting all genders
    val allGenders = listOf(Gender.MEN, Gender.WOMEN, Gender.OTHER)
    viewModel.setGenders(allGenders)
    assertEquals(allGenders, viewModel.selectedGenders.value)
  }

  @Test
  fun testSetAgeRange() {
    // Test setting valid age range
    val validRange = 18..30
    viewModel.setAgeRange(validRange)
    assertEquals(validRange, viewModel.ageRange.value)

    // Test setting single age range
    val singleAge = 25..25
    viewModel.setAgeRange(singleAge)
    assertEquals(singleAge, viewModel.ageRange.value)

    // Test setting null age range
    viewModel.setAgeRange(null)
    assertEquals(null, viewModel.ageRange.value)

    // Test setting wide age range
    val wideRange = 18..99
    viewModel.setAgeRange(wideRange)
    assertEquals(wideRange, viewModel.ageRange.value)
  }

  @Test
  fun testSetFilteredTags() {
    // Test setting single tag
    val singleTag = listOf("Sports")
    viewModel.setFilteredTags(singleTag)
    assertEquals(singleTag, viewModel.filteredTags.value)

    // Test setting multiple tags
    val multipleTags = listOf("Sports", "Music", "Art")
    viewModel.setFilteredTags(multipleTags)
    assertEquals(multipleTags, viewModel.filteredTags.value)

    // Test setting empty tags list
    viewModel.setFilteredTags(emptyList())
    assertEquals(emptyList<String>(), viewModel.filteredTags.value)
  }
}
