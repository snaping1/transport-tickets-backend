package com.transport.server

import com.transport.server.plugins.*
import com.transport.server.service.FirebaseService
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val credentialsFile = environment.config.propertyOrNull("firebase.credentialsFile")?.getString()
        ?: System.getenv("FIREBASE_CREDENTIALS_FILE")
    val projectId = environment.config.propertyOrNull("firebase.projectId")?.getString()
        ?: System.getenv("FIREBASE_PROJECT_ID")

    FirebaseService.init(credentialsFile, projectId)
    configureDatabase()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
