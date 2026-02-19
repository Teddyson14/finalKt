package com.example.controllers

import com.example.domain.models.OrderRequest
import com.example.services.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.*

fun Route.orderRoutes(orderService: OrderService) {
    route("/orders") {
        authenticate("auth-jwt") {

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))

                val request = try {
                    call.receive<OrderRequest>()
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }

                if (request.items.isEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Order must contain at least one item"))
                }

                try {
                    val order = orderService.createOrder(userId, request)
                    call.respond(HttpStatusCode.Created, order)
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create order"))
                }
            }


            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))

                val orders = orderService.getUserOrders(userId)
                call.respond(orders)
            }


            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", Int::class)
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))

                val orderId = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

                val cancelled = orderService.cancelOrder(orderId, userId)

                if (cancelled) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Order not found"))
                }
            }
        }
    }
}