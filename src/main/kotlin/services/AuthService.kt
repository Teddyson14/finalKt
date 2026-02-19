package com.example.services

import com.example.config.JwtConfig
import com.example.domain.models.UserResponse
import com.example.domain.models.UserRole
import com.example.repositories.UserRepository
import com.example.repositories.AuditRepository

class AuthService(
    private val userRepository: UserRepository,
    private val auditRepository: AuditRepository
) {

    suspend fun register(username: String, email: String, password: String): Pair<UserResponse, String> {

        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("Username already taken")
        }

        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email already registered")
        }


        val allUsers = userRepository.findAll()
        val role = if (allUsers.isEmpty()) UserRole.ADMIN else UserRole.USER


        val user = userRepository.create(username, email, password, role)


        val token = JwtConfig.generateToken(user)


        auditRepository.log(
            userId = user.id,
            action = "REGISTER",
            entityType = "USER",
            entityId = user.id,
            details = "User registered successfully"
        )

        return UserResponse(user.id, user.username, user.email, user.role.name) to token
    }

    suspend fun login(username: String, password: String): Pair<UserResponse, String>? {
        val user = userRepository.authenticate(username, password) ?: return null

        // Generate token
        val token = JwtConfig.generateToken(user)


        auditRepository.log(
            userId = user.id,
            action = "LOGIN",
            entityType = "USER",
            entityId = user.id,
            details = "User logged in"
        )

        return UserResponse(user.id, user.username, user.email, user.role.name) to token
    }
}