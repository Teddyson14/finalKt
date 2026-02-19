package com.example.cache

import com.example.config.RedisConfig
import com.example.domain.models.Product
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

import io.lettuce.core.ExperimentalLettuceCoroutinesApi

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class ProductCache {
    private val redis = RedisConfig.connection
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val TTL_SECONDS = 3600L // 1 hour

    suspend fun get(id: Int): Product? {
        val key = "product:$id"
        val cached = redis.get(key)

        return if (cached != null) {
            try {
                json.decodeFromString<Product>(cached)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun set(id: Int, product: Product) {
        val key = "product:$id"
        val value = json.encodeToString(product)
        redis.setex(key, TTL_SECONDS, value)
    }

    suspend fun clear(id: Int? = null) {
        if (id != null) {
            redis.del("product:$id")
        } else {

        }
    }
}