package com.transport.server.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.transport.server.dao.UserDao
import com.transport.server.models.AuthResponse
import com.transport.server.models.LoginRequest
import com.transport.server.models.RegisterRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.Date

private const val USER_ISSUER = "transport-user"
private const val USER_AUDIENCE = "transport-user-api"
private val TOKEN_TTL_MS = 30L * 24 * 60 * 60 * 1000

fun Route.authRoutes(userDao: UserDao, jwtSecret: String) {
    route("/auth") {

        post("/register") {
            val req = call.receive<RegisterRequest>()
            if (req.email.isBlank() || req.password.length < 6)
                throw IllegalArgumentException("Email required and password must be at least 6 characters")
            val user = userDao.register(req.email.trim().lowercase(), req.password)
            val token = buildToken(user.id, user.email, jwtSecret)
            call.respond(HttpStatusCode.Created, AuthResponse(token, user.id, user.email, user.createdAt))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val user = userDao.login(req.email.trim().lowercase(), req.password)
            val token = buildToken(user.id, user.email, jwtSecret)
            call.respond(HttpStatusCode.OK, AuthResponse(token, user.id, user.email, user.createdAt))
        }
    }
}

fun verifyUserToken(token: String, jwtSecret: String): Int {
    val verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
        .withIssuer(USER_ISSUER)
        .withAudience(USER_AUDIENCE)
        .build()
    val decoded = verifier.verify(token)
    return decoded.getClaim("userId").asInt()
        ?: throw SecurityException("Invalid token payload")
}

private fun buildToken(userId: Int, email: String, secret: String): String =
    JWT.create()
        .withIssuer(USER_ISSUER)
        .withAudience(USER_AUDIENCE)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_TTL_MS))
        .sign(Algorithm.HMAC256(secret))
