package com.github.se.icebreakrr.model.message

/** Those classes represent a Json body */
data class SendMessageDto(val to: String?, val notification: NotificationBody)

data class NotificationBody(val title: String, val body: String)
