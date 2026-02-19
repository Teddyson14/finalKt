package com.example.integration

import com.example.config.DatabaseConfig
import com.example.domain.models.ProductRequest
import com.example.repositories.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import kotlin.test.*

class ProductRepositoryTest {

    private lateinit var postgres: PostgreSQLContainer<Nothing>
    private lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test")
            start()
        }


        System.setProperty("DATABASE_URL", postgres.jdbcUrl)
        System.setProperty("DB_USERNAME", postgres.username)
        System.setProperty("DB_PASSWORD", postgres.password)


        DatabaseConfig.init()
        productRepository = ProductRepository()
    }

    @After
    fun tearDown() {
        postgres.stop()
    }

    @Test
    fun `test create and find product`() = runBlocking {
        val request = ProductRequest(
            name = "Test Product",
            description = "Test Description",
            price = BigDecimal("99.99"),
            stock = 10
        )

        val created = productRepository.create(request)


        val found = productRepository.findById(created.id)

        assertNotNull(found)
        assertEquals("Test Product", found.name)
        assertEquals(BigDecimal("99.99"), found.price)
        assertEquals(10, found.stock)
    }

    @Test
    fun `test update product stock`() = runBlocking {
        // Create product
        val request = ProductRequest(
            name = "Stock Test",
            description = "Test",
            price = BigDecimal("50.00"),
            stock = 20
        )

        val created = productRepository.create(request)


        val updated = productRepository.updateStock(created.id, 5)
        assertTrue(updated)


        val product = productRepository.findById(created.id)!!
        assertEquals(15, product.stock)
    }
}