package com.rajnish.rakshabandhan.features.auth.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.rajnish.rakshabandhan.core.security.DeviceKeyManager
import com.rajnish.rakshabandhan.features.auth.domain.AuthRepository
import com.rajnish.rakshabandhan.features.auth.domain.DeviceChallenge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl internal constructor(
    private val invitationApi: InvitationApi = InvitationApi(),
    private val deviceApi: DeviceApi = DeviceApi(),
    private val deviceKeyManager: DeviceKeyManager = DeviceKeyManager(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    override suspend fun hasFirebaseSession(): Boolean = firebaseAuth.currentUser != null

    override suspend fun hasDeviceKey(): Boolean =
        withContext(Dispatchers.IO) { deviceKeyManager.hasKey() }

    override suspend fun verifyInvitation(email: String, code: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val registrationKey = deviceKeyManager.ensureKey()
                invitationApi.verifyInvitation(
                    email = email,
                    code = code,
                    keyId = registrationKey.keyId,
                    publicKey = registrationKey.publicKey,
                )
                Unit
            }
        }

    override suspend fun requestDeviceChallenge(): Result<DeviceChallenge> =
        withContext(Dispatchers.IO) {
            runCatching {
                val key = deviceKeyManager.ensureKey()
                val challenge = deviceApi.requestChallenge(key.keyId)
                DeviceChallenge(
                    challengeId = challenge.challengeId,
                    challenge = challenge.challenge,
                )
            }
        }

    override suspend fun completeDeviceLogin(
        challengeId: String,
        signature: ByteArray,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val key = deviceKeyManager.ensureKey()
            val session = deviceApi.verifyChallenge(key.keyId, challengeId, signature)
            Tasks.await(firebaseAuth.signInWithCustomToken(session.customToken))

            val signedInUser = firebaseAuth.currentUser
                ?: error("Firebase session was not established")

            if (signedInUser.uid != session.authUid) {
                firebaseAuth.signOut()
                error("Firebase identity did not match the device identity")
            }
        }
    }

    override suspend fun refreshFirebaseSession(forceRefresh: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
                ?: return@withContext Result.success(Unit)

            runCatching {
                Tasks.await(user.getIdToken(forceRefresh))
                    ?: error("Firebase ID token was not available")
                Unit
            }
        }

    override suspend fun clearFirebaseSession() {
        firebaseAuth.signOut()
    }
}
