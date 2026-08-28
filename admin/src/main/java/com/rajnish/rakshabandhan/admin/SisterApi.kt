package com.rajnish.rakshabandhan.admin

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

internal data class SisterOption(val id: String, val name: String, val email: String)
internal data class SisterListResult(val success: Boolean, val message: String, val sisters: List<SisterOption>)

internal object SisterApi {
    fun getSisters(adminApiKey: String): SisterListResult {
        val backendUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
        val connection = (URL("$backendUrl/admin/sisters").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Admin-Api-Key", adminApiKey.trim())
        }
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { org.json.JSONObject(body) }.getOrNull()
            if (status !in 200..299 || json == null) {
                SisterListResult(false, json?.optString("message") ?: "Unable to load sisters.", emptyList())
            } else {
                val array = json.optJSONArray("sisters") ?: JSONArray()
                val result = buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val id = item.optString("sisterId")
                        if (id.isBlank()) continue
                        add(SisterOption(id, item.optString("name", "Sister"), item.optString("email", "")))
                    }
                }
                SisterListResult(true, "", result)
            }
        } catch (error: Exception) {
            SisterListResult(false, "Unable to reach $backendUrl. ${error.message ?: "Check the backend connection."}", emptyList())
        } finally {
            connection.disconnect()
        }
    }
}
