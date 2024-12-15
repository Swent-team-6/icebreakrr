package com.github.se.icebreakrr.model.message

import android.app.ActivityManager
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
  private val MSG_REQUEST = "Meeting request received!"
  private val DISTANCE_REASON_CANCELLATION = "Reason : You are too far away"
  private val TIME_REASON_CANCELLATION = "Reason : Request reached timeout"
  private val DEFAULT_REASON_CANCELLATION = "Reason : Unknown"
  private val CANCELLED_REASON_CANCELLATION = "Reason : Sender cancelled request"
  private val CLOSED_APP_REASON_CANCELLATION = "Reason : The other user closed the app"
  private val NOTIFICATION_ID = 0

  /**
   * Checks if the application is currently running in the foreground.
   *
   * This method uses the Android `ActivityManager` to retrieve a list of running app processes and
   * checks if the current app's process is marked as being in the foreground.
   *
   * @return `true` if the app is in the foreground, `false` otherwise.
   *
   * The method works by:
   * 1. Obtaining the `ActivityManager` system service to access information about running app
   *    processes.
   * 2. Retrieving the list of running app processes. If the list is null, it returns `false`.
   * 3. Iterating over the list of running processes to find if the current app's process is in the
   *    foreground.
   * 4. Comparing each process's importance level to `IMPORTANCE_FOREGROUND` and checking if the
   *    process name matches the app's package name.
   * 5. Returning `true` if a match is found, indicating the app is in the foreground; otherwise, it
   *    returns `false`.
   */
  fun isAppInForeground(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (activityManager == null) {
      Log.w("MeetingRequestService", "ActivityManager not available")
      return false
    }
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
    val title = remoteMessage.data["title"] ?: "null"
    val senderName = remoteMessage.data["senderName"] ?: "null"
    val message = remoteMessage.data["message"] ?: "null"

    when (title) {
      "MEETING REQUEST" -> {
        val locationMessage = remoteMessage.data["locationMessage"] ?: "null"
        val locationString = remoteMessage.data["location"] ?: "null"
        val latitudeString = locationString.split(", ")[0]
        val longitudeString = locationString.split(", ")[1]
        val location = (latitudeString.toDouble() to longitudeString.toDouble())
        MeetingRequestManager.meetingRequestViewModel?.addToMeetingRequestInbox(
            senderUid, message, locationMessage, location) {
              MeetingRequestManager.meetingRequestViewModel?.updateInboxOfMessages {}
            }
        if (!isAppInForeground()) {
          showNotification(MSG_REQUEST, "from : $senderName")
        }
      }
      "MEETING RESPONSE" -> {
        val accepted = remoteMessage.data["accepted"]?.toBoolean() ?: false
        val locationString = remoteMessage.data["location"]?.trim('(', ')') ?: "null"
        val latitudeString = locationString.split(", ")[0]
        val longitudeString = locationString.split(", ")[1]
        val location = Pair(latitudeString.toDouble(), longitudeString.toDouble())
        val locationAndMessage = Pair(message, location)
        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid) {
          MeetingRequestManager.meetingRequestViewModel?.updateInboxOfMessages {}
        }
        if (accepted) {
          MeetingRequestManager.meetingRequestViewModel?.confirmMeetingRequest(
              senderUid, locationAndMessage) {
                Log.e("LOCATION CONFIRMATION", "failed to confirm the meeting location")
              }
          if (!isAppInForeground()) {
            showNotification(senderName + MSG_RESPONSE_ACCEPTED, "")
          }
        } else {
          if (!isAppInForeground()) {
            showNotification(senderName + MSG_RESPONSE_REJECTED, "")
          }
        }
        MeetingRequestManager.meetingRequestViewModel?.stopMeetingRequestTimer(senderUid, this)
      }
      "MEETING CANCELLATION" -> {
        val stringReason =
            when (message) {
              "DISTANCE" -> DISTANCE_REASON_CANCELLATION
              "TIME" -> TIME_REASON_CANCELLATION
              "CANCELLED" -> CANCELLED_REASON_CANCELLATION
              "CLOSED" -> CLOSED_APP_REASON_CANCELLATION
              else -> DEFAULT_REASON_CANCELLATION
            }

        MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestSent(senderUid) {
          MeetingRequestManager.meetingRequestViewModel?.removeFromMeetingRequestInbox(senderUid) {
            MeetingRequestManager.meetingRequestViewModel?.removeChosenLocalisation(senderUid) {
              MeetingRequestManager.meetingRequestViewModel?.updateInboxOfMessages {}
            }
          }
        }
        MeetingRequestManager.meetingRequestViewModel?.stopMeetingRequestTimer(senderUid, this)
        showNotification("Cancelled meeting with $senderName", stringReason)
      }
      "ENGAGEMENT NOTIFICATION" -> {
        // Only show engagement notifications if app is in background
        if (!isAppInForeground()) {
          val name = remoteMessage.data["senderName"] ?: "null"
          showNotification(
              "A person with similar interests is close by !",
              "The user $name has the common tag : $message")
        } else {
          Log.d(
              "NotificationDebug", "Skipping engagement notification because app is in foreground")
        }
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
  fun showNotification(title: String, message: String) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel =
        NotificationChannel(
                MSG_CHANNEL_ID, MSG_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Channel for messaging notifications" }
    notificationManager.createNotificationChannel(channel)

    val notificationBuilder = createNotificationBuilder(title, message)

    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
  }

  /**
   * Create a notification builder
   *
   * @param title : the title of the notification
   * @param message : the message of the notification
   */
  fun createNotificationBuilder(title: String, message: String): NotificationCompat.Builder {
    return NotificationCompat.Builder(this, MSG_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with app icon
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
  }
}
