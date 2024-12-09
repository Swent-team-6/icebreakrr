package com.github.se.icebreakrr.model.message

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * This class is used to measure the time taken for a meeting request to be responded, if it isn't
 * after 20 min, the meeting request is cancelled
 */
class MessagingTimeoutWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
  override fun doWork(): Result {
    val meetingRequestViewModel = MeetingRequestManager.meetingRequestViewModel
    val targetUID = inputData.getString("TARGET_UID") ?: "Unknown UID"
    val targetToken = inputData.getString("TARGET_TOKEN") ?: "Unknown Key"
    val targetName = inputData.getString("TARGET_NAME") ?: "Unknown name"
    meetingRequestViewModel?.sendCancellationToBothUsers(
        targetUID, targetToken, targetName, MeetingRequestViewModel.CancellationType.TIME)
    return Result.success()
  }
}
