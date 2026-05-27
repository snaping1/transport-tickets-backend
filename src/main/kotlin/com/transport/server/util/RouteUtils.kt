package com.transport.server.util

import java.time.Instant
import java.time.temporal.ChronoUnit

object RouteUtils {
    fun hoursUntilDeparture(departureTime: String): Long = runCatching {
        ChronoUnit.HOURS.between(Instant.now(), Instant.parse(departureTime))
    }.getOrDefault(-1L)

    fun canCancel(departureTime: String, minHours: Long = 2L): Boolean =
        hoursUntilDeparture(departureTime) >= minHours

    fun isUpcoming(departureTime: String): Boolean =
        runCatching { Instant.parse(departureTime).isAfter(Instant.now()) }.getOrDefault(false)

    fun durationMinutes(departure: String, arrival: String): Long = runCatching {
        ChronoUnit.MINUTES.between(Instant.parse(departure), Instant.parse(arrival))
    }.getOrDefault(0L)
}
