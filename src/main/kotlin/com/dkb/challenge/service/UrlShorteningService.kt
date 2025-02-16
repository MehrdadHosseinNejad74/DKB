package com.dkb.challenge.service

import com.dkb.challenge.exception.CouldNotGenerateUniqueHashException
import com.dkb.challenge.exception.OriginalUrlNotFoundException
import com.dkb.challenge.model.db.UrlMapping
import com.dkb.challenge.model.domain.OriginalUrl
import com.dkb.challenge.model.domain.ShortenedUrl
import com.dkb.challenge.repository.UrlMappingRepository
import com.dkb.challenge.util.toURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.security.MessageDigest

interface UrlShorteningService {
    suspend fun shortenUrl(originalUrl: OriginalUrl): ShortenedUrl
    suspend fun getOriginalUrl(shortenedUrl: ShortenedUrl): OriginalUrl
}

@Service
class UrlShorteningServiceImpl(
    private val repository: UrlMappingRepository,
    @Value("\${shortener.hash-length:6}") private val hashLength: Int,
    @Value("\${shortener.base-url}") private val baseUrl: String,
    @Value("\${shortener.max-retry}") private val maxRetry: Int
) : UrlShorteningService {

    /**
     * Shortens the given [originalUrl].
     * If the URL is already shortened, returns the existing hash.
     * Otherwise, generates a new unique hash, saves it, and returns it.
     */
    @Cacheable("shortenedUrls", key = "#originalUrl.toString()")
    override suspend fun shortenUrl(originalUrl: OriginalUrl): ShortenedUrl {
        repository.findUrlMappingByOriginalUrl(originalUrl.url.toString())?.hash?.let { return ShortenedUrl("$baseUrl/$it".toURI()) }
        val uniqueHash = generateUniqueHash(originalUrl)
        repository.save(UrlMapping(originalUrl = originalUrl.url.toString(), hash = uniqueHash))
        return ShortenedUrl("$baseUrl/$uniqueHash".toURI())
    }

    @Cacheable("originalUrls", key = "#shortenedUrl.toString()")
    override suspend fun getOriginalUrl(shortenedUrl: ShortenedUrl): OriginalUrl {
        val hash = shortenedUrl.url.path.substringAfterLast("/")
        val originalUrlString = repository.findUrlMappingByHash(hash)?.originalUrl
            ?: throw OriginalUrlNotFoundException("Original url for ${shortenedUrl.url} could not be found")
        return OriginalUrl(originalUrlString.toURI())
    }

    suspend fun generateUniqueHash(originalUrl: OriginalUrl): String {
        for (salt in 0..maxRetry) {
            val hash = md5Hash(originalUrl.url.toString() + salt)
            if (repository.findUrlMappingByHash(hash) == null) return hash
        }
        throw CouldNotGenerateUniqueHashException("Could not generate a unique hash for $originalUrl")
    }

    /**
     * Returns the first [hashLength] characters of the MD5 hash.
     * Using 8 characters gives ~4.3 billion unique hashes (2^32).
     */
    suspend fun md5Hash(input: String): String = withContext(Dispatchers.Default) {
        val fullHash = MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
        fullHash.take(hashLength)
    }
}
