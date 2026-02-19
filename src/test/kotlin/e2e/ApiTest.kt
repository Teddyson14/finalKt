package com.example.e2e

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.Test
import com.example.ApplicationTest
import kotlin.test.*

class ApiTest {

    @Test
    fun `test health endpoint`() = testApplication {

        application {

        }

        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = bodyAsText()
            assertTrue(response.contains("OK"))
        }
    }

    @Test
    fun `test register and login flow`() = testApplication {
        application {

        }


        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"e2etest", "password":"test123"}""")
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)


        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"e2etest", "password":"test123"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        val json = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject
        assertTrue(json.containsKey("token"))
    }
}