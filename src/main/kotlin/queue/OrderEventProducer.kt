package com.example.queue

import com.example.config.RabbitMQConfig
import com.example.domain.models.OrderItemRequest
import com.rabbitmq.client.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class OrderCreatedEvent(
    val orderId: Int,
    val userId: Int,
    val items: List<OrderItemRequest>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class OrderCancelledEvent(
    val orderId: Int,
    val userId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class OrderEventProducer {
    private val exchangeName = "order_events"
    private val routingKey = "order.created"
    private val json = Json { ignoreUnknownKeys = true }

    fun sendOrderCreatedEvent(orderId: Int, userId: Int, items: List<OrderItemRequest>) {
        val event = OrderCreatedEvent(orderId, userId, items)
        val message = json.encodeToString(event)

        RabbitMQConfig.getChannel().use { channel ->
            channel.basicPublish(exchangeName, routingKey, null, message.toByteArray())
        }
    }

    fun sendOrderCancelledEvent(orderId: Int, userId: Int) {
        val event = OrderCancelledEvent(orderId, userId)
        val message = json.encodeToString(event)

        RabbitMQConfig.getChannel().use { channel ->
            channel.basicPublish(exchangeName, "order.cancelled", null, message.toByteArray())
        }
    }
}