package com.rajnish.rakshabandhan.admin

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class InvitationResult(
    val success: Boolean,
    val message: String,
)

internal object InvitationApi {
    fun createInvitation(sisterId: String, email: String): InvitationResult {
        val connection = (URL("${BuildConfig.BACKEND_BASE_URL}/invitations").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val payload = JSONObject()
                .put("sisterId", sisterId)
                .put("email", email)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrNull()
            val message = json?.optString("message")?.takeIf { it.isNotBlank() }
                ?: if (statusCode in 200..299) "Invitation sent successfully." else "Unable to send invitation."

            InvitationResult(statusCode in 200..299, message)
        } catch (error: Exception) {
            InvitationResult(false, "Unable to reach the backend. Check the network and try again.")
        } finally {
            connection.disconnect()
        }
    }
}
