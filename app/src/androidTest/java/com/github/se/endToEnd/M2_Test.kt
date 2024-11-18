package com.github.se.endToEnd

// import com.google.firebase.functions.dagger.Provides

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class M2Test {
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
  fun endToEnd2() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      // check if everything is displayed in the around you
      composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
      // click on a profile
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      // check if everything is displayed in alice's profile
      composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
      composeTestRule.onNodeWithTag("username").assertIsDisplayed()
      composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profileDescription").assertIsDisplayed()
      composeTestRule.onNodeWithTag("tagSection").assertIsDisplayed()
      composeTestRule.onNodeWithTag("catchPhrase").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
      composeTestRule.onNodeWithTag("aroundYouProfileScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("requestButton").assertIsDisplayed().assertHasClickAction()
      // check if alice has the good profile :
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed()
      composeTestRule.onNodeWithText("So much to see, so little time").assertIsDisplayed()
      composeTestRule
          .onNodeWithText("I am a software engineer who loves to travel and meet new people.")
          .assertIsDisplayed()
      composeTestRule.onNodeWithText("#Travel").assertIsDisplayed()
      composeTestRule.onNodeWithText("#Software").assertIsDisplayed()
      composeTestRule.onNodeWithText("#Music").assertIsDisplayed()
      // click on send request :
      composeTestRule.onNodeWithTag("requestButton").assertIsDisplayed().performClick()
      // check if everything is displayed :
      composeTestRule.onNodeWithTag("messageTextField").assertIsDisplayed()
      composeTestRule.onNodeWithTag("bluredBackground").assertIsDisplayed()
      composeTestRule.onNodeWithTag("sendButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed().assertHasClickAction()
      // try to send a message :
      composeTestRule
          .onNodeWithTag("messageTextField")
          .performTextInput("Hey, do you want to meet?")
      composeTestRule.onNodeWithText("Hey, do you want to meet?").assertIsDisplayed()
      composeTestRule.onNodeWithTag("sendButton").performClick()
      // reclick on alice profile :
      composeTestRule.onNodeWithText("Alice Inwonderland").assertIsDisplayed().performClick()
      // click on send button :
      composeTestRule.onNodeWithTag("requestButton").performClick()
      // click on cancel button :
      composeTestRule
          .onNodeWithTag("messageTextField")
          .performTextInput("Hey, do you want to meet?")
      composeTestRule.onNodeWithText("Hey, do you want to meet?").assertIsDisplayed()
      composeTestRule.onNodeWithTag("cancelButton").performClick()
      // click on the go back button :
      composeTestRule.onNodeWithTag("goBackButton").performClick()
      // click on the filter button :
      composeTestRule.onNodeWithTag("filterButton").performClick()
      // check if everything is on the filter :
      composeTestRule.onNodeWithTag("Back Button").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("FilterTopBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("FilterTopBarTitle").assertIsDisplayed()
      composeTestRule.onNodeWithTag("GenderButtonMen").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("GenderButtonWomen").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("GenderButtonOther").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("AgeFromTextField").assertIsDisplayed()
      composeTestRule.onNodeWithTag("AgeToTextField").assertIsDisplayed()
      composeTestRule.onNodeWithTag("FilterButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("ResetButton").assertIsDisplayed().assertHasClickAction()
      // put filters to filter no one :
      composeTestRule.onNodeWithTag("AgeFromTextField").performTextInput("500")
      composeTestRule.onNodeWithTag("AgeToTextField").performTextInput("600")
      composeTestRule.onNodeWithText("500").assertIsDisplayed()
      composeTestRule.onNodeWithText("600").assertIsDisplayed()
      composeTestRule.onNodeWithTag("GenderButtonMen").performClick().assertIsSelected()
      composeTestRule.onNodeWithTag("GenderButtonWomen").performClick().assertIsSelected()
      composeTestRule.onNodeWithTag("GenderButtonOther").performClick().assertIsSelected()
      composeTestRule.onNodeWithTag("GenderButtonMen").performClick().assertIsNotSelected()
      composeTestRule.onNodeWithTag("GenderButtonWomen").performClick().assertIsNotSelected()
      composeTestRule.onNodeWithTag("GenderButtonOther").performClick().assertIsNotSelected()
      composeTestRule.onNodeWithTag("ResetButton").performClick()
      // now really filter profiles :
      composeTestRule.onNodeWithTag("GenderButtonMen").performClick()
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
      composeTestRule.onNodeWithTag("FilterButton").performClick()
      // check if filter worked :
      composeTestRule.onNodeWithText("Bob Marley").assertIsDisplayed()
      composeTestRule.onNodeWithText("Indiana Jones").assertIsDisplayed()
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
      // click on edit profile :
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
      // click on notification :
      composeTestRule.onNodeWithText("Notifications").performClick()
      // check if everything exists on notifications :
      composeTestRule.onNodeWithTag("notificationScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("notificationScroll").assertIsDisplayed()
      composeTestRule.onNodeWithTag("notificationFirstText").assertIsDisplayed()
      scenario.close()
    }
  }
}

class TestPermissionDelegate : ActivityCompat.PermissionCompatDelegate {

  // Simulate the permission request being granted without showing the actual permission dialog
  override fun requestPermissions(
      activity: Activity,
      permissions: Array<out String>,
      requestCode: Int
  ): Boolean {
    // Directly simulate granting the permission
    return true
  }

  // Handle activity results (return true as a default response)
  override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
  ): Boolean {
    // Return true to indicate the activity result was successfully handled
    return true
  }
}
