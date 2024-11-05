package com.github.se.icebreakrr.model.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.github.se.icebreakrr.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MeetingRequestService : FirebaseMessagingService() {

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

  private fun showNotification(title: String, message: String) {
    val channelId = "your_channel_id"
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create notification channel for Android 8.0 and above
    val channel =
        NotificationChannel(channelId, "Channel name", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Channel description" }
    notificationManager.createNotificationChannel(channel)

    val notificationBuilder =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

    notificationManager.notify(0, notificationBuilder.build())
  }
}
