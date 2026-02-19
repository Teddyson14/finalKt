package com.example.controllers

import com.example.domain.models.AuthRequest
import com.example.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.domain.models.*
import io.ktor.server.auth.*





fun Route.authRoutes(authService: AuthService) {
    route("/auth") {

        post("/register") {
            val request = try {
                call.receive<AuthRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                return@post
            }

            try {
                val (user, token) = authService.register(
                    username = request.username,
                    email = "${request.username}@example.com",
                    password = request.password
                )

                call.respond(HttpStatusCode.Created, mapOf(
                    "user" to user,
                    "token" to token
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Registration failed"))
            }
        }


        post("/login") {
            val request = try {
                call.receive<AuthRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                return@post
            }

            val result = authService.login(request.username, request.password)

            if (result != null) {
                val (user, token) = result
                call.respond(mapOf(
                    "user" to user,
                    "token" to token
                ))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }
    }
}