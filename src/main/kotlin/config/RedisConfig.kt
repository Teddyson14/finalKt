package com.example.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.ExperimentalLettuceCoroutinesApi

@OptIn(ExperimentalLettuceCoroutinesApi::class)

object RedisConfig {
    private val redisClient: RedisClient by lazy {
        val uri = RedisURI.Builder
            .redis(System.getenv("REDIS_HOST") ?: "localhost")
            .withPort((System.getenv("REDIS_PORT") ?: "6379").toInt())
            .withPassword(System.getenv("REDIS_PASSWORD")?.toCharArray() ?: charArrayOf())
            .build()
        RedisClient.create(uri)
    }

    val connection: RedisCoroutinesCommands<String, String> by lazy {
        redisClient.connect().coroutines()
    }
}