package com.dkb.challenge

import com.dkb.challenge.controller.UrlShortenerController
import com.dkb.challenge.exception.OriginalUrlNotFoundException
import com.dkb.challenge.model.domain.OriginalUrl
import com.dkb.challenge.model.domain.ShortenedUrl
import com.dkb.challenge.model.dto.ResolvedUrl
import com.dkb.challenge.model.dto.ShortenRequest
import com.dkb.challenge.model.dto.ShortenResponse
import com.dkb.challenge.service.UrlShorteningService
import com.dkb.challenge.util.toURI
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebMvcTest(controllers = [UrlShortenerController::class])
class UrlShortenerControllerIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var urlShorteningService: UrlShorteningService

    private val baseUrl = "https://short.ly"

    @Test
    fun `should shorten a URL successfully`(): Unit = runBlocking {
        val request = ShortenRequest("https://example.com")
        val expectedShortenedUrl = "$baseUrl/abc123"
        val expectedResponse = ShortenResponse(expectedShortenedUrl.toURI())

        // Mock service behavior
        coEvery { urlShorteningService.shortenUrl(OriginalUrl(request.url.toURI())) } returns ShortenedUrl(expectedShortenedUrl.toURI())

        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(ShortenResponse::class.java)
            .isEqualTo(expectedResponse)
    }

    @Test
    fun `should resolve a shortened URL successfully`(): Unit = runBlocking {
        val shortenedUrl = "$baseUrl/abc123"
        val expectedOriginalUrl = "https://example.com"
        val expectedResponse = ResolvedUrl(expectedOriginalUrl.toURI())

        // Mock service behavior
        coEvery { urlShorteningService.getOriginalUrl(ShortenedUrl(shortenedUrl.toURI())) } returns OriginalUrl(expectedOriginalUrl.toURI())

        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/urls/resolve")
                    .queryParam("request", shortenedUrl)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(ResolvedUrl::class.java)
            .isEqualTo(expectedResponse)
    }

    @Test
    fun `should return 404 when short URL is not found`(): Unit = runBlocking {
        val shortenedUrl = "$baseUrl/notfound"

        // Mock service to throw not found exception
        coEvery { urlShorteningService.getOriginalUrl(ShortenedUrl(shortenedUrl.toURI())) } throws OriginalUrlNotFoundException("URL not found")

        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/urls/resolve")
                    .queryParam("request", "https://short.ly/notfound")
                    .build()
            }
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.message").isEqualTo("URL not found")
            .jsonPath("$.errorDetails").doesNotExist()
    }

    @Test
    fun `should return bad request for invalid original URL`() {
        val request = ShortenRequest("invalid-short-url")

        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Invalid argument. Please verify your input and try again.")
    }

    @Test
    fun `should return bad request for invalid URL for resolving original url`() {
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/urls/resolve")
                    .queryParam("request", "invalid-short-url")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Invalid request parameter. Please verify your input and try again.")
    }

    @Test
    fun `should return 400 when request body is missing`() {
        webTestClient.post()
            .uri("/api/v1/urls/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Invalid request: Missing or unreadable request body. Please provide a valid JSON payload.")
    }

    @Test
    fun `should return 400 when request parameter is missing`() {
        webTestClient.get()
            .uri("/api/v1/urls/resolve")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Invalid request: Missing required parameter 'request'. Please provide a valid value.")
    }
}
