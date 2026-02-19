package com.example

import com.example.config.*
import com.example.controllers.*
import com.example.queue.OrderEventConsumer
import com.example.queue.OrderEventProducer
import com.example.repositories.*
import com.example.services.*
import com.example.cache.ProductCache
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import kotlinx.coroutines.*

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {

    DatabaseConfig.init()

    try {
        RabbitMQConfig.initialize()
    } catch (e: Exception) {
        log.error("Failed to initialize RabbitMQ: ${e.message}")
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Internal server error",
                "message" to (if (System.getenv("ENV") == "development") cause.message else null)
            ))
        }
    }


    configureSecurity()


    val userRepository = UserRepository()
    val productRepository = ProductRepository()
    val orderRepository = OrderRepository()
    val auditRepository = AuditRepository()


    val productCache = ProductCache()

    // Initialize queue
    val eventProducer = OrderEventProducer()


    val authService = AuthService(userRepository, auditRepository)
    val productService = ProductService(productRepository, auditRepository, productCache)
    val orderService = OrderService(
        orderRepository,
        productRepository,
        auditRepository,
        productService,
        eventProducer
    )
    val auditService = AuditService(auditRepository)

    // Start consumer in background
    val eventConsumer = OrderEventConsumer(auditService)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            eventConsumer.startConsuming()
        } catch (e: Exception) {
            log.error("Failed to start consumer: ${e.message}")
        }
    }


    routing {
        // Swagger documentation
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        openAPI(path = "openapi/documentation.yaml", swaggerFile = "openapi/documentation.yaml")


        authRoutes(authService)
        productRoutes(productService)
        orderRoutes(orderService)
        adminRoutes(orderService, auditService)


        get("/health") {
            call.respond(mapOf(
                "status" to "OK",
                "service" to "Ktor E-Commerce API",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }
}