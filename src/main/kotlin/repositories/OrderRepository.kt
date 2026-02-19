package com.example.repositories

import com.example.domain.models.*
import com.example.domain.tables.OrderItems
import com.example.domain.tables.Orders
import com.example.domain.tables.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.math.BigDecimal
import java.time.LocalDateTime
import com.example.domain.models.tables.OrderItem

class OrderRepository : BaseRepository() {

    suspend fun create(userId: Int, items: List<OrderItemRequest>): Order {
        return dbQuery {

            val totalAmount = calculateTotal(items)


            val now = LocalDateTime.now()
            val orderId = Orders.insert {
                it[Orders.userId] = userId
                it[Orders.status] = OrderStatus.PENDING
                it[Orders.totalAmount] = totalAmount
                it[Orders.createdAt] = now
                it[Orders.updatedAt] = now
            } get Orders.id

            // Create order items
            items.forEach { item ->
                val product = Products.select { Products.id eq item.productId }.single()
                val price = product[Products.price]

                OrderItems.insert {
                    it[OrderItems.orderId] = orderId
                    it[OrderItems.productId] = item.productId
                    it[OrderItems.quantity] = item.quantity
                    it[OrderItems.price] = price
                }
            }

            findById(orderId)!!
        }
    }

    suspend fun findById(id: Int): Order? {
        return dbQuery {
            Orders.select { Orders.id eq id }
                .map(::toOrder)
                .singleOrNull()
        }
    }

    suspend fun findByUser(userId: Int): List<Order> {
        return dbQuery {
            Orders.select { Orders.userId eq userId }
                .orderBy(Orders.createdAt to SortOrder.DESC)
                .map(::toOrder)
        }
    }

    suspend fun findAll(status: OrderStatus? = null): List<Order> {
        return dbQuery {
            val query = if (status != null) {
                Orders.select { Orders.status eq status }
            } else {
                Orders.selectAll()
            }
            query.orderBy(Orders.createdAt to SortOrder.DESC)
                .map(::toOrder)
        }
    }

    suspend fun updateStatus(id: Int, status: OrderStatus): Boolean {
        return dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[Orders.status] = status
                it[Orders.updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun cancel(id: Int, userId: Int): Boolean {
        return dbQuery {
            Orders.update({ Orders.id eq id and (Orders.userId eq userId) }) {
                it[Orders.status] = OrderStatus.CANCELLED
                it[Orders.updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun getOrderWithItems(orderId: Int): Pair<Order, List<OrderItem>>? {
        return dbQuery {
            val order = Orders.select { Orders.id eq orderId }.singleOrNull() ?: return@dbQuery null

            val items = OrderItems.select { OrderItems.orderId eq orderId }
                .map { row ->
                    OrderItem(
                        id = row[OrderItems.id],
                        orderId = row[OrderItems.orderId],
                        productId = row[OrderItems.productId],
                        quantity = row[OrderItems.quantity],
                        price = row[OrderItems.price]
                    )
                }

            toOrder(order) to items
        }
    }

    suspend fun getOrderStats(): Map<String, Any> {
        return dbQuery {
            val totalOrders = Orders.selectAll().count()
            val totalRevenue = Orders.slice(Orders.totalAmount.sum())
                .selectAll()
                .map { it[Orders.totalAmount.sum()] }
                .firstOrNull() ?: BigDecimal.ZERO

            val ordersByStatus = OrderStatus.values().associate { status ->
                val count = Orders.select { Orders.status eq status }.count()
                status.name to count
            }

            mapOf(
                "totalOrders" to totalOrders,
                "totalRevenue" to totalRevenue,
                "ordersByStatus" to ordersByStatus
            )
        }
    }

    private suspend fun calculateTotal(items: List<OrderItemRequest>): BigDecimal {
        var total = BigDecimal.ZERO
        for (item in items) {
            val product = Products.select { Products.id eq item.productId }.single()
            total += product[Products.price].multiply(BigDecimal(item.quantity))
        }
        return total
    }

    private fun toOrder(row: ResultRow): Order = Order(
        id = row[Orders.id],
        userId = row[Orders.userId],
        status = row[Orders.status],
        totalAmount = row[Orders.totalAmount],
        createdAt = row[Orders.createdAt],
        updatedAt = row[Orders.updatedAt]
    )
}