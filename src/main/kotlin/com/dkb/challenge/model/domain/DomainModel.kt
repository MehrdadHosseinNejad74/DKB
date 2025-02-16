package com.dkb.challenge.model.domain

import java.net.URI

data class OriginalUrl(val url: URI) {
    constructor(url: String) : this(URI(url))
    override fun toString(): String = url.toString()
}

data class ShortenedUrl(val url: URI) {
    constructor(url: String) : this(URI(url))
    override fun toString(): String = url.toString()
}
