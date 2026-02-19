package com.example.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    @Contextual
    val createdAt: LocalDateTime,
    @Contextual
    val updatedAt: LocalDateTime
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String
)

enum class UserRole {
    USER, ADMIN
}