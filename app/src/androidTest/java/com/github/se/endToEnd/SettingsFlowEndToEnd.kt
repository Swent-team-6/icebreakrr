package com.github.se.endToEnd

import android.content.Intent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsProperties.ToggleableState
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.icebreakrr.MainActivity
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
class SettingsFlowEndToEnd {
  private val intent =
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
  fun SettingsFlowEndToEndTest() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      // Screen 1 : Around You Screen
      composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
      composeTestRule.waitForIdle()
      composeTestRule.onAllNodesWithTag("profileCard").onFirst().assertIsDisplayed()
      composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()

      // go to settings :
      composeTestRule.onNodeWithText("Settings").performClick()

      // check if everything is displayed :
      composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed()
      composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("logOutButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("Toggle Discoverability").assertIsDisplayed()

      // click on your profile :
      composeTestRule.onNodeWithTag("profileCard").performClick()

      // test if everything is displayed
      composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
      composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("username").assertIsDisplayed()
      composeTestRule.onNodeWithTag("editButton").assertIsDisplayed().assertHasClickAction()

      // edit your profile :
      composeTestRule.onNodeWithTag("editButton").performClick()
      // check if edit profile is displayed :
      composeTestRule.onNodeWithTag("profileEditScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("checkButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("profileEditScreenContent").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
      composeTestRule.onNodeWithTag("nameAndAge").assertIsDisplayed()
      composeTestRule.onNodeWithTag("catchphrase").assertIsDisplayed()
      composeTestRule.onNodeWithTag("description").assertIsDisplayed()
      // populate profile :
      composeTestRule.onNodeWithTag("catchphrase").performTextInput("This is my new catchphrase!")
      composeTestRule.onNodeWithText("This is my new catchphrase!").assertIsDisplayed()
      composeTestRule.onNodeWithTag("description").performTextInput("This is my new description!")
      composeTestRule.onNodeWithText("This is my new description!").assertHasClickAction()
      // test tag selector :
      composeTestRule.onNodeWithTag("inputTagSelector").performTextInput("Travel")
      composeTestRule.onNodeWithText("#Travel").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithTag("clickTestTag")
          .assertIsDisplayed()
          .performClick()
          .assertIsNotDisplayed()
      composeTestRule.onNodeWithTag("inputTagSelector").performTextClearance()
      composeTestRule.onNodeWithTag("inputTagSelector").performTextInput("Travel")
      composeTestRule.onNodeWithText("#Travel").assertIsDisplayed().performClick()
      // click on go back :
      composeTestRule.onNodeWithTag("goBackButton").performClick()
      // click on cancel :
      composeTestRule.onNodeWithText("Cancel").performClick()
      // click on go back :
      composeTestRule.onNodeWithTag("goBackButton").performClick()
      // click on discard changes :
      composeTestRule.onNodeWithText("Discard changes").performClick()
      // test that profile hasent changed :
      composeTestRule.onNodeWithText("This is my new catchphrase!").assertIsNotDisplayed()
      composeTestRule.onNodeWithText("This is my new description!").assertIsNotDisplayed()
      composeTestRule.onNodeWithText("#Travel").assertIsNotDisplayed()
      // click on edit profile :
      composeTestRule.onNodeWithTag("editButton").performClick()
      // repopulate profile :
      composeTestRule.onNodeWithTag("catchphrase").performTextInput("This is my new catchphrase!")
      composeTestRule.onNodeWithText("This is my new catchphrase!").assertIsDisplayed()
      composeTestRule.onNodeWithTag("description").performTextInput("This is my new description!")
      composeTestRule.onNodeWithText("This is my new description!").assertHasClickAction()
      composeTestRule.onNodeWithTag("inputTagSelector").performTextInput("Travel")
      composeTestRule.onNodeWithText("#Travel").assertIsDisplayed().performClick()
      // save changes :
      composeTestRule.onNodeWithTag("checkButton").performClick()
      // check if profile has been updated :
      composeTestRule.onNodeWithText("This is my new catchphrase!").assertIsDisplayed()
      composeTestRule.onNodeWithText("This is my new description!").assertIsDisplayed()
      composeTestRule.onNodeWithText("#Travel").assertIsDisplayed()
      // click on go back :
      composeTestRule.onNodeWithTag("goBackButton").performClick()

      // deactivate toogle discoverability :
      composeTestRule
          .onNodeWithTag("switchToggle Discoverability")
          .assertIsDisplayed()
          .assertIsOn()
          .performClick()
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        val node =
            composeTestRule.onNodeWithTag("switchToggle Discoverability").fetchSemanticsNode()
        node.config.getOrNull(SemanticsProperties.ToggleableState) ==
            androidx.compose.ui.state.ToggleableState.Off
      }
      composeTestRule
          .onNodeWithTag("switchToggle Discoverability")
          .assertIsDisplayed()
          .assertIsOff()
      // go to around you and check that the right text is displayed :
      composeTestRule.onNodeWithText("Around You").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithText("Activate location sharing in the app settings!")
          .assertIsDisplayed()
      // come back to settings and toggle option
      composeTestRule.onNodeWithText("Settings").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithTag("switchToggle Discoverability")
          .assertIsDisplayed()
          .assertIsOff()
          .performClick()
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        val node =
            composeTestRule.onNodeWithTag("switchToggle Discoverability").fetchSemanticsNode()
        node.config.getOrNull(SemanticsProperties.ToggleableState) ==
            androidx.compose.ui.state.ToggleableState.On
      }
      composeTestRule.onNodeWithTag("switchToggle Discoverability").assertIsDisplayed().assertIsOn()

