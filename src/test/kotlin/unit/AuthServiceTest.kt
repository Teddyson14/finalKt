package com.example.unit

import com.example.domain.models.User
import com.example.domain.models.UserRole
import com.example.repositories.UserRepository
import com.example.repositories.AuditRepository
import com.example.services.AuthService
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.*

class AuthServiceTest {

    @Test
    fun `test register new user success`() = runBlocking {

        val userRepo = mockk<UserRepository>()
        val auditRepo = mockk<AuditRepository>()


        coEvery { userRepo.findByUsername("testuser") } returns null
        coEvery { userRepo.findByEmail(any()) } returns null
        coEvery { userRepo.findAll() } returns emptyList()
        coEvery { userRepo.create(any(), any(), any(), any()) } returns User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hash",
            role = UserRole.ADMIN,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        coEvery { auditRepo.log(any(), any(), any(), any(), any()) } just Runs

        // Test
        val authService = AuthService(userRepo, auditRepo)
        val (user, token) = authService.register("testuser", "test@example.com", "password")


        assertEquals(1, user.id)
        assertEquals("testuser", user.username)
        assertNotNull(token)

        verify { coEvery { auditRepo.log(any(), "REGISTER", "USER", 1, any()) } }
    }

    @Test
    fun `test register with existing username throws exception`() = runBlocking {

        val userRepo = mockk<UserRepository>()
        val auditRepo = mockk<AuditRepository>()


        coEvery { userRepo.findByUsername("existing") } returns mockk()

        // Test
        val authService = AuthService(userRepo, auditRepo)

        assertFailsWith<IllegalArgumentException> {
            authService.register("existing", "email@test.com", "password")
        }
    }

    @Test
    fun `test login success`() = runBlocking {

        val userRepo = mockk<UserRepository>()
        val auditRepo = mockk<AuditRepository>()

        val user = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "\$2a\$12\$hashedpassword",
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        coEvery { userRepo.authenticate("testuser", "password") } returns user
        coEvery { auditRepo.log(any(), any(), any(), any(), any()) } just Runs


        val authService = AuthService(userRepo, auditRepo)
        val result = authService.login("testuser", "password")


        assertNotNull(result)
        val (userResponse, token) = result
        assertEquals(1, userResponse.id)
        assertEquals("testuser", userResponse.username)
        assertNotNull(token)
    }
}