package com.example.repositories

import com.example.domain.models.Product
import com.example.domain.models.ProductRequest
import com.example.domain.tables.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class ProductRepository : BaseRepository() {

    suspend fun create(request: ProductRequest): Product {
        val now = LocalDateTime.now()

        return dbQuery {
            val id = Products.insert {
                it[Products.name] = request.name
                it[Products.description] = request.description
                it[Products.price] = request.price
                it[Products.stock] = request.stock
                it[Products.createdAt] = now
                it[Products.updatedAt] = now
            } get Products.id

            findById(id)!!
        }
    }

    suspend fun update(id: Int, request: ProductRequest): Product? {
        dbQuery {
            Products.update({ Products.id eq id }) {
                it[Products.name] = request.name
                it[Products.description] = request.description
                it[Products.price] = request.price
                it[Products.stock] = request.stock
                it[Products.updatedAt] = LocalDateTime.now()
            }
        }

        return findById(id)
    }

    suspend fun delete(id: Int): Boolean {
        return dbQuery {
            Products.deleteWhere { Products.id eq id } > 0
        }
    }

    suspend fun findById(id: Int): Product? {
        return dbQuery {
            Products.select { Products.id eq id }
                .map(::toProduct)
                .singleOrNull()
        }
    }

    suspend fun findAll(limit: Int = 100, offset: Int = 0): List<Product> {
        return dbQuery {
            Products.selectAll()
                .limit(limit, offset.toLong())
                .orderBy(Products.createdAt to SortOrder.DESC)
                .map(::toProduct)
        }
    }

    suspend fun updateStock(id: Int, quantity: Int): Boolean {
        return dbQuery {
            Products.update({ Products.id eq id }) {
                with(SqlExpressionBuilder) {
                    it.update(Products.stock, Products.stock - quantity)
                }
                it[Products.updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun checkStock(id: Int, quantity: Int): Boolean {
        return dbQuery {
            Products.select { Products.id eq id }
                .any { it[Products.stock] >= quantity }
        }
    }

    private fun toProduct(row: ResultRow): Product = Product(
        id = row[Products.id],
        name = row[Products.name],
        description = row[Products.description],
        price = row[Products.price],
        stock = row[Products.stock],
        createdAt = row[Products.createdAt],
        updatedAt = row[Products.updatedAt]
    )
}