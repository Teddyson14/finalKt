package com.example.repositories

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.domain.models.User
import com.example.domain.models.UserRole
import com.example.domain.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.time.LocalDateTime

class UserRepository : BaseRepository() {

    suspend fun create(username: String, email: String, password: String, role: UserRole = UserRole.USER): User {
        val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val now = LocalDateTime.now()

        return dbQuery {
            val id = Users.insert {
                it[Users.username] = username
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.role] = role
                it[Users.createdAt] = now
                it[Users.updatedAt] = now
            } get Users.id

            findById(id)!!
        }
    }

    suspend fun findByUsername(username: String): User? {
        return dbQuery {
            Users.select { Users.username eq username }
                .map(::toUser)
                .singleOrNull()
        }
    }

    suspend fun findAll(): List<User> {
        return dbQuery {
            Users.selectAll().map(::toUser)
        }
    }

    suspend fun findByEmail(email: String): User? {
        return dbQuery {
            Users.select { Users.email eq email }
                .map(::toUser)
                .singleOrNull()
        }
    }

    suspend fun findById(id: Int): User? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map(::toUser)
                .singleOrNull()
        }
    }

    suspend fun authenticate(username: String, password: String): User? {
        val user = findByUsername(username) ?: return null

        val verified = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash).verified

        return if (verified) user else null
    }

    suspend fun updateRole(id: Int, role: UserRole) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[Users.role] = role
                it[Users.updatedAt] = LocalDateTime.now()
            }
        }
    }

    private fun toUser(row: ResultRow): User = User(
        id = row[Users.id],
        username = row[Users.username],
        email = row[Users.email],
        passwordHash = row[Users.passwordHash],
        role = row[Users.role],
        createdAt = row[Users.createdAt],
        updatedAt = row[Users.updatedAt]
    )
}
