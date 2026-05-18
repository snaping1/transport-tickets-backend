package com.transport.server.util

import com.transport.server.models.ApiError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String) =
    respond(status, ApiError(message))

suspend fun ApplicationCall.respondNotFound(entity: String = "Resource") =
    respondError(HttpStatusCode.NotFound, "$entity not found")

suspend fun ApplicationCall.respondBadRequest(message: String) =
    respondError(HttpStatusCode.BadRequest, message)

suspend fun ApplicationCall.respondUnauthorized(message: String = "Unauthorized") =
    respondError(HttpStatusCode.Unauthorized, message)
