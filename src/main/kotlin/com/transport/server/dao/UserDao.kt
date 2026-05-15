package com.transport.server.dao

import com.transport.server.models.User
import com.transport.server.plugins.DatabaseFactory.dbQuery
import java.sql.ResultSet

class UserDao {

    private fun ResultSet.toUser() = User(
        id = getInt("id"),
        firebaseUid = getString("firebase_uid"),
        email = getString("email"),
        createdAt = getString("created_at")
    )

    fun findByFirebaseUid(firebaseUid: String): User? = dbQuery { conn ->
        conn.prepareStatement("SELECT * FROM users WHERE firebase_uid = ?").use { stmt ->
            stmt.setString(1, firebaseUid)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.toUser() else null
            }
        }
    }

    fun createOrUpdate(firebaseUid: String, email: String): User = dbQuery { conn ->
        val existing = conn.prepareStatement("SELECT * FROM users WHERE firebase_uid = ?").use { stmt ->
            stmt.setString(1, firebaseUid)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.toUser() else null
            }
        }
        if (existing != null) {
            conn.prepareStatement("UPDATE users SET email = ? WHERE firebase_uid = ? RETURNING *").use { stmt ->
                stmt.setString(1, email)
                stmt.setString(2, firebaseUid)
                stmt.executeQuery().use { rs ->
                    rs.next()
                    rs.toUser()
                }
            }
        } else {
            conn.prepareStatement(
                "INSERT INTO users (firebase_uid, email) VALUES (?, ?) RETURNING *"
            ).use { stmt ->
                stmt.setString(1, firebaseUid)
                stmt.setString(2, email)
                stmt.executeQuery().use { rs ->
                    rs.next()
                    rs.toUser()
                }
            }
        }
    }
}
