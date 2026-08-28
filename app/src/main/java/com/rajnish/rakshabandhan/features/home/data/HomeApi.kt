package com.rajnish.rakshabandhan.features.home.data

import com.rajnish.rakshabandhan.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class SisterHomeData(
    val sisterId: String,
    val email: String,
    val name: String,
    val enrollmentStatus: String,
)

internal class HomeApi {
    fun getMe(idToken: String): SisterHomeData {
        val connection = (URL("${BuildConfig.BACKEND_BASE_URL}/me").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $idToken")
        }

        try {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val response = runCatching { JSONObject(responseText) }
                .getOrElse { throw IllegalStateException("Invalid server response") }

            if (connection.responseCode == 401) throw SecurityException("Authentication required")
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException(response.optString("message", "Unable to load home"))
            }

            return SisterHomeData(
                sisterId = response.getString("sisterId"),
                email = response.getString("email"),
                name = response.optString("name", "Sister").ifBlank { "Sister" },
                enrollmentStatus = response.optString("enrollmentStatus", "ACTIVE"),
            )
        } finally {
            connection.disconnect()
        }
    }
}
