package com.github.se.icebreakrr

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FirebaseAuthTest {

  private lateinit var mockFirebaseAuth: FirebaseAuth
  private lateinit var mockFirebaseUser: FirebaseUser

  @Before
  fun setup() {
    // Mock FirebaseAuth and FirebaseUser using Mockito
    mockFirebaseAuth = mock()
    mockFirebaseUser = mock()

    // Simulate a logged-in user
    whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    whenever(mockFirebaseUser.email).thenReturn("testuser@example.com")
  }

  @Test
  fun testFirebaseAuth() {
    val currentUser = mockFirebaseAuth.currentUser
    assertEquals("testuser@example.com", currentUser?.email)
  }
}
