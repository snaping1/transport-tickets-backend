package com.transport.server.plugins

import com.transport.server.dao.AdminDao
import com.transport.server.dao.RouteDao
import com.transport.server.dao.TicketDao
import com.transport.server.dao.UserDao
import com.transport.server.routes.adminRoutes
import com.transport.server.routes.authRoutes
import com.transport.server.routes.routesRoutes
import com.transport.server.routes.ticketRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(jwtSecret: String) {
    val userDao = UserDao()
    val routeDao = RouteDao()
    val ticketDao = TicketDao()
    val adminDao = AdminDao()

    routing {
        authRoutes(userDao, jwtSecret)
        routesRoutes(routeDao, userDao, jwtSecret)
        ticketRoutes(ticketDao, routeDao, userDao, jwtSecret)
        adminRoutes(adminDao, jwtSecret)
    }
}
