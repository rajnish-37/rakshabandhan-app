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
    val claim: GiftClaimData?,
)

data class GiftData(
    val giftId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val claimEligible: Boolean,
    val claimDeadline: String?,
)

data class GiftClaimData(
    val claimId: String,
    val sisterId: String,
    val sisterName: String,
    val sisterEmail: String,
    val giftId: String,
    val amount: Double,
    val currency: String,
    val upiId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val paidAt: String?,
    val paidBy: String?,
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
            val response = runCatching { JSONObject(responseText) }.getOrElse { throw IllegalStateException("Invalid server response") }

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
            val claimJson = response.optJSONObject("claim")
            val claim = claimJson?.let {
                GiftClaimData(
                    claimId = it.getString("claimId"),
                    sisterId = it.getString("sisterId"),
                    sisterName = it.getString("sisterName"),
                    sisterEmail = it.getString("sisterEmail"),
                    giftId = it.getString("giftId"),
                    amount = it.getDouble("amount"),
                    currency = it.getString("currency"),
                    upiId = it.getString("upiId"),
                    status = it.getString("status"),
                    createdAt = it.getString("createdAt"),
                    updatedAt = it.getString("updatedAt"),
                    paidAt = it.optString("paidAt").takeIf { value -> value.isNotBlank() && value != "null" },
                    paidBy = it.optString("paidBy").takeIf { value -> value.isNotBlank() && value != "null" },
                )
            }

            return SisterHomeData(
                sisterId = response.getString("sisterId"),
                email = response.getString("email"),
                name = response.optString("name", "Sister").ifBlank { "Sister" },
                enrollmentStatus = response.optString("enrollmentStatus", "ACTIVE"),
                gift = gift,
                claim = claim,
            )
        } finally {
            connection.disconnect()
        }
    }
}
