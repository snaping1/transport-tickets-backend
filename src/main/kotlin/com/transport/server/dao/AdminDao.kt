package com.transport.server.dao

import com.transport.server.models.AdminInfo
import com.transport.server.models.CreateRouteRequest
import com.transport.server.models.Route
import com.transport.server.plugins.DatabaseFactory.dbQuery
import org.mindrot.jbcrypt.BCrypt

class AdminDao {

    fun verifyPassword(email: String, password: String): AdminInfo? = dbQuery { conn ->
        val row = conn.prepareStatement(
            "SELECT id, email, password_hash FROM admins WHERE email = ?"
        ).use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs ->
                if (rs.next())
                    Triple(rs.getInt("id"), rs.getString("email"), rs.getString("password_hash"))
                else null
            }
        } ?: return@dbQuery null

        if (BCrypt.checkpw(password, row.third)) AdminInfo(row.first, row.second) else null
    }

    fun ensureAdminExists(email: String, password: String) = dbQuery { conn ->
        val exists = conn.prepareStatement("SELECT 1 FROM admins WHERE email = ?").use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { it.next() }
        }
        if (!exists) {
            val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
            conn.prepareStatement("INSERT INTO admins (email, password_hash) VALUES (?, ?)").use { stmt ->
                stmt.setString(1, email)
                stmt.setString(2, hash)
                stmt.executeUpdate()
            }
        }
    }

    fun getAllRoutes(): List<Route> = dbQuery { conn ->
        conn.prepareStatement("""
            SELECT r.id, oc.name AS origin_city, dc.name AS destination_city,
                   r.departure_time, r.arrival_time, r.price, r.total_seats,
                   r.available_seats, r.transport_type
            FROM routes r
            JOIN cities oc ON r.origin_city_id = oc.id
            JOIN cities dc ON r.destination_city_id = dc.id
            ORDER BY r.departure_time DESC
        """.trimIndent()).use { stmt ->
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(Route(
                            id             = rs.getInt("id"),
                            originCity     = rs.getString("origin_city"),
                            destinationCity= rs.getString("destination_city"),
                            departureTime  = rs.getTimestamp("departure_time").toInstant().toString(),
                            arrivalTime    = rs.getTimestamp("arrival_time").toInstant().toString(),
                            price          = rs.getDouble("price"),
                            totalSeats     = rs.getInt("total_seats"),
                            availableSeats = rs.getInt("available_seats"),
                            transportType  = rs.getString("transport_type")
                        ))
                    }
                }
            }
        }
    }

    fun createRoute(req: CreateRouteRequest, adminId: Int): Route = dbQuery { conn ->
        fun upsertCity(name: String): Int =
            conn.prepareStatement(
                "INSERT INTO cities (name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name RETURNING id"
            ).use { stmt ->
                stmt.setString(1, name)
                stmt.executeQuery().use { rs -> rs.next(); rs.getInt("id") }
            }

        val originId = upsertCity(req.originCity)
        val destId   = upsertCity(req.destinationCity)

        val newId = conn.prepareStatement("""
            INSERT INTO routes
                (origin_city_id, destination_city_id, departure_time, arrival_time, price, total_seats, available_seats, transport_type, created_by_admin_id)
            VALUES (?, ?, ?::timestamptz, ?::timestamptz, ?, ?, ?, ?, ?)
            RETURNING id
        """.trimIndent()).use { stmt ->
            stmt.setInt(1, originId)
            stmt.setInt(2, destId)
            stmt.setString(3, req.departureTime)
            stmt.setString(4, req.arrivalTime)
            stmt.setDouble(5, req.price)
            stmt.setInt(6, req.totalSeats)
            stmt.setInt(7, req.totalSeats)
            stmt.setString(8, req.transportType)
            stmt.setInt(9, adminId)
            stmt.executeQuery().use { rs -> rs.next(); rs.getInt("id") }
        }

        Route(
            id             = newId,
            originCity     = req.originCity,
            destinationCity= req.destinationCity,
            departureTime  = req.departureTime,
            arrivalTime    = req.arrivalTime,
            price          = req.price,
            totalSeats     = req.totalSeats,
            availableSeats = req.totalSeats,
            transportType  = req.transportType
        )
    }

    fun deleteRoute(id: Int): Boolean = dbQuery { conn ->
        conn.prepareStatement("DELETE FROM routes WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeUpdate() > 0
        }
    }
}
