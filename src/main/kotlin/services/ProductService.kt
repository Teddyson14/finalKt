package com.example.services

import com.example.cache.ProductCache
import com.example.domain.models.Product
import com.example.domain.models.ProductRequest
import com.example.domain.models.ProductResponse
import com.example.repositories.ProductRepository
import com.example.repositories.AuditRepository

class ProductService(
    private val productRepository: ProductRepository,
    private val auditRepository: AuditRepository,
    private val productCache: ProductCache
) {

    suspend fun createProduct(request: ProductRequest, userId: Int): ProductResponse {
        val product = productRepository.create(request)


        productCache.clear()


        auditRepository.log(
            userId = userId,
            action = "CREATE_PRODUCT",
            entityType = "PRODUCT",
            entityId = product.id,
            details = "Created product: ${product.name}"
        )

        return product.toResponse()
    }

    suspend fun updateProduct(id: Int, request: ProductRequest, userId: Int): ProductResponse? {
        val product = productRepository.update(id, request) ?: return null


        productCache.clear(id)


        auditRepository.log(
            userId = userId,
            action = "UPDATE_PRODUCT",
            entityType = "PRODUCT",
            entityId = id,
            details = "Updated product: ${product.name}"
        )

        return product.toResponse()
    }

    suspend fun deleteProduct(id: Int, userId: Int): Boolean {
        val result = productRepository.delete(id)

        if (result) {

            productCache.clear(id)


            auditRepository.log(
                userId = userId,
                action = "DELETE_PRODUCT",
                entityType = "PRODUCT",
                entityId = id,
                details = "Deleted product ID: $id"
            )
        }

        return result
    }

    suspend fun getProduct(id: Int): ProductResponse? {

        var product = productCache.get(id)

        if (product == null) {

            product = productRepository.findById(id)


            if (product != null) {
                productCache.set(id, product)
            }
        }

        return product?.toResponse()
    }

    suspend fun getAllProducts(limit: Int = 100, offset: Int = 0): List<ProductResponse> {
        return productRepository.findAll(limit, offset).map { it.toResponse() }
    }

    suspend fun checkAndUpdateStock(productId: Int, quantity: Int): Boolean {
        return if (productRepository.checkStock(productId, quantity)) {
            productRepository.updateStock(productId, quantity)
            productCache.clear(productId) // Clear cache after stock update
            true
        } else {
            false
        }
    }

    private fun Product.toResponse(): ProductResponse = ProductResponse(
        id = id,
        name = name,
        description = description,
        price = price,
        stock = stock
    )
}