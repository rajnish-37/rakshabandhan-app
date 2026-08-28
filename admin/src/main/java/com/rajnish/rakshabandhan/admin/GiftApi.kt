package com.rajnish.rakshabandhan.admin

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class GiftData(
    val amount: String,
    val currency: String,
    val status: String,
    val claimEligible: Boolean,
)

internal data class GiftResult(
    val success: Boolean,
    val message: String,
    val gift: GiftData? = null,
)

internal object GiftApi {
    fun getGift(sisterId: String, adminApiKey: String): GiftResult {
        val backendUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
        val connection = (URL("$backendUrl/admin/sisters/$sisterId/gift").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Admin-Api-Key", adminApiKey.trim())
        }
        return try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrNull()
            if (statusCode !in 200..299 || json == null) {
                GiftResult(false, json?.optString("message")?.takeIf { it.isNotBlank() } ?: "Unable to load gift.")
            } else {
                val gift = json.optJSONObject("gift")
                if (gift == null) GiftResult(true, "No gift configured for this sister.")
                else GiftResult(true, "", GiftData(gift.optString("amount", ""), gift.optString("currency", "INR"), gift.optString("status", "PENDING"), gift.optBoolean("claimEligible", false)))
            }
        } catch (error: Exception) {
            GiftResult(false, "Unable to reach $backendUrl. ${error.message ?: "Check the backend connection."}")
        } finally { connection.disconnect() }
    }

    fun configureGift(sisterId: String, amount: String, eligible: Boolean, adminApiKey: String): GiftResult {
        val backendUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
        val connection = (URL("$backendUrl/admin/gift").openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Admin-Api-Key", adminApiKey.trim())
        }
        return try {
            val numericAmount = amount.toDoubleOrNull() ?: return GiftResult(false, "Enter a valid gift amount.")
            val status = if (eligible) "ELIGIBLE" else "PENDING"
            val payload = JSONObject().put("sisterId", sisterId).put("amount", numericAmount).put("currency", "INR").put("status", status).put("claimEligible", eligible).toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(body) }.getOrNull()
            val message = json?.optString("message")?.takeIf { it.isNotBlank() } ?: if (statusCode in 200..299) "Gift saved successfully." else "Unable to save gift."
            GiftResult(statusCode in 200..299, message)
        } catch (error: Exception) {
            val detail = error.message?.takeIf { it.isNotBlank() }
            GiftResult(false, if (detail != null) "Unable to reach $backendUrl. $detail" else "Unable to reach $backendUrl. Check the backend and try again.")
        } finally { connection.disconnect() }
    }
}
