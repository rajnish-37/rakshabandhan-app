package com.rajnish.rakshabandhan.features.auth.domain

interface AuthRepository {

    suspend fun hasAuthenticatedSession(): Boolean

    suspend fun saveAuthenticatedSession()

    suspend fun clearAuthenticatedSession()
}