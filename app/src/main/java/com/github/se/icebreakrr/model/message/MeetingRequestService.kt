package com.github.se.icebreakrr.model.message

import android.app.ActivityManager
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
  private val MSG_RESPONSE_ACCEPTED = " accepted your meeting request!"
  private val MSG_RESPONSE_REJECTED = " rejected your meeting request :("
  private val MSG_CONFIRMATION = "Meeting confirmation from : "
  private val MSG_REQUEST = "Meeting request received!"
  private val DISTANCE_REASON_CANCELLATION = "Reason : You went too far away"
  private val DEFAULT_REASON_CANCELLATION = "Reason : Unknown"
  private val NOTIFICATION_ID = 0

  /**
   * Checks if the application is currently running in the foreground.
   *
   * This method uses the Android `ActivityManager` to retrieve a list of running app processes
   * and checks if the current app's process is marked as being in the foreground.
   *
   * @return `true` if the app is in the foreground, `false` otherwise.
   *
   * The method works by:
   * 1. Obtaining the `ActivityManager` system service to access information about running app processes.
   * 2. Retrieving the list of running app processes. If the list is null, it returns `false`.
   * 3. Iterating over the list of running processes to find if the current app's process is in the foreground.
   * 4. Comparing each process's importance level to `IMPORTANCE_FOREGROUND` and checking if the process name matches the app's package name.
   * 5. Returning `true` if a match is found, indicating the app is in the foreground; otherwise, it returns `false`.
   */
  private fun isAppInForeground(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses ?: return false
    val packageName = packageName
    
    for (appProcess in appProcesses) {
      if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
          appProcess.processName == packageName) {
        return true
      }
    }
    return false
  }

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
        val name = remoteMessage.data["senderName"] ?: "null"
        MeetingRequestManager.meetingRequestViewModel?.addToMeetingRequestInbox(senderUid, message)
        MeetingRequestManager.meetingRequestViewModel?.updateInboxOfMessagesAndThen() {}
        showNotification(MSG_REQUEST, "from : $name")
      }
      "MEETING RESPONSE" -> {
        val name = remoteMessage.data["senderName"] ?: "null"
        val accepted = remoteMessage.data["accepted"]?.toBoolean() ?: false
        val senderToken = remoteMessage.data["senderToken"] ?: "null"

        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid)
        if (accepted) {
          showNotification(name + MSG_RESPONSE_ACCEPTED, "")
          MeetingRequestManager.meetingRequestViewModel?.setMeetingConfirmation(
              targetToken = senderToken,
              newMessage = "The meeting with ${MeetingRequestManager.ourName} is confirmed !")
          MeetingRequestManager.meetingRequestViewModel?.sendMeetingConfirmation()
        } else {
          showNotification(name + MSG_RESPONSE_REJECTED, "")
          MeetingRequestManager.meetingRequestViewModel?.setMeetingConfirmation(
              targetToken = senderToken,
              newMessage = "The meeting with ${MeetingRequestManager.ourName} is cancelled !")
        }
      }
      "MEETING CONFIRMATION" -> {
        val name = remoteMessage.data["senderName"] ?: "null"
        val hashedLocation = remoteMessage.data["location"] ?: "null"
        val geoHash = GeoHash.fromGeohashString(hashedLocation)
        val location = geoHash.boundingBox.center
        showNotification(MSG_CONFIRMATION + name, location.toString())
      }
      "MEETING CANCELLATION" -> {
        val name = remoteMessage.data["senderName"] ?: "null"
        Log.d("CANCELLATION REASON", message)
        val stringReason =
            when (message) {
              "distance" -> DISTANCE_REASON_CANCELLATION
              else -> DEFAULT_REASON_CANCELLATION
            }
        showNotification("Cancelled meeting with $name", stringReason)
        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestInbox(senderUid)
        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid)
      }
      "ENGAGEMENT NOTIFICATION" -> {
        // Only show engagement notifications if app is in background
        // Commented out for now as the locations don't update in the background so the feature can't work until that is changed
        //if (!isAppInForeground()) {
          val name = remoteMessage.data["senderName"] ?: "null"
          showNotification(
              "A person with similar interests is close by !",
              "The user $name has the common tag : $message")
        //} else {
        //  Log.d("NotificationDebug", "Skipping engagement notification because app is in foreground")
        //}
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
