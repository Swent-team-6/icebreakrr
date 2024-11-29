package com.github.se.icebreakrr.utils

import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq

class FakeUserGeneratorTest {

  private lateinit var profilesViewModel: ProfilesViewModel

  @Before
  fun setUp() {
    profilesViewModel = mock(ProfilesViewModel::class.java)
  }

  @Test
  fun generateFakeUsers_callsAddNewProfile() {
    // Given
    val centerLat = 0.0
    val centerLon = 0.0
    val radius = 1000.0

    // When
    generateFakeUsers(centerLat, centerLon, radius, profilesViewModel)

    // Then
    for (userId in predefinedUserIds) {
      verify(profilesViewModel)
          .addNewProfile(argThat { uid == userId && name == "Fake User $userId" })
    }
  }

  @Test
  fun deleteFakeUsers_callsDeleteProfileByUid() {
    // When
    deleteFakeUsers(profilesViewModel)

    // Then
    for (userId in predefinedUserIds) {
      verify(profilesViewModel).deleteProfileByUid(eq(userId))
    }
  }
}
