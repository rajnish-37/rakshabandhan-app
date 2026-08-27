package com.rajnish.rakshabandhan.features.auth.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.rajnish.rakshabandhan.core.security.DeviceKeyManager
import com.rajnish.rakshabandhan.features.auth.domain.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl internal constructor(
    private val invitationApi: InvitationApi = InvitationApi(),
    private val deviceApi: DeviceApi = DeviceApi(),
    private val deviceKeyManager: DeviceKeyManager = DeviceKeyManager(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    override suspend fun hasFirebaseSession(): Boolean {
        return firebaseAuth.currentUser != null
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

                val idToken = Tasks.await(signedInUser.getIdToken(true)).token
                    ?: error("Firebase ID token was unavailable")

                val registrationKey = deviceKeyManager.ensureKey()
                deviceApi.registerDeviceKey(
                    idToken = idToken,
                    keyId = registrationKey.keyId,
                    publicKey = registrationKey.publicKey,
                )
            }
        }
    }

    override suspend fun clearAuthenticatedSession() {
        firebaseAuth.signOut()
    }
}
