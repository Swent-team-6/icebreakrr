package com.github.se.icebreakrr.model.ai

import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AiRepositoryChatGPTTest {

  private lateinit var mockWebServer: MockWebServer
  private lateinit var aiRepository: AiRepositoryChatGPT
  private val validApiKey = "test-api-key"

  @Before
  fun setup() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    aiRepository =
        AiRepositoryChatGPT(
            apiKey = "test-api-key",
            baseUrl = mockWebServer.url("/").toString() // Point vers MockWebServer
            )
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test generateResponse with valid request returns expected response`() = runBlocking {
    // Arrange
    val expectedResponse = "This is a response"
    mockWebServer.enqueue(
        MockResponse()
            .setResponseCode(200)
            .setBody(
                """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "$expectedResponse"
                            }
                        }
                    ]
                }
                """
                    .trimIndent()))
    val request =
        AiRequest(
            systemPrompt = "Hello system",
            userPrompt = "Hello user",
            model = "gpt-4o-mini",
            temperature = 0.7,
            maxTokens = 50)

    // Act
    val result = aiRepository.generateResponse(request)

    // Assert
    assertEquals(expectedResponse, result)
  }

  @Test
  fun `test generateResponse with invalid response throws exception`() = runBlocking {
    // Arrange: Mock invalid response
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))
    val request =
        AiRequest(
            systemPrompt = "Hello system",
            userPrompt = "Hello user",
            model = "gpt-4o-mini",
            temperature = 0.7,
            maxTokens = 50)

    // Act
    try {
      aiRepository.generateResponse(request)
      // Fail if no exception is thrown
      assert(false) { "Expected IOException to be thrown" }
    } catch (e: IOException) {
      // Assert
      assertTrue(e.message!!.contains("Invalid JSON response"))
    }
  }

  @Test
  fun `test generateResponse handles network failure`() = runBlocking {
    // Arrange
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
    val request = AiRequest(systemPrompt = "System", userPrompt = "User")

    // Act & Assert
    val exception =
        assertThrows(IOException::class.java) {
          runBlocking { aiRepository.generateResponse(request) }
        }
    assertTrue(exception.message!!.contains("error"))
  }

  @Test
  fun `test generateResponse with empty API key throws IllegalArgumentException`() = runBlocking {
    // Arrange
    val invalidApiKeyRepository = AiRepositoryChatGPT("")

    val request = AiRequest(systemPrompt = "System", userPrompt = "User")

    // Act & Assert
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { invalidApiKeyRepository.generateResponse(request) }
        }
    assertEquals("API key cannot be blank.", exception.message)
  }

  @Test
  fun `test generateResponse with blank systemPrompt throws IllegalArgumentException`() =
      runBlocking {
        // Arrange
        val request = AiRequest(systemPrompt = "", userPrompt = "User")

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
              runBlocking { aiRepository.generateResponse(request) }
            }
        assertEquals("System prompt cannot be blank.", exception.message)
      }

  @Test
  fun `test generateResponse with blank userPrompt throws IllegalArgumentException`() =
      runBlocking {
        // Arrange
        val request = AiRequest(systemPrompt = "System", userPrompt = "")

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
              runBlocking { aiRepository.generateResponse(request) }
            }
        assertEquals("User prompt cannot be blank.", exception.message)
      }

  @Test
  fun `test generateResponse with blank model throws IllegalArgumentException`() {
    // Arrange
    val request = AiRequest(systemPrompt = "System", userPrompt = "User", model = "")

    // Act & Assert
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { aiRepository.generateResponse(request) }
        }
    assertEquals("Model cannot be blank.", exception.message)
  }

  @Test
  fun `test generateResponse with invalid temperature throws IllegalArgumentException`() {
    // Arrange
    val request = AiRequest(systemPrompt = "System", userPrompt = "User", temperature = 2.5)

    // Act & Assert
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { aiRepository.generateResponse(request) }
        }
    assertEquals("Temperature must be between 0.0 and 1.0", exception.message)
  }

  @Test
  fun `test generateResponse with invalid maxTokens throws IllegalArgumentException`() {
    // Arrange
    val request = AiRequest(systemPrompt = "System", userPrompt = "User", maxTokens = 0)

    // Act & Assert
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          runBlocking { aiRepository.generateResponse(request) }
        }
    assertEquals("Max tokens must be greater than 0", exception.message)
  }
}
