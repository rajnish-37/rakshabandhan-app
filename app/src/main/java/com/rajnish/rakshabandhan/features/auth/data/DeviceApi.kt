package com.rajnish.rakshabandhan.features.auth.data

import com.rajnish.rakshabandhan.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal class DeviceApi {

    fun registerDeviceKey(idToken: String, keyId: String, publicKey: String) {
        val connection = (URL("${BuildConfig.BACKEND_BASE_URL}/devices/register")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $idToken")
        }

        try {
            val payload = JSONObject()
                .put("keyId", keyId)
                .put("publicKey", publicKey)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val responseText = (if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (connection.responseCode !in 200..299) {
                val response = runCatching { JSONObject(responseText) }.getOrNull()
                throw IllegalStateException(
                    response?.optString("message", "Device key registration failed")
                        ?: "Device key registration failed"
                )
            }
        } finally {
            connection.disconnect()
        }
    }
}
