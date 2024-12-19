package com.github.se.endToEnd

import android.content.Intent
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.se.icebreakrr.Icebreakrr
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

private const val TIME = "TIME"
private const val MESSAGE = "message"
private const val TITLE = "title"
private const val MEETING_CANCELLATION = "MEETING CANCELLATION"
private const val DISTANCE = "DISTANCE"
private const val CANCELLED = "CANCELLED"
private const val CLOSED = "CLOSED"
private const val MEETING_REQUEST = "MEETING REQUEST"

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ReceiveRequestFlowEndToEnd {
  val intent =
      Intent(InstrumentationRegistry.getInstrumentation().targetContext, Icebreakrr::class.java)

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
            .addData("senderUID", "0")
            .addData("senderName", "Alice Inwonderland")
  }

  @Test
  fun receiveRequestDeclineFlowEndToEnd() {
    ActivityScenario.launch<Icebreakrr>(intent).use { scenario ->
      receiveRequestAndCheckProfile()

      composeTestRule.waitForIdle()

      // decline and check that everything updates
      composeTestRule.onNodeWithTag("declineButton").performClick()
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      composeTestRule.onNodeWithTag("badgeHeatmap").assertIsNotDisplayed()

      // go on heatmap
      composeTestRule.onNodeWithTag("navItem_Map").performClick()
      scenario.close()
    }
  }

  @Test
  fun receiveRequestTimeCancellationFlowEndToEnd() {
    ActivityScenario.launch<Icebreakrr>(intent).use { scenario ->
      receiveRequestAndCheckProfile()

      // receives a cancellation for time :
      message.addData(MESSAGE, TIME).addData(TITLE, MEETING_CANCELLATION)
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disappear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun receiveRequestAndDistanceCancellationFlowEndToEnd() {
    ActivityScenario.launch<Icebreakrr>(intent).use { scenario ->
      receiveRequestAndCheckProfile()

      // receives a cancellation for distance :
      message.addData(MESSAGE, DISTANCE).addData(TITLE, MEETING_CANCELLATION)
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun receiveRequestCancelledCancellationFlowEndToEnd() {
    ActivityScenario.launch<Icebreakrr>(intent).use { scenario ->
      receiveRequestAndCheckProfile()

      // receives a cancellation because the other one cancelled :
      message.addData(MESSAGE, CANCELLED).addData(TITLE, MEETING_CANCELLATION)
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun receiveRequestClosedCancellationFlowEndToEnd() {
    ActivityScenario.launch<Icebreakrr>(intent).use { scenario ->
      receiveRequestAndCheckProfile()

      // receives a cancellation because the other one cancelled :
      message.addData(MESSAGE, CLOSED).addData(TITLE, MEETING_CANCELLATION)
      service.onMessageReceived(message.build())

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      scenario.close()
    }
  }

  @Test
  fun receiveRequestAcceptFlowEndToEnd() {
    ActivityScenario.launch<Icebreakrr>(intent).use { scenario ->
      receiveRequestAndCheckProfile()

      // accept and check everything updates
      composeTestRule.onNodeWithTag("acceptButton").performClick()
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      composeTestRule.onNodeWithTag("badgeHeatmap", useUnmergedTree = true).assertIsDisplayed()
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()

      // go in heatmap
      composeTestRule
          .onNodeWithTag("navItem_Map")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitForIdle()

      // check that alice disapear :
      composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
      composeTestRule
          .onNodeWithTag("badgeNotification", useUnmergedTree = true)
          .assertIsNotDisplayed()
      scenario.close()
    }
  }

  /**
   * This function checks everything in the around you Then it receives a meeting request with two
   * messages and a location check that you received the message on the UI (badge) go to
   * notification and check that the UI is coherent click on the request you received (Alice) checks
   * everything in Alice's profile and the location of the request comes back in Alice's profile
   */
  private fun receiveRequestAndCheckProfile() {
    // check if everything is displayed in the around you
    composeTestRule.onNodeWithTag("aroundYouScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // receives a message
    message
        .addData(TITLE, MEETING_REQUEST)
        .addData(MESSAGE, "Hey, do you want to meet?")
        .addData("locationMessage", "Let's meet in the 3'rd floor of the building!")
        .addData("location", "46.0, 6.0")
    service.onMessageReceived(message.build())

    composeTestRule.waitForIdle()

    // check that we have received the badge :
    composeTestRule.onNodeWithTag("badgeNotification", useUnmergedTree = true).assertIsDisplayed()

    // go to notification to see the profile :
    composeTestRule
        .onNodeWithTag("navItem_Notifications")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithTag("profileCard").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("sentButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("profileCard").assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("inboxButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    // click on Alice's profile :
    composeTestRule.onNodeWithTag("profileCard").performClick()
    // check if it is Alice's profile :
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertIsDisplayed()
    composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tagSection").assertExists().performScrollTo().assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("catchPhrase")
        .assertExists()
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("NotificationProfileScreen").assertExists()
    // check that the elements in the box to accept/decline are well desplayed :
    composeTestRule
        .onNodeWithText("Hey, do you want to meet?")
        .assertExists()
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("Accept/DeclineBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RequestMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("declineButton").assertIsDisplayed().assertHasClickAction()
    // click on the location button and come back
    composeTestRule
        .onNodeWithTag("locationButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag("goBackButton")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
  }
}
