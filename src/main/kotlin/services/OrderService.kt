package com.example.services

import com.example.domain.models.*
import com.example.queue.OrderEventProducer
import com.example.repositories.OrderRepository
import com.example.repositories.ProductRepository
import com.example.repositories.AuditRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.runBlocking
import com.example.domain.models.tables.OrderItem

class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val auditRepository: AuditRepository,
    private val productService: ProductService,
    private val eventProducer: OrderEventProducer
) {

    suspend fun createOrder(userId: Int, request: OrderRequest): OrderResponse {

        for (item in request.items) {
            if (!productService.checkAndUpdateStock(item.productId, item.quantity)) {
                val product = productRepository.findById(item.productId)
                throw IllegalStateException("Insufficient stock for product: ${product?.name ?: item.productId}")
            }
        }


        val order = orderRepository.create(userId, request.items)

        // Аудит
        auditRepository.log(
            userId = userId,
            action = "CREATE_ORDER",
            entityType = "ORDER",
            entityId = order.id,
            details = "Created order with ${request.items.size} items"
        )

        // Отправка в очередь
        eventProducer.sendOrderCreatedEvent(order.id, userId, request.items)

        // Получение заказа с товарами
        val (fullOrder, items) = orderRepository.getOrderWithItems(order.id)!!

        return fullOrder.toResponse(items)
    }




    suspend fun getUserOrders(userId: Int): List<OrderResponse> {
        val orders = orderRepository.findByUser(userId)

        return orders.map { order ->
            val (_, items) = orderRepository.getOrderWithItems(order.id)!!
            order.toResponse(items)
        }
    }

    suspend fun getAllOrders(status: OrderStatus? = null): List<OrderResponse> {
        val orders = orderRepository.findAll(status)

        return orders.map { order ->
            val (_, items) = orderRepository.getOrderWithItems(order.id)!!
            order.toResponse(items)
        }
    }

    suspend fun cancelOrder(orderId: Int, userId: Int): Boolean {
        val result = orderRepository.cancel(orderId, userId)

        if (result) {
            // Audit log
            auditRepository.log(
                userId = userId,
                action = "CANCEL_ORDER",
                entityType = "ORDER",
                entityId = orderId,
                details = "Cancelled order"
            )

            // Send event
            eventProducer.sendOrderCancelledEvent(orderId, userId)
        }

        return result
    }

    suspend fun getOrderStats(): Map<String, Any> {
        return orderRepository.getOrderStats()
    }

    private suspend fun Order.toResponse(items: List<OrderItem>): OrderResponse {
        val itemResponses = items.map { item ->
            val product = productRepository.findById(item.productId)!!
            OrderItemResponse(
                productId = item.productId,
                productName = product.name,
                quantity = item.quantity,
                price = item.price
            )
        }

        return OrderResponse(
            id = id,
            userId = userId,
            status = status.name,
            totalAmount = totalAmount,
            items = itemResponses,
            createdAt = createdAt.toString()
        )
    }
}