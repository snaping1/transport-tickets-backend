package com.transport.server.dao

import com.transport.server.models.Route
import com.transport.server.plugins.DatabaseFactory.dbQuery
import java.sql.Connection
import java.sql.ResultSet

class RouteDao {

    private data class TariffCounts(val sv: Int, val coupe: Int, val platzkart: Int, val seatCar: Int)

    private fun ResultSet.toRoute() = Route(
        id = getInt("id"),
        originCity = getString("origin_city"),
        destinationCity = getString("destination_city"),
        departureTime = getTimestamp("departure_time").toInstant().toString(),
        arrivalTime = getTimestamp("arrival_time").toInstant().toString(),
        price = getDouble("price"),
        totalSeats = getInt("total_seats"),
        availableSeats = getInt("available_seats"),
        transportType = getString("transport_type")
    )

    private fun computePerTariff(routeIds: List<Int>, conn: Connection): Map<Int, TariffCounts> {
        if (routeIds.isEmpty()) return emptyMap()
        val placeholders = routeIds.joinToString(",") { "?" }
        val occupied = mutableMapOf<Int, MutableList<Int>>()
        conn.prepareStatement(
            "SELECT route_id, seat_numbers FROM tickets WHERE route_id IN ($placeholders) AND status = 'active' AND seat_numbers IS NOT NULL AND seat_numbers != ''"
        ).use { stmt ->
            routeIds.forEachIndexed { i, id -> stmt.setInt(i + 1, id) }
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    val rid = rs.getInt("route_id")
                    rs.getString("seat_numbers")
                        .split(",").mapNotNull { it.trim().toIntOrNull() }
                        .forEach { occupied.getOrPut(rid) { mutableListOf() }.add(it) }
                }
            }
        }
        return routeIds.associateWith { id ->
            val seats = occupied[id] ?: emptyList()
            TariffCounts(
                sv = 18 - seats.count { it in 1..18 },
                coupe = 144 - seats.count { it in 19..162 },
                platzkart = 432 - seats.count { it in 163..594 },
                seatCar = 60 - seats.count { it in 595..654 }
            )
        }
    }

    fun findAll(
        origin: String? = null,
        destination: String? = null,
        date: String? = null,
        transportType: String? = null
    ): List<Route> = dbQuery { conn ->
        val conditions = mutableListOf("departure_time > NOW()")
        val params = mutableListOf<Any>()

        if (!origin.isNullOrBlank()) {
            conditions.add("LOWER(origin_city) LIKE LOWER(?)")
            params.add("%$origin%")
        }
        if (!destination.isNullOrBlank()) {
            conditions.add("LOWER(destination_city) LIKE LOWER(?)")
            params.add("%$destination%")
        }
        if (!date.isNullOrBlank()) {
            conditions.add("DATE(departure_time AT TIME ZONE 'UTC') = ?::date")
            params.add(date)
        }
        if (!transportType.isNullOrBlank()) {
            conditions.add("transport_type = ?")
            params.add(transportType)
        }

        val sql = "SELECT * FROM routes WHERE ${conditions.joinToString(" AND ")} ORDER BY departure_time"
        val baseRoutes = conn.prepareStatement(sql).use { stmt ->
            params.forEachIndexed { i, param -> stmt.setString(i + 1, param.toString()) }
            stmt.executeQuery().use { rs ->
                buildList { while (rs.next()) add(rs.toRoute()) }
            }
        }

        val trainIds = baseRoutes.filter { it.transportType == "train" }.map { it.id }
        val perTariff = computePerTariff(trainIds, conn)

        baseRoutes.map { route ->
            if (route.transportType == "train") {
                val t = perTariff[route.id]
                route.copy(
                    svAvailableSeats = t?.sv,
                    coupeAvailableSeats = t?.coupe,
                    platzkartAvailableSeats = t?.platzkart,
                    seatCarAvailableSeats = t?.seatCar
                )
            } else route
        }
    }

    fun findById(id: Int): Route? = dbQuery { conn ->
        val route = conn.prepareStatement("SELECT * FROM routes WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.toRoute() else null
            }
        } ?: return@dbQuery null

        if (route.transportType == "train") {
            val t = computePerTariff(listOf(id), conn)[id]
            route.copy(
                svAvailableSeats = t?.sv,
                coupeAvailableSeats = t?.coupe,
                platzkartAvailableSeats = t?.platzkart,
                seatCarAvailableSeats = t?.seatCar
            )
        } else route
    }

    fun getOccupiedSeats(routeId: Int): List<Int> = dbQuery { conn ->
        conn.prepareStatement(
            "SELECT seat_numbers FROM tickets WHERE route_id = ? AND status = 'active' AND seat_numbers != ''"
        ).use { stmt ->
            stmt.setInt(1, routeId)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        val nums = rs.getString("seat_numbers")
                        if (!nums.isNullOrBlank()) {
                            addAll(nums.split(",").mapNotNull { it.trim().toIntOrNull() })
                        }
                    }
                }
            }
        }
    }

    fun decrementSeats(routeId: Int, count: Int, conn: Connection): Boolean {
        val updated = conn.prepareStatement(
            "UPDATE routes SET available_seats = available_seats - ? WHERE id = ? AND available_seats >= ?"
        ).use { stmt ->
            stmt.setInt(1, count)
            stmt.setInt(2, routeId)
            stmt.setInt(3, count)
            stmt.executeUpdate()
        }
        return updated > 0
    }

    fun incrementSeats(routeId: Int, count: Int, conn: Connection) {
        conn.prepareStatement(
            "UPDATE routes SET available_seats = LEAST(available_seats + ?, total_seats) WHERE id = ?"
        ).use { stmt ->
            stmt.setInt(1, count)
            stmt.setInt(2, routeId)
            stmt.executeUpdate()
        }
    }
}
