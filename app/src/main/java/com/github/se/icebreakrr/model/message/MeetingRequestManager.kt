package com.github.se.icebreakrr.model.message

/*
   Class that stores a meetingViewModel that can be accessed from everywhere (useful for MeetingRequestService)
*/
object MeetingRequestManager {
  var meetingRequestViewModel: MeetingRequestViewModel? = null

  /**
   * Update the fcm token stored in the Firebase to keep it up to date
   *
   * @param newToken: the new token
   */
  fun updateRemoteToken(newToken: String) {
    meetingRequestViewModel?.onRemoteTokenChange(newToken)
  }
}
