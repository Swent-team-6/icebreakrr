package com.github.se.icebreakrr.model.message

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

private const val TIMER_TARGET_UID = "TARGET_UID"
private const val TIMER_TARGET_TOKEN = "TARGET_TOKEN"
private const val TIMER_TARGET_NAME = "TARGET_NAME"

/**
 * This class is used to measure the time taken for a meeting request to be responded, if it isn't
 * after 20 min, the meeting request is cancelled
 */
class MessagingTimeoutWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
  override fun doWork(): Result {
    val meetingRequestViewModel = MeetingRequestManager.meetingRequestViewModel
    val targetUID = inputData.getString(TIMER_TARGET_UID)
    val targetToken = inputData.getString(TIMER_TARGET_TOKEN)
    val targetName = inputData.getString(TIMER_TARGET_NAME)
    if (targetName != null && targetToken != null && targetUID != null) {
      meetingRequestViewModel?.sendCancellationToBothUsers(
          targetUID, targetToken, targetName, MeetingRequestViewModel.CancellationType.TIME)
      return Result.success()
    }
    return Result.failure()
  }
}
