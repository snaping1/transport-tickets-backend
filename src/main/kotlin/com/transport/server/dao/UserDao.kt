package com.transport.server.dao

import com.transport.server.models.User
import com.transport.server.plugins.DatabaseFactory.dbQuery
import org.mindrot.jbcrypt.BCrypt
import java.sql.ResultSet

class UserDao {

    private fun ResultSet.toUser() = User(
        id = getInt("id"),
        firebaseUid = getString("firebase_uid"),
        email = getString("email"),
        createdAt = getString("created_at")
    )

    fun findById(id: Int): User? = dbQuery { conn ->
        conn.prepareStatement("SELECT * FROM users WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs -> if (rs.next()) rs.toUser() else null }
        }
    }

    fun findByEmail(email: String): User? = dbQuery { conn ->
        conn.prepareStatement("SELECT * FROM users WHERE email = ?").use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs -> if (rs.next()) rs.toUser() else null }
        }
    }

    fun register(email: String, password: String): User = dbQuery { conn ->
        val existing = conn.prepareStatement("SELECT id FROM users WHERE email = ?").use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs -> rs.next() }
        }
        if (existing) throw IllegalArgumentException("Email already registered")
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        conn.prepareStatement(
            "INSERT INTO users (email, password_hash) VALUES (?, ?) RETURNING *"
        ).use { stmt ->
            stmt.setString(1, email)
            stmt.setString(2, hash)
            stmt.executeQuery().use { rs -> rs.next(); rs.toUser() }
        }
    }

    fun login(email: String, password: String): User = dbQuery { conn ->
        val (user, hash) = conn.prepareStatement(
            "SELECT *, password_hash FROM users WHERE email = ?"
        ).use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) throw SecurityException("Invalid email or password")
                Pair(rs.toUser(), rs.getString("password_hash"))
            }
        }
        if (hash == null || !BCrypt.checkpw(password, hash))
            throw SecurityException("Invalid email or password")
        user
    }
}
