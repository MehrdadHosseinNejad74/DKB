package com.dkb.challenge

import com.dkb.challenge.exception.CouldNotGenerateUniqueHashException
import com.dkb.challenge.model.domain.OriginalUrl
import com.dkb.challenge.repository.UrlMappingRepository
import com.dkb.challenge.service.UrlShorteningServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class UrlShorteningServiceTest(
    @MockK(relaxed = true)
    private val repository: UrlMappingRepository
) {

    private val hashLength = 6
    private val maxRetry = 4
    private val baseUrl = "https://short.ly"
    private val service = UrlShorteningServiceImpl(repository, hashLength, baseUrl, maxRetry)

    @Test
    fun `should return an MD5 hash of correct length`() = runBlocking {
        val input = "https://example.com"
        val hash = service.md5Hash(input)
        assertEquals(hashLength, hash.length)
    }

    @Test
    fun `should generate different hashes for different inputs`() = runBlocking {
        val hash1 = service.md5Hash("https://example.com")
        val hash2 = service.md5Hash("https://google.com")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `should generate a hash without salt if no collision exists`(): Unit = runBlocking {
        val url = OriginalUrl(URI("https://example.com"))
        coEvery { repository.findUrlMappingByHash(any()) } returns null
        // Spy on md5Hash and capture the salt values used
        val service = spyk(service)
        val urlToBeHashed = slot<String>()
        coEvery { service.md5Hash(capture(urlToBeHashed)) } coAnswers { callOriginal() }

        service.generateUniqueHash(url)
        // Verify that the md5Hash called only once and with salt 0
        coVerify(exactly = 1) { service.md5Hash(any()) }
        assertThat(urlToBeHashed.captured).endsWith("0")
    }

    @Test
    fun `should retry with next salt when hash collision occurs`(): Unit = runBlocking {
        val url = OriginalUrl(URI("https://example.com"))
        val service = spyk(service)
        coEvery { service.md5Hash(match { it.endsWith("0") }) } returns "0"
        coEvery { service.md5Hash(match { it.endsWith("1") }) } returns "1"
        coEvery { service.md5Hash(match { it.endsWith("2") }) } returns "2"

        // Simulate first two attempts as collisions, third attempt as unique
        coEvery { repository.findUrlMappingByHash("0") } returns mockk()
        coEvery { repository.findUrlMappingByHash("1") } returns mockk()
        coEvery { repository.findUrlMappingByHash("2") } returns null // Unique hash

        service.generateUniqueHash(url)
        coVerify(exactly = 3) { service.md5Hash(any()) }
    }

    @Test
    fun `should generate different hashes for different URLs`() = runBlocking {
        val url1 = OriginalUrl(URI("https://example.com"))
        val url2 = OriginalUrl(URI("https://another.com"))

        coEvery { repository.findUrlMappingByHash(any()) } returns null

        val hash1 = service.generateUniqueHash(url1)
        val hash2 = service.generateUniqueHash(url2)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `should throw exception when max retry attempts are exceeded`(): Unit = runBlocking {
        val url = OriginalUrl(URI("https://example.com"))

        // Simulate a collision for every generated hash up to maxRetry
        coEvery { repository.findUrlMappingByHash(any()) } returns mockk()

        // Assert that the exception is thrown
        assertThrows(CouldNotGenerateUniqueHashException::class.java) {
            runBlocking { service.generateUniqueHash(url) }
        }
    }
}
