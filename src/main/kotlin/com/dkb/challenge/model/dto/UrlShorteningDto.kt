package com.dkb.challenge.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.net.URI

@Schema(
    description = "Represents a request to shorten a URL.",
    example = """
    {
      "url": "https://example.com"
    }
    """
)
data class ShortenRequest(
    @Schema(
        description = "The URL to be shortened. Must be a valid HTTP or HTTPS URL.",
        example = "https://example.com"
    )
    @field:Pattern(
        regexp = "^(https?)://.+",
        message = "Invalid URL format. Please provide a valid HTTP or HTTPS URL."
    )
    val url: String
)

@Schema(
    description = "Represents the response containing the shortened URL.",
    example = """
    {
      "shortenedUrl": "https://short.ly/abc123"
    }
    """
)
data class ShortenResponse(
    @Schema(
        description = "The shortened URL that maps to the original URL.",
        example = "https://short.ly/abc123"
    )
    val shortenedUrl: URI
)
