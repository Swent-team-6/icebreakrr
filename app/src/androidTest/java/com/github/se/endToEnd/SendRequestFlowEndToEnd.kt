package com.github.se.endToEnd

import android.content.Intent
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.icebreakrr.MainActivity
import com.github.se.icebreakrr.model.message.MeetingRequestService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ALICE = "Alice Inwonderland"

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SendRequestFlowEndToEnd {
  val intent =
      Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
  @Inject lateinit var authInjected: FirebaseAuth
  @Inject lateinit var firestoreInjected: FirebaseFirestore
  @Inject lateinit var authStateListener: FirebaseAuth.AuthStateListener

  private lateinit var message: RemoteMessage.Builder
  private lateinit var service: MeetingRequestService

  @get:Rule var hiltRule = HiltAndroidRule(this)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    hiltRule.inject()
    intent.putExtra("IS_TESTING", true)
    ActivityCompat.setPermissionCompatDelegate(TestPermissionDelegate())

    service = MeetingRequestService()
    message =
        RemoteMessage.Builder("test_sender")
            .setMessageId("12345")
            .addData("senderUID", "2")
            .addData("senderName", "Alice Inwonderland")
  }

  @Test
  fun sendYouCancelRequestFlowEndToEnd() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      sendRequestAliceAndCheck()
      // click on alice to cancel the meeting request
      composeTestRule.onNodeWithText(ALICE).assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("confirmDialog").assertIsDisplayed()
      composeTestRule.onNodeWithText("No").assertIsDisplayed().assertHasClickAction().performClick()
      composeTestRule.onNodeWithText(ALICE).assertIsDisplayed().performClick()
      composeTestRule.onNodeWithTag("confirmDialog").assertIsDisplayed()
      composeTestRule
          .onNodeWithText("Yes")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(ALICE).assertIsNotDisplayed()

      // go to inbox to be sure it is empty :
      composeTestRule.onNodeWithTag("inboxButton").performClick()
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun sendRequestDeclineFlowEndToEnd() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      sendRequestAliceAndCheck()

      // receives a message
      message
          .addData("title", "MEETING RESPONSE")
          .addData("accepted", "false")
          .addData("location", "(46.0, 6.0)")
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun sendRequestDistanceCancellationFlowEndToEnd() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      sendRequestAliceAndCheck()

      // receives a cancellation for time :
      message.addData("message", "DISTANCE").addData("title", "MEETING CANCELLATION")
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun sendRequestCancelledCancellationFlowEndToEnd() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      sendRequestAliceAndCheck()

      // receives a cancellation for time :
      message.addData("message", "CANCELLED").addData("title", "MEETING CANCELLATION")
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun sendRequestClosedCancellationFlowEndToEnd() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      sendRequestAliceAndCheck()

      // receives a cancellation for time :
      message.addData("message", "CLOSED").addData("title", "MEETING CANCELLATION")
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun sendAcceptRequestFlowEndToEnd() {
    ActivityScenario.launch<MainActivity>(intent).use { scenario ->
      sendRequestAliceAndCheck()

      // receives a message
      message
          .addData("title", "MEETING RESPONSE")
          .addData("accepted", "true")
          .addData("location", "(46.0, 6.0)")
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that notification cleared and badge in heatmap :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag("inboxButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule.onNodeWithTag("badgeHeatmap", useUnmergedTree = true).assertIsDisplayed()

      // go on heat map :
      composeTestRule
          .onNodeWithTag("navItem_Map")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      scenario.close()
    }
  }

  private fun sendRequestAliceAndCheck() {
    // check if everything is displayed in the around you
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    // click on a profile
    composeTestRule.onNodeWithText(ALICE).assertIsDisplayed().performClick()
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
    composeTestRule.onNodeWithText(ALICE).assertIsDisplayed()
    composeTestRule.onNodeWithText("«So much to see, so little time»").assertIsDisplayed()
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
    composeTestRule.onNodeWithTag("messageTextField").performTextInput("Hey, do you want to meet?")
    composeTestRule.onNodeWithText("Hey, do you want to meet?").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sendButton").performClick()
    // choose location screen :
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("LocationSelectorMapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addTextAndSendLocationBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addDetailsTextField").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("buttonSendMessageLocation")
        .assertIsDisplayed()
        .assertHasClickAction()
    // put a message :
    composeTestRule
        .onNodeWithTag("addDetailsTextField")
        .performTextInput("Lets meet in the building, 3rd floor!")
    composeTestRule.onNodeWithText("Lets meet in the building, 3rd floor!").assertIsDisplayed()
    // send the request :
    composeTestRule.onNodeWithTag("buttonSendMessageLocation").performClick()

    // go to notification to check if you have sended a message
    composeTestRule.onNodeWithTag("navItem_Notifications").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("inboxButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("sentButton").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(ALICE).assertIsDisplayed()
  }
}
