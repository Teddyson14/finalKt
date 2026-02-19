package com.example.services

import com.example.domain.models.AuditLog
import com.example.repositories.AuditRepository

class AuditService(
    private val auditRepository: AuditRepository
) {

    suspend fun log(
        userId: Int?,
        action: String,
        entityType: String,
        entityId: Int? = null,
        details: String
    ) {
        auditRepository.log(userId, action, entityType, entityId, details)
    }

    suspend fun getLogs(
        userId: Int? = null,
        action: String? = null,
        entityType: String? = null,
        limit: Int = 100
    ): List<AuditLog> {
        return auditRepository.getLogs(userId, action, entityType, limit)
    }
}