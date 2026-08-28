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

internal sealed interface ClaimResult {
    data class Success(val claim: GiftClaimData?) : ClaimResult
    data class Failure(val message: String) : ClaimResult
}

internal class ClaimApi {
    fun getMyClaim(idToken: String): ClaimResult {
        val connection = openConnection("/claims/me", "GET", idToken)
        return execute(connection, null) { json ->
            ClaimResult.Success(json.optJSONObject("claim")?.let(::parseClaim))
        }
    }

    fun submitClaim(idToken: String, upiId: String): ClaimResult {
        val connection = openConnection("/claims", "POST", idToken).apply { doOutput = true }
        val payload = JSONObject().put("upiId", upiId.trim()).toString()
        return execute(connection, payload) { json ->
            ClaimResult.Success(json.optJSONObject("claim")?.let(::parseClaim))
        }
    }

    private fun openConnection(path: String, method: String, idToken: String): HttpURLConnection =
        (URL("${BuildConfig.BACKEND_BASE_URL}$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $idToken")
            if (method == "POST") setRequestProperty("Content-Type", "application/json")
        }

    private fun execute(
        connection: HttpURLConnection,
        payload: String?,
        parser: (JSONObject) -> ClaimResult,
    ): ClaimResult {
        return try {
            payload?.let { connection.outputStream.use { stream -> stream.write(it.toByteArray(Charsets.UTF_8)) } }
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrNull()
            if (statusCode !in 200..299 || json == null) {
                ClaimResult.Failure(json?.optString("message")?.takeIf { it.isNotBlank() } ?: "Unable to process gift claim.")
            } else parser(json)
        } catch (_: Exception) {
            ClaimResult.Failure("Unable to reach the backend. Please try again.")
        } finally {
            connection.disconnect()
        }
    }

    private fun parseClaim(json: JSONObject): GiftClaimData = GiftClaimData(
        claimId = json.getString("claimId"),
        sisterId = json.getString("sisterId"),
        sisterName = json.getString("sisterName"),
        sisterEmail = json.getString("sisterEmail"),
        giftId = json.getString("giftId"),
        amount = json.getDouble("amount"),
        currency = json.getString("currency"),
        upiId = json.getString("upiId"),
        status = json.getString("status"),
        createdAt = json.getString("createdAt"),
        updatedAt = json.getString("updatedAt"),
        paidAt = json.optString("paidAt").takeIf { it.isNotBlank() && it != "null" },
        paidBy = json.optString("paidBy").takeIf { it.isNotBlank() && it != "null" },
    )
}

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
