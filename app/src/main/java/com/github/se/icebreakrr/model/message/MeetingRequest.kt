package com.github.se.icebreakrr.model.message

/*
The data class representing the message sent by the user when he does a meeting request
*/
data class MeetingRequest(
    val targetToken: String = "",
    val senderUID: String = "",
    val message: String = "",
    val picture: String? = null,
    val location: Location? = null,
    val isEnteringMessage: Boolean = true
)
