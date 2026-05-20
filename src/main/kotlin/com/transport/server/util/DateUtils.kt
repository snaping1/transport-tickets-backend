package com.transport.server.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val zone = ZoneId.of("Europe/Moscow")
    private val fmt  = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale("ru")).withZone(zone)

    fun format(instant: Instant): String = fmt.format(instant)

    fun parseOrNull(iso: String): Instant? = runCatching { Instant.parse(iso) }.getOrNull()

    fun isValidRange(from: String, to: String): Boolean {
        val f = parseOrNull(from) ?: return false
        val t = parseOrNull(to)   ?: return false
        return f.isBefore(t)
    }

    fun isInFuture(iso: String): Boolean =
        runCatching { Instant.parse(iso).isAfter(Instant.now()) }.getOrDefault(false)
}
