package com.rajnish.rakshabandhan.features.invitation.domain

/**
 * Server-side invitation record used to provision a sister identity.
 *
 * The raw invitation code is never part of this model. Only its SHA-256
 * digest is retained so the verifier does not need to persist the secret.
 */
data class Invitation(
    val invitationId: String,
    val sisterId: String,
    val codeHash: String,
    val createdAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val consumedAtEpochMillis: Long? = null
) {
    fun isExpired(nowEpochMillis: Long = System.currentTimeMillis()): Boolean =
        nowEpochMillis >= expiresAtEpochMillis

    fun isConsumed(): Boolean = consumedAtEpochMillis != null

    fun isActive(nowEpochMillis: Long = System.currentTimeMillis()): Boolean =
        !isConsumed() && !isExpired(nowEpochMillis)
}
