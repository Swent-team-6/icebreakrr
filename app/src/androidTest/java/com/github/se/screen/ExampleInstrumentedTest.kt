package com.github.se.screen

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.endToEnd.TestPermissionDelegate
import com.github.se.icebreakrr.Icebreakrr
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * Test nothing, just run the app to generate the coverage report
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTest : TestCase() {
  val intent =
      Intent(InstrumentationRegistry.getInstrumentation().targetContext, Icebreakrr::class.java)
  @Inject lateinit var authInjected: FirebaseAuth
  @Inject lateinit var firestoreInjected: FirebaseFirestore
  @Inject lateinit var authStateListener: FirebaseAuth.AuthStateListener

  @get:Rule var hiltRule = HiltAndroidRule(this)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    hiltRule.inject()
    intent.putExtra("IS_TESTING", true)
    ActivityCompat.setPermissionCompatDelegate(TestPermissionDelegate())
  }

  @Test fun test() = run { ActivityScenario.launch<Icebreakrr>(intent).use {} }
}
