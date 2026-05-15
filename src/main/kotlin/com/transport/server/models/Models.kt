package com.transport.server.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val firebaseUid: String,
    val email: String,
    val createdAt: String = ""
)

@Serializable
data class Route(
    val id: Int = 0,
    val originCity: String,
    val destinationCity: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: Double,
    val totalSeats: Int,
    val availableSeats: Int,
    val transportType: String,
    val svAvailableSeats: Int? = null,
    val coupeAvailableSeats: Int? = null,
    val platzkartAvailableSeats: Int? = null,
    val seatCarAvailableSeats: Int? = null
)

@Serializable
data class Ticket(
    val id: Int = 0,
    val userId: Int,
    val routeId: Int,
    val seatCount: Int,
    val totalPrice: Double,
    val status: String,
    val createdAt: String = "",
    val route: Route? = null,
    val seatNumbers: List<Int> = emptyList()
)

// Request/Response DTOs
@Serializable
data class VerifyTokenRequest(val idToken: String)

@Serializable
data class VerifyTokenResponse(val userId: Int, val firebaseUid: String, val email: String)

@Serializable
data class BuyTicketRequest(val routeId: Int, val seatCount: Int, val seatNumbers: List<Int> = emptyList())

@Serializable
data class ApiError(val message: String)
