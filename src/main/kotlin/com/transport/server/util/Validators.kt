package com.transport.server.util

object Validators {
    fun isValidEmail(email: String): Boolean =
        email.length >= 3 && email.contains('@') && email.substringAfter('@').contains('.')

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isValidTransportType(type: String): Boolean = type in setOf("bus", "train", "plane")

    fun isValidPrice(price: Double): Boolean = price > 0

    fun isValidSeatCount(count: Int): Boolean = count in 1..10

    fun isValidDocumentNumber(number: String): Boolean = number.isNotBlank() && number.length >= 4
}
