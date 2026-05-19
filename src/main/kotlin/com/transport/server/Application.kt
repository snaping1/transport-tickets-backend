package com.transport.server

import com.transport.server.dao.AdminDao
import com.transport.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val jwtSecret = environment.config.propertyOrNull("admin.jwtSecret")?.getString()
        ?: System.getenv("ADMIN_JWT_SECRET")
        ?: "transport-tickets-admin-jwt-secret-2024"

    configureDatabase()
    configureSerialization()
    configureSecurity()
    configureAdminAuth(jwtSecret)
    configureRouting(jwtSecret)

    AdminDao().ensureAdminExists("admin@transport.ru", "Admin2024!")
}
