package com.transport.server.util

object Constants {
    const val TOKEN_TTL_DAYS          = 30L
    const val ADMIN_TOKEN_TTL_HOURS   = 24L
    const val MAX_SEATS_PER_TICKET    = 10
    const val MIN_CANCEL_HOURS_BEFORE = 2L
    const val BCRYPT_ROUNDS           = 12

    object Train {
        val SV_RANGE        = 1..18
        val COUPE_RANGE     = 19..162
        val PLATZKART_RANGE = 163..594
        val SEAT_CAR_RANGE  = 595..654

        const val SV_TOTAL        = 18
        const val COUPE_TOTAL     = 144
        const val PLATZKART_TOTAL = 432
        const val SEAT_CAR_TOTAL  = 60
    }
}
