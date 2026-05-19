package com.transport.server.routes

import com.transport.server.dao.RouteDao
import com.transport.server.dao.UserDao
import com.transport.server.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.routesRoutes(routeDao: RouteDao, userDao: UserDao, jwtSecret: String) {
    route("/routes") {
        get {
            call.requireUserAuth(userDao, jwtSecret)
            val origin = call.request.queryParameters["origin"]
            val destination = call.request.queryParameters["destination"]
            val date = call.request.queryParameters["date"]
            val transportType = call.request.queryParameters["transportType"]
            val routes = routeDao.findAll(origin, destination, date, transportType)
            call.respond(HttpStatusCode.OK, routes)
        }

        get("/{id}/seats") {
            call.requireUserAuth(userDao, jwtSecret)
            val routeId = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid route id")
            val occupied = routeDao.getOccupiedSeats(routeId)
            call.respond(HttpStatusCode.OK, occupied)
        }
    }
}

suspend fun ApplicationCall.requireUserAuth(userDao: UserDao, jwtSecret: String): User {
    val authHeader = request.headers["Authorization"]
        ?: throw SecurityException("Missing Authorization header")
    val token = authHeader.removePrefix("Bearer ").trim()
    val userId = verifyUserToken(token, jwtSecret)
    return userDao.findById(userId)
        ?: throw SecurityException("User not found")
}
