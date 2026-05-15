package com.transport.server.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import java.io.FileInputStream
import java.io.InputStream

object FirebaseService {

    fun init(credentialsFile: String? = null, projectId: String? = null) {
        if (FirebaseApp.getApps().isNotEmpty()) return

        val credentials: GoogleCredentials = if (credentialsFile != null) {
            FileInputStream(credentialsFile).use { GoogleCredentials.fromStream(it) }
        } else {
            // Try GOOGLE_APPLICATION_CREDENTIALS env var or default credentials
            val envFile = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
            if (envFile != null) {
                FileInputStream(envFile).use { GoogleCredentials.fromStream(it) }
            } else {
                GoogleCredentials.getApplicationDefault()
            }
        }

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .apply { if (projectId != null) setProjectId(projectId) }
            .build()

        FirebaseApp.initializeApp(options)
    }

    fun verifyToken(idToken: String): FirebaseToken {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: Exception) {
            throw SecurityException("Invalid Firebase token: ${e.message}")
        }
    }
}