      // blocked users should be empty :
      composeTestRule.onNodeWithTag("blockedUsersButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("You have not blocked any users!").assertIsDisplayed()
      composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
      // go in around you to block alice :
      composeTestRule.onNodeWithText("Around You").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("flagButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("alertDialogReportBlock").assertIsDisplayed()
      // cancel and then block :
      composeTestRule.onNodeWithText("Cancel").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("flagButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Block").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Cancel").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("flagButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Block").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Block").assertIsDisplayed().performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsNotDisplayed()
      // go back in blocked users to see if it has been updated :
      composeTestRule.onNodeWithText("Settings").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("blockedUsersButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed()
      // unblock and check it is updated :
      composeTestRule.onNodeWithText("Alice Inwonderland").performClick()
      composeTestRule.onNodeWithText("No").assertIsDisplayed().assertHasClickAction().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").performClick()
      composeTestRule
          .onNodeWithText("Yes")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithText("You have not blocked any users!").assertIsDisplayed()
      composeTestRule
          .onNodeWithTag("goBackButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithText("Around You").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed()
      composeTestRule.onNodeWithText("Settings").assertIsDisplayed().performClick()

      // already met alice and check everything updated :
      composeTestRule.onNodeWithTag("alreadyMetButton").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithText("You haven't met anyone yet. Go explore the world!")
          .assertIsDisplayed()
      composeTestRule
          .onNodeWithTag("goBackButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithText("Around You").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithTag("alreadyMetButton")
          .performScrollTo()
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsNotDisplayed()
      composeTestRule.onNodeWithText("Settings").assertIsDisplayed().performClick()
      // remove alice from already met to see if updated :
      composeTestRule.onNodeWithTag("alreadyMetButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("No").assertIsDisplayed().assertHasClickAction().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithText("Yes")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule
          .onNodeWithText("You haven't met anyone yet. Go explore the world!")
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Around You").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed()

      // report alice
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("flagButton").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithText("Report")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule
          .onNodeWithText("Spam")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithText("Report").assertExists().assertHasClickAction().performClick()
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsNotDisplayed()

      // log out :
      composeTestRule.onNodeWithText("Settings").assertIsDisplayed().performClick()
      composeTestRule
          .onNodeWithTag("logOutButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("loginScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
      composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed().assertHasClickAction()

      scenario.close()
    }
  }
}