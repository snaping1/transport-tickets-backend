package com.transport.server.routes

import com.transport.server.dao.RouteDao
import com.transport.server.dao.UserDao
import com.transport.server.service.FirebaseService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.routesRoutes(routeDao: RouteDao, userDao: UserDao) {
    route("/routes") {
        get {
            call.requireAuth(userDao)
            val origin = call.request.queryParameters["origin"]
            val destination = call.request.queryParameters["destination"]
            val date = call.request.queryParameters["date"]
            val transportType = call.request.queryParameters["transportType"]
            val routes = routeDao.findAll(origin, destination, date, transportType)
            call.respond(HttpStatusCode.OK, routes)
        }

        get("/{id}/seats") {
            call.requireAuth(userDao)
            val routeId = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid route id")
            val occupied = routeDao.getOccupiedSeats(routeId)
            call.respond(HttpStatusCode.OK, occupied)
        }
    }
}

suspend fun ApplicationCall.requireAuth(userDao: UserDao): com.transport.server.models.User {
    val authHeader = request.headers["Authorization"]
        ?: throw SecurityException("Missing Authorization header")
    val token = authHeader.removePrefix("Bearer ").trim()
    val firebaseToken = FirebaseService.verifyToken(token)
    return userDao.findByFirebaseUid(firebaseToken.uid)
        ?: throw SecurityException("User not registered. Call /auth/verify first.")
}
