package com.rajnish.rakshabandhan.features.auth.data

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.rajnish.rakshabandhan.features.auth.domain.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

private val Context.authDataStore by preferencesDataStore(
    name = "auth_preferences"
)

private val AUTHENTICATED_KEY = booleanPreferencesKey("authenticated")

class AuthRepositoryImpl(
    private val context: Context,
    private val invitationApi: InvitationApi = InvitationApi(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    override suspend fun hasFirebaseSession(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun hasAuthenticatedSession(): Boolean {
        val locallyAuthenticated = context.authDataStore.data
            .first()[AUTHENTICATED_KEY] ?: false

        return locallyAuthenticated && firebaseAuth.currentUser != null
    }

    override suspend fun verifyInvitation(email: String, code: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val session = invitationApi.verifyInvitation(email, code)

                Tasks.await(firebaseAuth.signInWithCustomToken(session.customToken))

                val signedInUser = firebaseAuth.currentUser
                    ?: error("Firebase session was not established")

                if (signedInUser.uid != session.authUid) {
                    firebaseAuth.signOut()
                    error("Firebase identity did not match the invitation")
                }
            }
        }
    }

    override suspend fun saveAuthenticatedSession() {
        context.authDataStore.edit { preferences ->
            preferences[AUTHENTICATED_KEY] = true
        }
    }

    override suspend fun clearAuthenticatedSession() {
        firebaseAuth.signOut()

        context.authDataStore.edit { preferences ->
            preferences[AUTHENTICATED_KEY] = false
        }
    }
}
