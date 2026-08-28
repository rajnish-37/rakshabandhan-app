package com.rajnish.rakshabandhan.features.home.data

import com.rajnish.rakshabandhan.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ClaimSuccess(val claim: GiftClaimData?)

sealed interface ClaimResult {
    data class Success(val claim: GiftClaimData?) : ClaimResult
    data class Failure(val message: String) : ClaimResult
}

class ClaimApi {
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
            payload?.let { body -> connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) } }
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
