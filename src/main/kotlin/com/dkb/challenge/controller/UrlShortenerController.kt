package com.dkb.challenge.controller

import com.dkb.challenge.model.domain.OriginalUrl
import com.dkb.challenge.model.domain.ShortenedUrl
import com.dkb.challenge.model.dto.ResolvedUrl
import com.dkb.challenge.model.dto.ShortenRequest
import com.dkb.challenge.model.dto.ShortenResponse
import com.dkb.challenge.service.UrlShorteningService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/urls")
@Validated
@Tag(name = "URL Shortener", description = "API for shortening and resolving URLs.")
class UrlShortenerController(private val urlShorteningService: UrlShorteningService) {

    @PostMapping("/shorten")
    @Operation(
        summary = "Shorten a URL",
        description = "Takes a long URL and returns a shortened version."
    )
    suspend fun shortenUrl(
        @RequestBody @Valid
        request: ShortenRequest
    ): ShortenResponse {
        val shortedUrl = urlShorteningService.shortenUrl(OriginalUrl(request.url))
        return ShortenResponse(shortedUrl.url)
    }

    @GetMapping("/resolve")
    @Operation(
        summary = "Resolve a shortened URL",
        description = "Takes a shortened URL and returns the original URL."
    )
    suspend fun resolveUrl(
        @RequestParam
        @Pattern(
            regexp = "^(https?)://.+",
            message = "Invalid URL format. Please provide a valid HTTP or HTTPS URL."
        )
        request: String
    ): ResolvedUrl {
        val originalUrl = urlShorteningService.getOriginalUrl(ShortenedUrl(request))
        return ResolvedUrl(originalUrl.url)
    }
}
