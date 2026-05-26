package com.transport.server.util

object ErrorCodes {
    const val INVALID_CREDENTIALS  = "AUTH_001"
    const val TOKEN_EXPIRED        = "AUTH_002"
    const val TOKEN_INVALID        = "AUTH_003"

    const val INSUFFICIENT_SEATS   = "TICKET_001"
    const val SEATS_ALREADY_TAKEN  = "TICKET_002"
    const val CANCEL_TOO_LATE      = "TICKET_003"
    const val TICKET_NOT_FOUND     = "TICKET_004"

    const val ROUTE_NOT_FOUND      = "ROUTE_001"
    const val ROUTE_EXPIRED        = "ROUTE_002"

    const val USER_NOT_FOUND       = "USER_001"
    const val EMAIL_TAKEN          = "USER_002"

    const val VALIDATION_ERROR     = "GENERAL_001"
    const val NOT_FOUND            = "GENERAL_002"
    const val FORBIDDEN            = "GENERAL_003"
}
