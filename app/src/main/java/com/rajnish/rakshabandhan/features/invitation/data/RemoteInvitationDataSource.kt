package com.rajnish.rakshabandhan.features.invitation.data

import com.rajnish.rakshabandhan.features.invitation.domain.Invitation

/**
 * Remote boundary shared by the admin and sister clients.
 *
 * Implementations must keep invitation-code verification server-side and
 * must never expose the stored code hash to the client.
 */
interface RemoteInvitationDataSource {

    suspend fun createInvitation(
        sisterId: String,
        name: String,
        email: String,
        codeHash: String,
        createdAtEpochMillis: Long,
        expiresAtEpochMillis: Long
    ): Invitation

    /**
     * Finds and atomically consumes a pending invitation matching the email
     * and submitted code hash. Returns null when no valid invitation exists.
     */
    suspend fun verifyAndConsumeInvitation(
        email: String,
        codeHash: String,
        nowEpochMillis: Long
    ): Invitation?
}
