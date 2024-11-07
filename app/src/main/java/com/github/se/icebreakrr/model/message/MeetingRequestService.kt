package com.github.se.icebreakrr.model.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.github.se.icebreakrr.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MeetingRequestService : FirebaseMessagingService() {

  private val MSG_CHANNEL_ID = "message_channel_id"
  private val MSG_CHANNEL_NAME = "channel_message"
  private val NOTIFICATION_ID = 0

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    // Check if the message contains a notification payload
    remoteMessage.notification?.let { notification ->
      // Extract title and body from the notification payload
      val title = notification.title ?: "Default Title"
      val body = notification.body ?: "Default Body"

      // Show the notification
      showNotification(title, body)
    }
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
  }

  fun showNotification(title: String, message: String) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create notification channel for Android 8.0 and above
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
