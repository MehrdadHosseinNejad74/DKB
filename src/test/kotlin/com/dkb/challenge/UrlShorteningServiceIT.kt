package com.dkb.challenge

import com.dkb.challenge.exception.OriginalUrlNotFoundException
import com.dkb.challenge.model.domain.OriginalUrl
import com.dkb.challenge.model.domain.ShortenedUrl
import com.dkb.challenge.repository.UrlMappingRepository
import com.dkb.challenge.service.UrlShorteningServiceImpl
import com.ninjasquad.springmockk.SpykBean
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager

@SpringBootTest
@MongoContainer
class UrlShorteningServiceIT @Autowired constructor(
    private val repository: UrlMappingRepository,
    @Value("\${shortener.base-url}") private val baseUrl: String,
    private val cacheManager: CacheManager
) {

    @SpykBean
    private lateinit var service: UrlShorteningServiceImpl

    @BeforeEach
    fun clean(): Unit = runBlocking {
        repository.deleteAll()
        clearAllMocks()
        cacheManager.getCache("shortenedUrls")?.clear()
        cacheManager.getCache("originalUrls")?.clear()
    }

    @Test
    fun `should shorten the url`(): Unit = runBlocking {
        coEvery { service.generateUniqueHash((any())) } returns "abc123"
        val originalUrl = OriginalUrl("https://example.com")
        val shortenedUrl = service.shortenUrl(originalUrl)
        assertThat(shortenedUrl).isEqualTo(ShortenedUrl("$baseUrl/abc123"))
    }

    @Test
    fun `should return the same shortened url for the same original URL from the db and not hash it again`() =
        runBlocking {
            val url = OriginalUrl("https://example.com")
            val short1 = service.shortenUrl(url)
            val short2 = service.shortenUrl(url)
            coVerify(exactly = 1) { service.generateUniqueHash(url) }
            assertEquals(short1, short2)
        }

    @Test
    fun `should generate different shortened urls for different original URLs`() = runBlocking {
        val url1 = OriginalUrl("https://example.com")
        val url2 = OriginalUrl("https://google.com")
        val short1 = service.shortenUrl(url1)
        val short2 = service.shortenUrl(url2)

        assertNotEquals(short1, short2)
    }

    @Test
    fun `given a shortened url, should return the original URL`() = runBlocking {
        // Create a shortened URL
        val originalUrl = OriginalUrl("https://example.com")
        val shortenedUrl = service.shortenUrl(originalUrl)

        // Resolve the original URL
        val fetchedOriginalUrl = service.getOriginalUrl(shortenedUrl)

        // Verify repository was called correctly
        assertEquals(fetchedOriginalUrl, originalUrl)
    }

    @Test
    fun `should throws NotFound exception when shortened url does not exist`(): Unit = runBlocking {
        val shortenedUrl = ShortenedUrl("http://notInDb.com/1212")
        assertThrows<OriginalUrlNotFoundException> {
            service.getOriginalUrl(shortenedUrl)
        }
    }

    @Test
    fun `should cache shortened URL`() = runBlocking {
        val originalUrl = OriginalUrl("https://example.com")

        // First call - should store in Redis
        val shortenedUrl = service.shortenUrl(originalUrl)

        // Fetch from cache
        val cachedShortUrl =
            cacheManager.getCache("shortenedUrls")?.get(originalUrl.toString(), ShortenedUrl::class.java)!!
        assertEquals(shortenedUrl, cachedShortUrl)
    }

    @Test
    fun `should return same shortened URL from cache without hitting DB`() = runBlocking {
        val originalUrl = OriginalUrl("https://example.com")

        val firstShortUrl = service.shortenUrl(originalUrl)
        val secondShortUrl = service.shortenUrl(originalUrl)
        coVerify(exactly = 1) { service.generateUniqueHash(originalUrl) }
        assertEquals(firstShortUrl, secondShortUrl)
    }

    @Test
    fun `should return same original URL from cache without hitting DB`() = runBlocking {
        val shortenedUrl = service.shortenUrl(OriginalUrl("https://example.com"))

        val firstOriginalUrl = service.getOriginalUrl(shortenedUrl)
        // There is no recode in the db
        repository.deleteAll()

        // Should not hit the db
        val secondOriginalUrl = service.getOriginalUrl(shortenedUrl)
        assertEquals(firstOriginalUrl, secondOriginalUrl)
    }
}
