package com.transport.server.routes

import com.transport.server.dao.UserDao
import com.transport.server.models.VerifyTokenRequest
import com.transport.server.models.VerifyTokenResponse
import com.transport.server.service.FirebaseService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userDao: UserDao) {
    route("/auth") {
        post("/verify") {
            val request = call.receive<VerifyTokenRequest>()
            val firebaseToken = FirebaseService.verifyToken(request.idToken)
            val user = userDao.createOrUpdate(
                firebaseUid = firebaseToken.uid,
                email = firebaseToken.email ?: ""
            )
            call.respond(
                HttpStatusCode.OK,
                VerifyTokenResponse(
                    userId = user.id,
                    firebaseUid = user.firebaseUid,
                    email = user.email
                )
            )
        }
    }
}
