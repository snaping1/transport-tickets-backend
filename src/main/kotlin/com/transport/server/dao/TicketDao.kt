package com.transport.server.dao

import com.transport.server.models.PassengerData
import com.transport.server.models.Route
import com.transport.server.models.Ticket
import com.transport.server.plugins.DatabaseFactory.dbQuery
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

class TicketDao {

    private fun ResultSet.toTicket(includeRoute: Boolean = false): Ticket {
        val route: Route? = if (includeRoute && getObject("r_id") != null) {
            try {
                Route(
                    id             = getInt("r_id"),
                    originCity     = getString("origin_city"),
                    destinationCity= getString("destination_city"),
                    departureTime  = getTimestamp("departure_time").toInstant().toString(),
                    arrivalTime    = getTimestamp("arrival_time").toInstant().toString(),
                    price          = getDouble("price"),
                    totalSeats     = getInt("total_seats"),
                    availableSeats = getInt("available_seats"),
                    transportType  = getString("transport_type")
                )
            } catch (_: Exception) { null }
        } else null

        val seatNums = getString("seat_numbers")
            ?.let { raw -> if (raw.isBlank()) emptyList() else raw.split(",").mapNotNull { it.trim().toIntOrNull() } }
            ?: emptyList()

        return Ticket(
            id         = getInt("id"),
            userId     = getInt("user_id"),
            routeId    = getInt("route_id"),
            seatCount  = getInt("seat_count"),
            totalPrice = getDouble("total_price"),
            status     = getString("status"),
            createdAt  = getTimestamp("created_at").toInstant().toString(),
            route      = route,
            seatNumbers= seatNums
        )
    }

    fun create(
        userId: Int,
        routeId: Int,
        seatCount: Int,
        totalPrice: Double,
        seatNumbers: List<Int>,
        passengers: List<PassengerData>,
        conn: Connection
    ): Ticket {
        val (ticketId, createdAt) = conn.prepareStatement(
            "INSERT INTO tickets (user_id, route_id, seat_count, total_price, status) VALUES (?, ?, ?, ?, 'active') RETURNING id, created_at"
        ).use { stmt ->
            stmt.setInt(1, userId)
            stmt.setInt(2, routeId)
            stmt.setInt(3, seatCount)
            stmt.setDouble(4, totalPrice)
            stmt.executeQuery().use { rs ->
                rs.next()
                Pair(rs.getInt("id"), rs.getTimestamp("created_at").toInstant().toString())
            }
        }

        // Insert ticket_seats one-by-one to get IDs for passenger FK
        val seatIdByNumber = mutableMapOf<Int, Int>()
        seatNumbers.forEach { seatNum ->
            val seatId = conn.prepareStatement(
                "INSERT INTO ticket_seats (ticket_id, route_id, seat_number) VALUES (?, ?, ?) RETURNING id"
            ).use { stmt ->
                stmt.setInt(1, ticketId)
                stmt.setInt(2, routeId)
                stmt.setInt(3, seatNum)
                stmt.executeQuery().use { rs -> rs.next(); rs.getInt("id") }
            }
            seatIdByNumber[seatNum] = seatId
        }

        // Insert passengers linked to their seat
        passengers.forEach { p ->
            conn.prepareStatement("""
                INSERT INTO passengers (ticket_id, seat_id, first_name, last_name, patronymic,
                    document_type, document_series, document_number, birth_date, gender)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::date, ?)
            """.trimIndent()).use { stmt ->
                stmt.setInt(1, ticketId)
                val seatId = seatIdByNumber[p.seatNumber]
                if (seatId != null) stmt.setInt(2, seatId) else stmt.setNull(2, Types.INTEGER)
                stmt.setString(3, p.firstName)
                stmt.setString(4, p.lastName)
                if (p.patronymic.isBlank()) stmt.setNull(5, Types.VARCHAR) else stmt.setString(5, p.patronymic)
                stmt.setString(6, p.documentType)
                if (p.documentSeries.isBlank()) stmt.setNull(7, Types.VARCHAR) else stmt.setString(7, p.documentSeries)
                stmt.setString(8, p.documentNumber)
                if (p.birthDate.isBlank()) stmt.setNull(9, Types.DATE) else stmt.setString(9, p.birthDate)
                stmt.setString(10, p.gender)
                stmt.executeUpdate()
            }
        }

        // Auto-create payment record
        conn.prepareStatement(
            "INSERT INTO payments (ticket_id, user_id, amount, payment_method, status) VALUES (?, ?, ?, 'card', 'completed')"
        ).use { stmt ->
            stmt.setInt(1, ticketId)
            stmt.setInt(2, userId)
            stmt.setDouble(3, totalPrice)
            stmt.executeUpdate()
        }

        return Ticket(
            id         = ticketId,
            userId     = userId,
            routeId    = routeId,
            seatCount  = seatCount,
            totalPrice = totalPrice,
            status     = "active",
            createdAt  = createdAt,
            route      = null,
            seatNumbers= seatNumbers
        )
    }

    fun findByUserId(userId: Int): List<Ticket> = dbQuery { conn ->
        val sql = """
            SELECT t.id, t.user_id, t.route_id, t.seat_count, t.total_price, t.status, t.created_at,
                   r.id AS r_id, oc.name AS origin_city, dc.name AS destination_city,
                   r.departure_time, r.arrival_time, r.price, r.total_seats,
                   r.available_seats, r.transport_type,
                   STRING_AGG(ts.seat_number::text, ',' ORDER BY ts.seat_number) AS seat_numbers
            FROM tickets t
            LEFT JOIN routes r ON t.route_id = r.id
            LEFT JOIN cities oc ON r.origin_city_id = oc.id
            LEFT JOIN cities dc ON r.destination_city_id = dc.id
            LEFT JOIN ticket_seats ts ON ts.ticket_id = t.id
            WHERE t.user_id = ?
            GROUP BY t.id, r.id, oc.name, dc.name
            ORDER BY t.created_at DESC
        """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, userId)
            stmt.executeQuery().use { rs ->
                buildList { while (rs.next()) add(rs.toTicket(includeRoute = true)) }
            }
        }
    }

    fun findById(id: Int): Ticket? = dbQuery { conn ->
        val sql = """
            SELECT t.id, t.user_id, t.route_id, t.seat_count, t.total_price, t.status, t.created_at,
                   STRING_AGG(ts.seat_number::text, ',' ORDER BY ts.seat_number) AS seat_numbers
            FROM tickets t
            LEFT JOIN ticket_seats ts ON ts.ticket_id = t.id
            WHERE t.id = ?
            GROUP BY t.id
        """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs -> if (rs.next()) rs.toTicket() else null }
        }
    }

    fun cancelWithReturn(id: Int, userId: Int, conn: Connection): Pair<Int, Int>? {
        return conn.prepareStatement(
            "UPDATE tickets SET status = 'cancelled' WHERE id = ? AND user_id = ? AND status = 'active' RETURNING seat_count, route_id"
        ).use { stmt ->
            stmt.setInt(1, id)
            stmt.setInt(2, userId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) Pair(rs.getInt("seat_count"), rs.getInt("route_id")) else null
            }
        }
    }
}
