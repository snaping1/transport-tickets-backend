package com.transport.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.transport.server.models.ApiError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAdminAuth(jwtSecret: String) {
    install(Authentication) {
        jwt("admin-jwt") {
            realm = "Transport Admin"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience("transport-admin-api")
                    .withIssuer("transport-admin")
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("adminId").asInt() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiError("Admin authentication required"))
            }
        }
    }
}
