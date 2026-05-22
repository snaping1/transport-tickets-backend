package com.transport.server.dao

import com.transport.server.models.Route
import com.transport.server.plugins.DatabaseFactory.dbQuery
import java.sql.Connection
import java.sql.ResultSet

class RouteDao {

    private data class TariffCounts(val sv: Int, val coupe: Int, val platzkart: Int, val seatCar: Int)

    private val baseSelect = """
        SELECT r.id, oc.name AS origin_city_name, dc.name AS destination_city_name,
               r.departure_time, r.arrival_time, r.price, r.total_seats,
               r.available_seats, r.transport_type
        FROM routes r
        JOIN cities oc ON r.origin_city_id = oc.id
        JOIN cities dc ON r.destination_city_id = dc.id
    """.trimIndent()

    private fun ResultSet.toRoute() = Route(
        id = getInt("id"),
        originCity = getString("origin_city_name"),
        destinationCity = getString("destination_city_name"),
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
            "SELECT ts.route_id, ts.seat_number FROM ticket_seats ts JOIN tickets t ON ts.ticket_id = t.id WHERE ts.route_id IN ($placeholders) AND t.status = 'active'"
        ).use { stmt ->
            routeIds.forEachIndexed { i, id -> stmt.setInt(i + 1, id) }
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    occupied.getOrPut(rs.getInt("route_id")) { mutableListOf() }.add(rs.getInt("seat_number"))
                }
            }
        }
        return routeIds.associateWith { id ->
            val seats = occupied[id] ?: emptyList()
            TariffCounts(
                sv       = 18  - seats.count { it in 1..18 },
                coupe    = 144 - seats.count { it in 19..162 },
                platzkart= 432 - seats.count { it in 163..594 },
                seatCar  = 60  - seats.count { it in 595..654 }
            )
        }
    }

    fun findAll(
        origin: String? = null,
        destination: String? = null,
        date: String? = null,
        transportType: String? = null
    ): List<Route> = dbQuery { conn ->
        val conditions = mutableListOf("r.departure_time > NOW()")
        val params = mutableListOf<Any>()

        if (!origin.isNullOrBlank()) {
            conditions.add("LOWER(oc.name) LIKE LOWER(?)")
            params.add("%$origin%")
        }
        if (!destination.isNullOrBlank()) {
            conditions.add("LOWER(dc.name) LIKE LOWER(?)")
            params.add("%$destination%")
        }
        if (!date.isNullOrBlank()) {
            conditions.add("DATE(r.departure_time AT TIME ZONE 'UTC') = ?::date")
            params.add(date)
        }
        if (!transportType.isNullOrBlank()) {
            conditions.add("r.transport_type = ?")
            params.add(transportType)
        }

        val sql = "$baseSelect WHERE ${conditions.joinToString(" AND ")} ORDER BY r.departure_time"
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
                    svAvailableSeats       = t?.sv,
                    coupeAvailableSeats    = t?.coupe,
                    platzkartAvailableSeats= t?.platzkart,
                    seatCarAvailableSeats  = t?.seatCar
                )
            } else route
        }
    }

    fun findById(id: Int): Route? = dbQuery { conn ->
        val route = conn.prepareStatement("$baseSelect WHERE r.id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs -> if (rs.next()) rs.toRoute() else null }
        } ?: return@dbQuery null

        if (route.transportType == "train") {
            val t = computePerTariff(listOf(id), conn)[id]
            route.copy(
                svAvailableSeats       = t?.sv,
                coupeAvailableSeats    = t?.coupe,
                platzkartAvailableSeats= t?.platzkart,
                seatCarAvailableSeats  = t?.seatCar
            )
        } else route
    }

    fun getOccupiedSeats(routeId: Int): List<Int> = dbQuery { conn ->
        conn.prepareStatement(
            "SELECT ts.seat_number FROM ticket_seats ts JOIN tickets t ON ts.ticket_id = t.id WHERE ts.route_id = ? AND t.status = 'active'"
        ).use { stmt ->
            stmt.setInt(1, routeId)
            stmt.executeQuery().use { rs ->
                buildList { while (rs.next()) add(rs.getInt("seat_number")) }
            }
        }
    }

    fun decrementSeats(routeId: Int, count: Int, conn: Connection): Boolean {
        return conn.prepareStatement(
            "UPDATE routes SET available_seats = available_seats - ? WHERE id = ? AND available_seats >= ?"
        ).use { stmt ->
            stmt.setInt(1, count)
            stmt.setInt(2, routeId)
            stmt.setInt(3, count)
            stmt.executeUpdate() > 0
        }
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
