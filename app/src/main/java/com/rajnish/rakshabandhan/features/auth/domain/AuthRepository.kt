package com.rajnish.rakshabandhan.features.auth.domain

interface AuthRepository {

    suspend fun hasFirebaseSession(): Boolean

    suspend fun hasDeviceKey(): Boolean

    suspend fun verifyInvitation(email: String, code: String): Result<Unit>

    suspend fun requestDeviceChallenge(): Result<DeviceChallenge>

    suspend fun completeDeviceLogin(challengeId: String, signature: ByteArray): Result<Unit>

    suspend fun clearAuthenticatedSession()
}

data class DeviceChallenge(
    val challengeId: String,
    val challenge: String,
)
