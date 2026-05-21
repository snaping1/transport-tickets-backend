package com.transport.server.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object TokenUtils {
    fun extractIntClaim(token: String, claim: String, secret: String): Int? = runCatching {
        JWT.require(Algorithm.HMAC256(secret)).build()
            .verify(token)
            .getClaim(claim)
            .asInt()
    }.getOrNull()

    fun isExpired(token: String): Boolean = runCatching {
        JWT.decode(token).expiresAt?.before(java.util.Date()) ?: true
    }.getOrDefault(true)

    fun bearerToken(header: String?): String? =
        header?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")
}
