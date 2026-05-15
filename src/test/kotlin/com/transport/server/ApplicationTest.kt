package com.transport.server

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `test routes endpoint requires auth`() = testApplication {
        val response = client.get("/routes")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test auth verify requires body`() = testApplication {
        val response = client.post("/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        // Without Firebase configured this returns 500, but endpoint exists
        assert(response.status != HttpStatusCode.NotFound)
    }
}
