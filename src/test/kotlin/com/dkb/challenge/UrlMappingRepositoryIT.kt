package com.dkb.challenge

import com.dkb.challenge.model.db.UrlMapping
import com.dkb.challenge.repository.UrlMappingRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import kotlin.test.Test

@DataMongoTest
@MongoContainer
class UrlMappingRepositoryIT @Autowired constructor(
    private val repository: UrlMappingRepository
) {

    private val originalUrl = "https://example.com"
    private val hash = "abc123"

    @BeforeEach
    fun clean() = runBlocking {
        repository.deleteAll()
    }

    @Test
    fun `should insert URL mapping`() = runBlocking {
        val urlMapping = UrlMapping(originalUrl = originalUrl, hash = hash)
        val saved = repository.save(urlMapping)

        assertNotNull(saved)
        assertEquals(originalUrl, saved.originalUrl)
        assertEquals(hash, saved.hash)
    }

    @Test
    fun `should retrieve URL mapping by hash`() = runBlocking {
        // First insert the URL mapping
        val urlMapping = UrlMapping(originalUrl = originalUrl, hash = hash)
        repository.save(urlMapping)

        val retrieved = repository.findUrlMappingByHash(hash)

        assertNotNull(retrieved)
        assertEquals(originalUrl, retrieved?.originalUrl)
        assertEquals(hash, retrieved?.hash)
    }

    @Test
    fun `should retrieve URL mapping by original URL`() = runBlocking {
        // Arrange - First insert the URL mapping
        val urlMapping = UrlMapping(originalUrl = originalUrl, hash = hash)
        repository.save(urlMapping)

        val retrieved = repository.findUrlMappingByOriginalUrl(originalUrl)

        assertNotNull(retrieved)
        assertEquals(originalUrl, retrieved?.originalUrl)
        assertEquals(hash, retrieved?.hash)
    }
}
