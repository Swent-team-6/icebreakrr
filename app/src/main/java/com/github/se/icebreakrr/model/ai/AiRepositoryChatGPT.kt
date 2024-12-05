package com.github.se.icebreakrr.model.ai

import java.io.IOException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

// Data class for encapsulating request parameters
data class AiRequest(
    val systemPrompt: String,
    val userPrompt: String,
    val model: String = "gpt-4o-mini", // Default model
    val temperature: Double = 0.7,
    val maxTokens: Int = 50
)

class AiRepositoryChatGPT(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1/chat/completions"
) : AiRepository {

  private val client = OkHttpClient()

  /**
   * Generates a response from the AI model based on the given request parameters.
   *
   * @param request The encapsulated request parameters.
   * @return The response text from the AI model.
   * @throws IOException if an error occurs during the API call.
   */
  override suspend fun generateResponse(request: AiRequest): String =
      withContext(Dispatchers.IO) {
        // Validate input
        validateRequest(request)
        try {
          // Construct the request body
          val requestBody =
              JSONObject()
                  .apply {
                    put("model", request.model)
                    put(
                        "messages",
                        JSONArray().apply {
                          put(
                              JSONObject().apply {
                                put("role", "system")
                                put("content", request.systemPrompt)
                              })
                          put(
                              JSONObject().apply {
                                put("role", "user")
                                put("content", request.userPrompt)
                              })
                        })
                    put("temperature", request.temperature)
                    put("max_tokens", request.maxTokens)
                  }
                  .toString()

          // Build the HTTP request
          val httpRequest =
              Request.Builder()
                  .url(baseUrl)
                  .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                  .addHeader("Authorization", "Bearer $apiKey")
                  .build()

          // Execute the request and handle the response
          suspendCoroutine { continuation ->
            client
                .newCall(httpRequest)
                .enqueue(
                    object : Callback {
                      override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWith(Result.failure(e))
                      }

                      override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                          val responseBody = response.body?.string()
                          if (responseBody != null) {
                            val responseText =
                                JSONObject(responseBody)
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content")
                            continuation.resumeWith(Result.success(responseText))
                          } else {
                            continuation.resumeWith(
                                Result.failure(IOException("Empty response from the server")))
                          }
                        } else {
                          continuation.resumeWith(
                              Result.failure(
                                  IOException("Error: ${response.code} - ${response.message}")))
                        }
                      }
                    })
          }
        } catch (e: Exception) {
          throw IOException("Unexpected error occurred: ${e.message}", e)
        }
      }

  /**
   * Validates the input request parameters.
   *
   * @param request The request to validate.
   * @throws IllegalArgumentException if any parameter is invalid.
   */
  private fun validateRequest(request: AiRequest) {
    require(request.systemPrompt.isNotBlank()) { "System prompt cannot be blank." }
    require(request.userPrompt.isNotBlank()) { "User prompt cannot be blank." }
    require(apiKey.isNotBlank()) { "API key cannot be blank." }
  }
}
