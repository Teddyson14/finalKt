package com.example.config

import com.rabbitmq.client.ConnectionFactory

object RabbitMQConfig {
    private const val EXCHANGE_NAME = "order_events"
    private const val QUEUE_NAME = "order_notifications"
    private const val ROUTING_KEY = "order.created"

    val connection by lazy {
        val factory = ConnectionFactory().apply {
            host = System.getenv("RABBITMQ_HOST") ?: "localhost"
            port = (System.getenv("RABBITMQ_PORT") ?: "5672").toInt()
            username = System.getenv("RABBITMQ_USER") ?: "guest"
            password = System.getenv("RABBITMQ_PASSWORD") ?: "guest"
        }
        factory.newConnection()
    }

    fun initialize() {
        connection.createChannel().use { channel ->
            channel.exchangeDeclare(EXCHANGE_NAME, "topic", true)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null)
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY)
        }
    }

    fun getChannel() = connection.createChannel()
}