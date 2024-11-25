package com.github.se.icebreakrr.model.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import ch.hsr.geohash.GeoHash
import com.github.se.icebreakrr.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/*
   Class that deals with the messaging system of the application. Manages the messages received and the fcm token sent by Firebase
*/
class MeetingRequestService : FirebaseMessagingService() {

  private val MSG_CHANNEL_ID = "message_channel_id"
  private val MSG_CHANNEL_NAME = "channel_message"
  private val NOTIFICATION_ID = 0

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
    when (title) {
      "MEETING REQUEST" -> {

        MeetingRequestManager.meetingRequestViewModel?.addToMeetingRequestInbox(senderUid, message)
      }
      "MEETING RESPONSE" -> {

        val name = remoteMessage.data["senderName"] ?: "null"
        val accepted = remoteMessage.data["accepted"]?.toBoolean() ?: false
        val senderToken = remoteMessage.data["senderToken"] ?: "null"
        val acceptedString = if (accepted) "accepted" else "rejected"

        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid)
        showNotification("Meeting response from : $name", message)
        MeetingRequestManager.meetingRequestViewModel?.setMeetingConfirmation(
            targetToken = senderToken,
            newMessage = "The meeting with ${MeetingRequestManager.ourName} is confirmed !")
        MeetingRequestManager.meetingRequestViewModel?.sendMeetingConfirmation()
        Log.d("CONFIRMATION MESSAGE SENT", "YES !")
      }
      "MEETING CONFIRMATION" -> {

        Log.d("CONFIRMATION MESSAGE RECEIVED", "YES !")
        val name = remoteMessage.data["senderName"] ?: "null"
        val hashedLocation = remoteMessage.data["location"] ?: "null"
        val geoHash = GeoHash.fromGeohashString(hashedLocation)
        val location = geoHash.boundingBox.center
        showNotification("Meeting confirmation from : $name", location.toString())
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
