package com.transport.server.util

object SeatConstants {
    val SV_RANGE        = 1..18
    val COUPE_RANGE     = 19..162
    val PLATZKART_RANGE = 163..594
    val SEAT_CAR_RANGE  = 595..654

    const val SV_TOTAL        = 18
    const val COUPE_TOTAL     = 144
    const val PLATZKART_TOTAL = 432
    const val SEAT_CAR_TOTAL  = 60
    const val TRAIN_TOTAL     = 654

    const val SV_MULTIPLIER            = 2.5
    const val COUPE_UPPER_MULTIPLIER   = 2.0
    const val COUPE_LOWER_MULTIPLIER   = 1.6
    const val PLATZKART_UPPER_MULT     = 1.3
    const val PLATZKART_LOWER_MULT     = 0.85
    const val PLATZKART_SIDE_UPPER     = 1.1
    const val PLATZKART_SIDE_LOWER     = 0.80
    const val PLATZKART_SERVICE_UPPER  = 0.75
    const val PLATZKART_SERVICE_LOWER  = 0.65
    const val SEAT_CAR_MULTIPLIER      = 0.7
}
