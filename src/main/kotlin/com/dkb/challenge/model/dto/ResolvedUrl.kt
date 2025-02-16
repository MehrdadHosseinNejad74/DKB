package com.dkb.challenge.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URI

@Schema(
    description = "Represents the response containing the original URL after resolving a shortened URL.",
    example = """
    {
      "originalUrl": "https://example.com"
    }
    """
)
data class ResolvedUrl(
    @Schema(
        description = "The original URL that corresponds to the shortened URL.",
        example = "https://example.com"
    )
    val originalUrl: URI
)
