package com.dkb.challenge.repository

import com.dkb.challenge.model.db.UrlMapping
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UrlMappingRepository : CoroutineCrudRepository<UrlMapping, String> {
    suspend fun findUrlMappingByHash(hash: String): UrlMapping?
    suspend fun findUrlMappingByOriginalUrl(originalUrl: String): UrlMapping?
}
