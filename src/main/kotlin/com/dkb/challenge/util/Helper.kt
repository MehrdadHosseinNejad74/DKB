package com.dkb.challenge.util

import java.net.URI

fun String.toURI(): URI = URI.create(this)
