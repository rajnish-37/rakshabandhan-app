package com.rajnish.rakshabandhan.features.auth.data

import com.rajnish.rakshabandhan.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class DeviceChallenge(
    val challengeId: String,
    val challenge: String,
)

internal data class DeviceLoginSession(
    val authUid: String,
    val sisterId: String,
    val customToken: String,
)

internal class DeviceApi {

    fun requestChallenge(keyId: String): DeviceChallenge {
        val connection = openConnection("/devices/challenge", "POST")
        try {
            writeJson(connection, JSONObject().put("keyId", keyId))
            val response = readJson(connection)
            ensureSuccess(connection, response, "Unable to create device challenge")

            if (response.optString("status") != "challenge") {
                throw IllegalStateException("Invalid device challenge response")
            }

            return DeviceChallenge(
                challengeId = response.getString("challengeId"),
                challenge = response.getString("challenge"),
            )
        } finally {
            connection.disconnect()
        }
    }

    fun verifyChallenge(
        keyId: String,
        challengeId: String,
        signature: ByteArray,
    ): DeviceLoginSession {
        val connection = openConnection("/devices/challenge/verify", "POST")
        try {
            val signatureBase64 = android.util.Base64.encodeToString(
                signature,
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING,
            )
            writeJson(
                connection,
                JSONObject()
                    .put("keyId", keyId)
                    .put("challengeId", challengeId)
                    .put("signature", signatureBase64),
            )

            val response = readJson(connection)
            ensureSuccess(connection, response, "Unable to authenticate device")

            if (response.optString("status") != "authenticated") {
                throw IllegalStateException("Device authentication failed")
            }

            val customToken = response.optString("customToken")
            if (customToken.isBlank()) {
                throw IllegalStateException("Server did not return an authentication token")
            }

            return DeviceLoginSession(
                authUid = response.getString("authUid"),
                sisterId = response.getString("sisterId"),
                customToken = customToken,
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(path: String, method: String): HttpURLConnection =
        (URL("${BuildConfig.BACKEND_BASE_URL}$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

    private fun writeJson(connection: HttpURLConnection, payload: JSONObject) {
        connection.outputStream.use { output ->
            output.write(payload.toString().toByteArray(Charsets.UTF_8))
        }
    }

    private fun readJson(connection: HttpURLConnection): JSONObject {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        return runCatching { JSONObject(text) }
            .getOrElse { throw IllegalStateException("Invalid server response") }
    }

    private fun ensureSuccess(connection: HttpURLConnection, response: JSONObject, fallback: String) {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(response.optString("message", fallback))
        }
    }
}
