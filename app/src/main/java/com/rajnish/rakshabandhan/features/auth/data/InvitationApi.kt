package com.rajnish.rakshabandhan.features.auth.data

import com.rajnish.rakshabandhan.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class InvitationSession(
    val invitationId: String,
    val sisterId: String,
    val email: String,
    val authUid: String,
    val customToken: String,
)

internal class InvitationApi {

    fun verifyInvitation(email: String, code: String): InvitationSession {
        val connection = (URL("${BuildConfig.BACKEND_BASE_URL}/invitations/verify")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val payload = JSONObject()
                .put("email", email.trim())
                .put("code", code.trim().uppercase())
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val responseText = (if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader()?.use { it.readText() }.orEmpty()

            val response = runCatching { JSONObject(responseText) }
                .getOrElse { throw IllegalStateException("Invalid server response") }

            if (connection.responseCode !in 200..299) {
                throw IllegalStateException(
                    response.optString("message", "Invitation verification failed")
                )
            }

            if (response.optString("status") != "verified") {
                throw IllegalStateException("Invitation verification failed")
            }

            val customToken = response.optString("customToken")
            if (customToken.isBlank()) {
                throw IllegalStateException("Server did not return an authentication token")
            }

            return InvitationSession(
                invitationId = response.getString("invitationId"),
                sisterId = response.getString("sisterId"),
                email = response.getString("email"),
                authUid = response.getString("authUid"),
                customToken = customToken,
            )
        } finally {
            connection.disconnect()
        }
    }
}
