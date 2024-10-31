package com.github.se.icebreakrr.model.message

data class MeetingRequest(
    val targetToken: String = "",
    val senderID: String = "",
    val message: String = "",
    val picture: String? = null,
    val location: Location? = null
)
