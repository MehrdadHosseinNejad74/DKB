package com.dkb.challenge

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Testcontainers
@ContextConfiguration(initializers = [MongoInitializer::class])
annotation class MongoContainer

class MongoInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.data.mongodb.uri=${MongoTestContainer.instance.replicaSetUrl}"
        ).applyTo(applicationContext.environment)
    }
}

object MongoTestContainer {
    val instance: MongoDBContainer by lazy {
        MongoDBContainer("mongo:latest").apply {
            withExposedPorts(27017)
            start()
        }
    }
}
