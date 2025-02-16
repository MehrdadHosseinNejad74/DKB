package com.dkb.challenge.model.db

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "url_mappings")
data class UrlMapping(
    @Id
    val id: String? = null,
    val originalUrl: String,
    val hash: String,
    @CreatedDate
    val createdAt: Instant = Instant.now()
)
