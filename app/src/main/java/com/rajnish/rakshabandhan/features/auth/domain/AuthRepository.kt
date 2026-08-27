package com.rajnish.rakshabandhan.features.auth.domain

interface AuthRepository {

    suspend fun hasFirebaseSession(): Boolean

    suspend fun verifyInvitation(email: String, code: String): Result<Unit>

    suspend fun clearAuthenticatedSession()
}
