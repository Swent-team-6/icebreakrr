package com.github.se.icebreakrr.model.message

data class MeetingRequest(
    val targetToken: String = "",
    val senderUID: String = "",
    val message: String = "",
    val picture: String? = null,
    val location: Location? = null,
    val isEnteringMessage: Boolean = true
)

data class MeetingResponse(
    val targetToken: String = "",
    val senderUID: String = "",
    val accept: Boolean? = null,
    val message: String = "",
    val isEnteringMessage: Boolean = true
)
