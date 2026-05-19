package com.transport.server.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.transport.server.dao.AdminDao
import com.transport.server.models.AdminLoginRequest
import com.transport.server.models.AdminLoginResponse
import com.transport.server.models.CreateRouteRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.Date

private const val ISSUER = "transport-admin"
private const val AUDIENCE = "transport-admin-api"

fun Route.adminRoutes(adminDao: AdminDao, jwtSecret: String) {
    route("/admin") {
        post("/login") {
            val req = call.receive<AdminLoginRequest>()
            val admin = adminDao.verifyPassword(req.email, req.password)
                ?: throw SecurityException("Invalid email or password")

            val token = JWT.create()
                .withAudience(AUDIENCE)
                .withIssuer(ISSUER)
                .withClaim("adminId", admin.id)
                .withClaim("email", admin.email)
                .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000L))
                .sign(Algorithm.HMAC256(jwtSecret))

            call.respond(HttpStatusCode.OK, AdminLoginResponse(token = token, email = admin.email))
        }

        authenticate("admin-jwt") {
            get("/routes") {
                call.respond(HttpStatusCode.OK, adminDao.getAllRoutes())
            }

            post("/routes") {
                val req = call.receive<CreateRouteRequest>()
                if (req.originCity.isBlank() || req.destinationCity.isBlank())
                    throw IllegalArgumentException("Origin and destination are required")
                if (req.transportType !in listOf("bus", "train", "plane"))
                    throw IllegalArgumentException("Transport type must be bus, train, or plane")
                if (req.price <= 0) throw IllegalArgumentException("Price must be positive")
                if (req.totalSeats <= 0) throw IllegalArgumentException("Total seats must be positive")
                call.respond(HttpStatusCode.Created, adminDao.createRoute(req))
            }

            delete("/routes/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid route id")
                if (!adminDao.deleteRoute(id))
                    throw NoSuchElementException("Route $id not found")
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
