package com.github.se.endToEnd

import android.content.Intent
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.icebreakrr.MainActivity
import com.github.se.icebreakrr.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class M1_Test {
  val intent =
      Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)

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

  @Test
  fun end_to_end_test_1() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      // Screen 1 : Around You Screen
      composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
      composeTestRule.waitForIdle()
      composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
      composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("filterButton").performClick()

      // Screen 2 : Filter Screen and Click on Men
      composeTestRule.onNodeWithTag("FilterTopBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("GenderButtonMen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("GenderButtonOther").assertIsDisplayed()
      composeTestRule.onNodeWithTag("AgeFromTextField").assertIsDisplayed()
      composeTestRule.onNodeWithTag("AgeToTextField").assertIsDisplayed()
      composeTestRule.onNodeWithTag("FilterButton").assertIsDisplayed()

      composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
      composeTestRule.onNodeWithTag("GenderButtonMen").assertIsSelected()
      composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsNotSelected()
      composeTestRule.onNodeWithTag("GenderButtonOther").assertIsNotSelected()

      composeTestRule.onNodeWithTag("FilterTopBar").onChildAt(0).performClick()
      composeTestRule.onNodeWithText("Discard changes").performClick()
      // Screen 3 : Go back to Around You
      composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("navItem_${R.string.notifications}").performClick()

      // Screen 4 : Notification Screen
      composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
      composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
      composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
      composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
      composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
      composeTestRule.onNodeWithTag("notificationSecondText").performScrollTo()
      composeTestRule.onNodeWithTag("navItem_${R.string.around_you}").performClick()

      // Screen 5 : Go back to Around You and click on card
      composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
      composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
      composeTestRule.onAllNodesWithTag("profileCard").onFirst().performClick()

      // Screen 6 : Profile Screen
      composeTestRule.onNodeWithTag("aroundYouProfileScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
      composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("requestButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("username").assertTextEquals("Alice Inwonderland")
      composeTestRule.onNodeWithTag("goBackButton").performClick()

      // Go to the settings from Around You
      composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("navItem_${R.string.settings}").performClick()

      // Screen 7 : Setting Screen
      composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()
      composeTestRule.onNodeWithTag("Toggle Discoverability").assertIsDisplayed()
      composeTestRule.onNodeWithTag("logOutButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("logOutButton").performClick()

      scenario.close()
    }
  }
}
