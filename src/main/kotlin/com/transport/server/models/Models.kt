package com.transport.server.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val firebaseUid: String? = null,
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
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val userId: Int, val email: String, val createdAt: String)

@Serializable
data class PassengerData(
    val seatNumber: Int = 0,
    val firstName: String,
    val lastName: String,
    val patronymic: String = "",
    val documentType: String = "passport",
    val documentSeries: String = "",
    val documentNumber: String,
    val birthDate: String = "",
    val gender: String = "male"
)

@Serializable
data class BuyTicketRequest(
    val routeId: Int,
    val seatCount: Int,
    val seatNumbers: List<Int> = emptyList(),
    val passengers: List<PassengerData> = emptyList()
)

@Serializable
data class ApiError(val message: String)

// Admin models
@Serializable
data class AdminInfo(val id: Int, val email: String)

@Serializable
data class AdminLoginRequest(val email: String, val password: String)

@Serializable
data class AdminLoginResponse(val token: String, val email: String)

@Serializable
data class CreateRouteRequest(
    val originCity: String,
    val destinationCity: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: Double,
    val totalSeats: Int,
    val transportType: String
)
