package com.rajnish.rakshabandhan.features.home.data

import com.rajnish.rakshabandhan.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SisterHomeData(
    val sisterId: String,
    val email: String,
    val name: String,
    val enrollmentStatus: String,
    val gift: GiftData?,
)

data class GiftData(
    val giftId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val claimEligible: Boolean,
    val claimDeadline: String?,
)

class HomeApi {
    fun getMe(idToken: String): SisterHomeData {
        val connection = (URL("${BuildConfig.BACKEND_BASE_URL}/me").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $idToken")
        }

        try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val response = runCatching { JSONObject(responseText) }
                .getOrElse { throw IllegalStateException("Invalid server response") }

            if (statusCode == 401) throw SecurityException("Authentication required")
            if (statusCode !in 200..299) throw IllegalStateException(response.optString("message", "Unable to load home"))

            val giftJson = response.optJSONObject("gift")
            val gift = giftJson?.let {
                GiftData(
                    giftId = it.getString("giftId"),
                    amount = it.getDouble("amount"),
                    currency = it.getString("currency"),
                    status = it.getString("status"),
                    claimEligible = it.getBoolean("claimEligible"),
                    claimDeadline = it.optString("claimDeadline").takeIf(String::isNotBlank),
                )
            }

            return SisterHomeData(
                sisterId = response.getString("sisterId"),
                email = response.getString("email"),
                name = response.optString("name", "Sister").ifBlank { "Sister" },
                enrollmentStatus = response.optString("enrollmentStatus", "ACTIVE"),
                gift = gift,
            )
        } finally {
            connection.disconnect()
        }
    }
}
