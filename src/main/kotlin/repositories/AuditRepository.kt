package com.example.repositories

import com.example.domain.models.AuditLog
import com.example.domain.tables.AuditLogs
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.time.LocalDateTime

class AuditRepository : BaseRepository() {

    suspend fun log(
        userId: Int?,
        action: String,
        entityType: String,
        entityId: Int? = null,
        details: String
    ) {
        dbQuery {
            AuditLogs.insert {
                it[AuditLogs.userId] = userId
                it[AuditLogs.action] = action
                it[AuditLogs.entityType] = entityType
                it[AuditLogs.entityId] = entityId
                it[AuditLogs.details] = details
                it[AuditLogs.timestamp] = LocalDateTime.now()
            }
        }
    }

    suspend fun getLogs(
        userId: Int? = null,
        action: String? = null,
        entityType: String? = null,
        limit: Int = 100
    ): List<AuditLog> {
        return dbQuery {
            val query = AuditLogs.selectAll()

            userId?.let {
                query.andWhere { AuditLogs.userId eq it }
            }

            action?.let {
                query.andWhere { AuditLogs.action eq it }
            }

            entityType?.let {
                query.andWhere { AuditLogs.entityType eq it }
            }

            query.orderBy(AuditLogs.timestamp to SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    AuditLog(
                        id = row[AuditLogs.id],
                        userId = row[AuditLogs.userId],
                        action = row[AuditLogs.action],
                        entityType = row[AuditLogs.entityType],
                        entityId = row[AuditLogs.entityId],
                        details = row[AuditLogs.details],
                        timestamp = row[AuditLogs.timestamp]
                    )
                }
        }
    }
}