package com.example.queue

import com.example.config.RabbitMQConfig
import com.example.services.AuditService
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory

class OrderEventConsumer(
    private val auditService: AuditService
) {
    private val logger = LoggerFactory.getLogger(OrderEventConsumer::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val queueName = "order_notifications"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startConsuming() {
        val channel = RabbitMQConfig.getChannel()

        val deliverCallback = DeliverCallback { _, delivery ->
            val message = String(delivery.body, Charsets.UTF_8)

            try {
                when (delivery.envelope.routingKey) {
                    "order.created" -> {
                        scope.launch {
                            handleOrderCreated(message)
                        }
                    }
                    "order.cancelled" -> {
                        scope.launch {
                            handleOrderCancelled(message)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing message: ${e.message}", e)
            } finally {
                channel.basicAck(delivery.envelope.deliveryTag, false)
            }
        }

        channel.basicConsume(queueName, false, deliverCallback) { consumerTag ->
            logger.info("Consumer $consumerTag cancelled")
        }

        logger.info("Started consuming messages from $queueName")
    }

    private suspend fun handleOrderCreated(message: String) {
        val event = json.decodeFromString<OrderCreatedEvent>(message)

        logger.info("Processing order created event: Order ID ${event.orderId}, User ID ${event.userId}")


        sendEmail(event.userId, "Order Confirmation", "Your order #${event.orderId} has been created")


        auditService.log(
            userId = event.userId,
            action = "ORDER_EVENT_PROCESSED",
            entityType = "ORDER",
            entityId = event.orderId,
            details = "Order created event processed via RabbitMQ"
        )
    }

    private suspend fun handleOrderCancelled(message: String) {
        val event = json.decodeFromString<OrderCancelledEvent>(message)

        logger.info("Processing order cancelled event: Order ID ${event.orderId}, User ID ${event.userId}")


        sendEmail(event.userId, "Order Cancelled", "Your order #${event.orderId} has been cancelled")


        auditService.log(
            userId = event.userId,
            action = "ORDER_EVENT_PROCESSED",
            entityType = "ORDER",
            entityId = event.orderId,
            details = "Order cancelled event processed via RabbitMQ"
        )
    }

    private suspend fun sendEmail(userId: Int, subject: String, content: String) {

        delay(100)
        logger.info("Email sent to user $userId: $subject - $content")
    }
}