package com.github.se.icebreakrr.model.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.se.icebreakrr.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/*
   Class that deals with the messaging system of the application. Manages the messages received and the fcm token sent by Firebase
*/
class MeetingRequestService : FirebaseMessagingService() {

  private val MSG_CHANNEL_ID = "message_channel_id"
  private val MSG_CHANNEL_NAME = "channel_message"
  private val MSG_RESPONSE_ACCEPTED = " accepted your meeting request!"
  private val MSG_RESPONSE_REJECTED = " rejected your meeting request :("
  private val MSG_CONFIRMATION = " has chosen the location for your meeting!"
  private val MSG_REQUEST = "Meeting request received!"
  private val DISTANCE_REASON_CANCELLATION = "Reason : You went too far away"
  private val DEFAULT_REASON_CANCELLATION = "Reason : Unknown"
  private val NOTIFICATION_ID = 0
  private val MSG_CONFIRMATION_INFO = "Go to your heatmap to see the pin!"

  /**
   * Manage the messages received that were sent by other users of the app
   *
   * @param remoteMessage : The messages received, that has a title and a body
   */
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    val senderUid = remoteMessage.data["senderUID"] ?: "null"
    val message = remoteMessage.data["message"] ?: "null"
    val title = remoteMessage.data["title"] ?: "null"
    val senderName = remoteMessage.data["senderName"] ?: "null"

    when (title) {
      "MEETING REQUEST" -> {
        MeetingRequestManager.meetingRequestViewModel?.addToMeetingRequestInbox(
            senderUid, message) {
              MeetingRequestManager.meetingRequestViewModel?.updateInboxOfMessages {}
            }
        showNotification(MSG_REQUEST, "from : $senderName")

      }
      "MEETING RESPONSE" -> {
        val accepted = remoteMessage.data["accepted"]?.toBoolean() ?: false

        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid) {
          MeetingRequestManager.meetingRequestViewModel?.updateInboxOfMessages {}
        }
        if (accepted) {
          showNotification(senderName + MSG_RESPONSE_ACCEPTED, "")
          MeetingRequestManager.meetingRequestViewModel?.addPendingLocation(senderUid) {}
        } else {
          showNotification(senderName + MSG_RESPONSE_REJECTED, "")
        }
      }
      "MEETING CONFIRMATION" -> {
        val locationString = remoteMessage.data["location"] ?: "null"
        val latitudeString = locationString.split(", ")[0]
        val longitudeString = locationString.split(", ")[1]
        MeetingRequestManager.meetingRequestViewModel?.confirmMeetingLocation(
            senderUid, Pair(message, Pair(latitudeString.toDouble(), longitudeString.toDouble()))) {
              Log.e("MeetingRequestService", "error when confirmMeetingLocation : ${it.message}")
            }
        showNotification(senderName + MSG_CONFIRMATION, MSG_CONFIRMATION_INFO)
      }
      "MEETING CANCELLATION" -> {
        val stringReason =
            when (message) {
              "distance" -> DISTANCE_REASON_CANCELLATION
              else -> DEFAULT_REASON_CANCELLATION
            }
        showNotification("Cancelled meeting with $senderName", stringReason)
        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestInbox(senderUid)
        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid) {}
      }
      "ENGAGEMENT NOTIFICATION" -> {
        showNotification(
            "A person with similar interests is close by !",
            "The user $senderName has the common tag : $message")
      }
    }
  }

  /**
   * Manage the new fcm token sent by Firebase for our phone
   *
   * @param token : the new fcm token for our phone
   */
  override fun onNewToken(token: String) {
    super.onNewToken(token)
    MeetingRequestManager.updateRemoteToken(token)
  }

  /**
   * Shows the notification of the message received
   *
   * @param title : the title of the notification
   * @param message : the message of our notification
   */
  private fun showNotification(title: String, message: String) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel =
        NotificationChannel(
                MSG_CHANNEL_ID, MSG_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Channel for messaging notifications" }
    notificationManager.createNotificationChannel(channel)

    val notificationBuilder =
        NotificationCompat.Builder(this, MSG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
  }
}
