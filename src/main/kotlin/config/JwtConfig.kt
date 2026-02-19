package com.example.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.models.User
import java.util.*

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "your-secret-key-change-in-production"
    private const val issuer = "ktor-ecommerce"
    private const val validityInMs = 24 * 60 * 60 * 1000 // 24 hours

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(user: User): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", user.id)
            .withClaim("username", user.username)
            .withClaim("role", user.role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }
}