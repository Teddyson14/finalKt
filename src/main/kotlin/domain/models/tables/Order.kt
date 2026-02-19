package com.example.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class Order(
    val id: Int,
    val userId: Int,
    val status: OrderStatus,
    @Contextual
    val totalAmount: BigDecimal,
    @Contextual
    val createdAt: LocalDateTime,
    @Contextual
    val updatedAt: LocalDateTime
)

@Serializable
data class OrderRequest(
    val items: List<OrderItemRequest>
)

@Serializable
data class OrderItemRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class OrderResponse(
    val id: Int,
    val userId: Int,
    val status: String,
    @Contextual
    val totalAmount: BigDecimal,
    val items: List<OrderItemResponse>,
    val createdAt: String
)

@Serializable
data class OrderItemResponse(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    @Contextual
    val price: BigDecimal
)

enum class OrderStatus {
    PENDING, PROCESSING, COMPLETED, CANCELLED
}