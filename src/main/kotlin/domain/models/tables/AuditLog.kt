package com.example.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Serializable
data class AuditLog(
    val id: Int,
    val userId: Int?,
    val action: String,
    val entityType: String,
    val entityId: Int?,
    val details: String,
    @Contextual
    val timestamp: LocalDateTime
)