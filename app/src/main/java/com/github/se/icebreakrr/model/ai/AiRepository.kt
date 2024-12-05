package com.github.se.icebreakrr.model.ai

interface AiRepository {

  suspend fun generateResponse(request: AiRequest): String
}
