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
        conn.prepareStatement("SELECT * FROM routes ORDER BY departure_time DESC").use { stmt ->
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            Route(
                                id = rs.getInt("id"),
                                originCity = rs.getString("origin_city"),
                                destinationCity = rs.getString("destination_city"),
                                departureTime = rs.getTimestamp("departure_time").toInstant().toString(),
                                arrivalTime = rs.getTimestamp("arrival_time").toInstant().toString(),
                                price = rs.getDouble("price"),
                                totalSeats = rs.getInt("total_seats"),
                                availableSeats = rs.getInt("available_seats"),
                                transportType = rs.getString("transport_type")
                            )
                        )
                    }
                }
            }
        }
    }

    fun createRoute(req: CreateRouteRequest): Route = dbQuery { conn ->
        conn.prepareStatement(
            """
            INSERT INTO routes
                (origin_city, destination_city, departure_time, arrival_time, price, total_seats, available_seats, transport_type)
            VALUES (?, ?, ?::timestamptz, ?::timestamptz, ?, ?, ?, ?)
            RETURNING id
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, req.originCity)
            stmt.setString(2, req.destinationCity)
            stmt.setString(3, req.departureTime)
            stmt.setString(4, req.arrivalTime)
            stmt.setDouble(5, req.price)
            stmt.setInt(6, req.totalSeats)
            stmt.setInt(7, req.totalSeats)
            stmt.setString(8, req.transportType)
            stmt.executeQuery().use { rs ->
                rs.next()
                Route(
                    id = rs.getInt("id"),
                    originCity = req.originCity,
                    destinationCity = req.destinationCity,
                    departureTime = req.departureTime,
                    arrivalTime = req.arrivalTime,
                    price = req.price,
                    totalSeats = req.totalSeats,
                    availableSeats = req.totalSeats,
                    transportType = req.transportType
                )
            }
        }
    }

    fun deleteRoute(id: Int): Boolean = dbQuery { conn ->
        conn.prepareStatement("DELETE FROM routes WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeUpdate() > 0
        }
    }
}
