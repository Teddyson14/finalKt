package com.example.middleware

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.util.*

object AuthMiddleware {
    fun install(application: Application) {
        application.intercept(ApplicationCallPipeline.Features) {
            val principal = call.principal<JWTPrincipal>()


            if (principal != null) {
                val userId = principal.payload.getClaim("userId").asInt()
                val username = principal.payload.getClaim("username").asString()
                val role = principal.payload.getClaim("role").asString()

                call.attributes.put(AttributeKey("userId"), userId)
                call.attributes.put(AttributeKey("username"), username)
                call.attributes.put(AttributeKey("role"), role)
            }

            proceed()
        }
    }

    fun Route.requireRole(vararg roles: String) {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val userRole = principal?.payload?.getClaim("role")?.asString()
            if (userRole == null || !roles.contains(userRole)) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Insufficient permissions"))
                finish()
            }
        }
    }
}