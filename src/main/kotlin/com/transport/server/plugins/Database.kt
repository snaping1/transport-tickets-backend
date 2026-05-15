package com.transport.server.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import java.sql.Connection

object DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun init(jdbcUrl: String, maxPoolSize: Int = 10) {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.maximumPoolSize = maxPoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        dataSource = HikariDataSource(config)
        runMigrations()
    }

    private fun runMigrations() {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }

    fun <T> dbQuery(block: (Connection) -> T): T {
        dataSource.connection.use { connection ->
            return try {
                val result = block(connection)
                connection.commit()
                result
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }
}

fun Application.configureDatabase() {
    val jdbcUrl = environment.config.propertyOrNull("database.jdbcUrl")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: error("DATABASE_URL is not configured")
    DatabaseFactory.init(jdbcUrl)
}
