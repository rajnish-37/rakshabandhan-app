package com.rajnish.rakshabandhan.admin

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class AdminClaim(
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
    val paidAt: String?,
    val paidBy: String?,
)

internal data class ClaimsResult(
    val success: Boolean,
    val message: String,
    val claims: List<AdminClaim> = emptyList(),
)

internal object ClaimApi {
    fun getPendingClaims(adminApiKey: String): ClaimsResult = request("/admin/claims/pending", "GET", adminApiKey, null)

    fun markPaid(adminApiKey: String, claimId: String): ClaimsResult =
        request("/admin/claims/mark-paid", "POST", adminApiKey, JSONObject().put("claimId", claimId).toString())

    private fun request(path: String, method: String, adminApiKey: String, body: String?): ClaimsResult {
        val backendUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
        val connection = (URL("$backendUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = body != null
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Admin-Api-Key", adminApiKey.trim())
            if (body != null) setRequestProperty("Content-Type", "application/json")
        }
        return try {
            body?.let { connection.outputStream.use { output -> output.write(it.toByteArray(Charsets.UTF_8)) } }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrNull()
            if (status !in 200..299 || json == null) {
                ClaimsResult(false, json?.optString("message")?.takeIf { it.isNotBlank() } ?: "Unable to process claims.")
            } else {
                val claims = buildList {
                    val array = json.optJSONArray("claims") ?: JSONArray().apply {
                        json.optJSONObject("claim")?.let { put(it) }
                    }
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        add(parseClaim(item))
                    }
                }
                ClaimsResult(true, json.optString("message"), claims)
            }
        } catch (error: Exception) {
            ClaimsResult(false, "Unable to reach $backendUrl. ${error.message ?: "Check the backend connection."}")
        } finally {
            connection.disconnect()
        }
    }

    private fun parseClaim(json: JSONObject) = AdminClaim(
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
        paidAt = json.optString("paidAt").takeIf { it.isNotBlank() && it != "null" },
        paidBy = json.optString("paidBy").takeIf { it.isNotBlank() && it != "null" },
    )
}
