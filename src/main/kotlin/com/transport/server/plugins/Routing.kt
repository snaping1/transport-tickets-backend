package com.transport.server.plugins

import com.transport.server.dao.RouteDao
import com.transport.server.dao.TicketDao
import com.transport.server.dao.UserDao
import com.transport.server.routes.authRoutes
import com.transport.server.routes.routesRoutes
import com.transport.server.routes.ticketRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userDao = UserDao()
    val routeDao = RouteDao()
    val ticketDao = TicketDao()

    routing {
        authRoutes(userDao)
        routesRoutes(routeDao, userDao)
        ticketRoutes(ticketDao, routeDao, userDao)
    }
}
