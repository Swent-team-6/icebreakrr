package com.github.se.icebreakrr.model.message

object MeetingRequestManager {
  var meetingRequestViewModel: MeetingRequestViewModel? = null

  fun updateRemoteToken(newToken: String) {
    meetingRequestViewModel?.onRemoteTokenChange(newToken)
  }
}
