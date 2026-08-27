package com.rajnish.rakshabandhan.features.auth.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.rajnish.rakshabandhan.features.auth.domain.AuthRepository
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore(
    name = "auth_preferences"
)

private val AUTHENTICATED_KEY = booleanPreferencesKey("authenticated")

class AuthRepositoryImpl(
    private val context: Context
) : AuthRepository {

    override suspend fun hasAuthenticatedSession(): Boolean {
        return context.authDataStore.data
            .first()[AUTHENTICATED_KEY] ?: false
    }

    override suspend fun saveAuthenticatedSession() {
        context.authDataStore.edit { preferences ->
            preferences[AUTHENTICATED_KEY] = true
        }
    }

    override suspend fun clearAuthenticatedSession() {
        context.authDataStore.edit { preferences ->
            preferences[AUTHENTICATED_KEY] = false
        }
    }
}