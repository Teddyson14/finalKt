package com.example.domain.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val role = enumerationByName("role", 20, com.example.domain.models.UserRole::class)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object Products : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val stock = integer("stock")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object Orders : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val status = enumerationByName("status", 20, com.example.domain.models.OrderStatus::class)
    val totalAmount = decimal("total_amount", 10, 2)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, userId, createdAt)
    }
}

object OrderItems : Table() {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id)
    val productId = integer("product_id").references(Products.id)
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)

    override val primaryKey = PrimaryKey(id)
}

object AuditLogs : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id).nullable()
    val action = varchar("action", 50)
    val entityType = varchar("entity_type", 50)
    val entityId = integer("entity_id").nullable()
    val details = text("details")
    val timestamp = datetime("timestamp")

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, timestamp)
    }
}