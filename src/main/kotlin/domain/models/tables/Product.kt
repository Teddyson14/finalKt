package com.example.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val description: String,
    @Contextual
    val price: BigDecimal,
    val stock: Int,
    @Contextual
    val createdAt: LocalDateTime,
    @Contextual
    val updatedAt: LocalDateTime
)

@Serializable
data class ProductRequest(
    val name: String,
    val description: String,
    @Contextual
    val price: BigDecimal,
    val stock: Int
)

@Serializable
data class ProductResponse(
    val id: Int,
    val name: String,
    val description: String,
    @Contextual
    val price: BigDecimal,
    val stock: Int
)