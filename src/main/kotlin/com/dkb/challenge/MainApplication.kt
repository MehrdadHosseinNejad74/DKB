package com.dkb.challenge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class ChallengeApplication

fun main(args: Array<String>) {
    runApplication<ChallengeApplication>(*args)
}
