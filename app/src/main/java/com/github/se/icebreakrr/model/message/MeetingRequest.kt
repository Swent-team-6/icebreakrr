package com.github.se.icebreakrr.model.message

/*
The message sent by the user when he does a meeting request
*/
data class MeetingRequest(
    val targetToken: String = "",
    val message1: String = "",
    val message2: String = "",
    val location: String = ""
)

/*
The message sent to respond to a user meeting request
*/
data class MeetingResponse(
    val targetToken: String = "",
    val message: String = "",
    val accepted: Boolean = false,
    val location: String = ""
)

/*
The message sent to confirm a meeting request
*/
data class MeetingConfirmation(
    val targetToken: String = "",
    val message: String = "",
    val location: String = ""
)

/*
The message sent to cancel a meeting request
*/
data class MeetingCancellation(
    val targetToken: String = "",
    val senderUID: String = "",
    val nameTargetUser: String = "",
    val message: String = "",
)
