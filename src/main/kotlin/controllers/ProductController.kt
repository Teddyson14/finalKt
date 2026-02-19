package com.example.controllers

import com.example.domain.models.ProductRequest
import com.example.services.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.*
import com.example.middleware.AuthMiddleware.requireRole

fun Route.productRoutes(productService: ProductService) {
    route("/products") {

        get {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val products = productService.getAllProducts(limit, offset)
            call.respond(products)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

            val product = productService.getProduct(id)
            if (product != null) {
                call.respond(product)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
            }
        }


        authenticate("auth-jwt") {
            requireRole("ADMIN")

            post {
                val request = try {
                    call.receive<ProductRequest>()
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class) ?: 0

                try {
                    val product = productService.createProduct(request, userId)
                    call.respond(HttpStatusCode.Created, product)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

                val request = try {
                    call.receive<ProductRequest>()
                } catch (e: Exception) {
                    return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))

                val product = productService.updateProduct(id, request, userId)
                if (product != null) {
                    call.respond(product)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))

                val deleted = productService.deleteProduct(id, userId)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
                }
            }
        }
    }
}