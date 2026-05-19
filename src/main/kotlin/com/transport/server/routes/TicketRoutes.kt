package com.transport.server.routes

import com.transport.server.dao.RouteDao
import com.transport.server.dao.TicketDao
import com.transport.server.dao.UserDao
import com.transport.server.models.BuyTicketRequest
import com.transport.server.plugins.DatabaseFactory.dbQuery
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Route.ticketRoutes(ticketDao: TicketDao, routeDao: RouteDao, userDao: UserDao, jwtSecret: String) {
    route("/tickets") {

        post("/buy") {
            val user = call.requireUserAuth(userDao, jwtSecret)
            val request = call.receive<BuyTicketRequest>()

            require(request.seatCount in 1..10) { "Seat count must be between 1 and 10" }
            require(request.seatNumbers.isEmpty() || request.seatNumbers.size == request.seatCount) {
                "seatNumbers count must match seatCount"
            }

            val ticket = dbQuery { conn ->
                val route = routeDao.findById(request.routeId)
                    ?: throw NoSuchElementException("Route not found")

                if (request.seatNumbers.isNotEmpty()) {
                    val occupied = routeDao.getOccupiedSeats(request.routeId)
                    val conflict = request.seatNumbers.intersect(occupied.toSet())
                    if (conflict.isNotEmpty()) throw IllegalStateException("Seats already taken: $conflict")
                }

                val seatsDecremented = routeDao.decrementSeats(request.routeId, request.seatCount, conn)
                if (!seatsDecremented) throw IllegalStateException("Not enough available seats")

                val totalPrice = if (route.transportType == "train" && request.seatNumbers.isNotEmpty()) {
                    request.seatNumbers.sumOf { seatNum ->
                        route.price * when {
                            seatNum in 1..18 -> 2.5
                            seatNum in 19..162 -> { val p = (seatNum - 19) % 4; if (p == 0 || p == 2) 2.0 else 1.6 }
                            seatNum in 163..594 -> {
                                val local = (seatNum - 163) % 54
                                if (local < 36) when (local % 4) { 0 -> 1.3; 1 -> 0.85; 2 -> 1.1; else -> 0.80 }
                                else if ((local - 36) % 2 == 0) 0.75 else 0.65
                            }
                            seatNum in 595..654 -> 0.7
                            else -> 1.0
                        }
                    }
                } else {
                    route.price * request.seatCount
                }
                ticketDao.create(user.id, request.routeId, request.seatCount, totalPrice, request.seatNumbers, conn)
            }

            call.respond(HttpStatusCode.Created, ticket)
        }

        get("/my") {
            val user = call.requireUserAuth(userDao, jwtSecret)
            val tickets = ticketDao.findByUserId(user.id)
            call.respond(HttpStatusCode.OK, tickets)
        }

        delete("/{id}") {
            val user = call.requireUserAuth(userDao, jwtSecret)
            val ticketId = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid ticket id")

            dbQuery { conn ->
                val ticket = ticketDao.findById(ticketId)
                    ?: throw NoSuchElementException("Ticket not found")
                if (ticket.userId != user.id) throw SecurityException("Access denied")
                if (ticket.status == "cancelled") throw IllegalStateException("Ticket already cancelled")

                val route = routeDao.findById(ticket.routeId)
                    ?: throw NoSuchElementException("Route not found")
                val departure = Instant.parse(route.departureTime)
                val hoursUntilDeparture = ChronoUnit.HOURS.between(Instant.now(), departure)
                if (hoursUntilDeparture < 2) {
                    throw IllegalStateException("Cannot cancel ticket less than 2 hours before departure")
                }

                val result = ticketDao.cancelWithReturn(ticketId, user.id, conn)
                    ?: throw NoSuchElementException("Ticket not found or already cancelled")
                routeDao.incrementSeats(result.second, result.first, conn)
            }

            call.respond(HttpStatusCode.OK, mapOf("message" to "Ticket cancelled successfully"))
        }
    }
}
