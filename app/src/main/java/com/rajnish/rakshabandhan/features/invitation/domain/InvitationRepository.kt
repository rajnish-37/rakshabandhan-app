package com.rajnish.rakshabandhan.features.invitation.domain

/**
 * Application-facing contract for invitation operations.
 *
 * The concrete implementation will later be backed by the remote service
 * shared by the admin and sister applications/devices.
 */
interface InvitationRepository {

    /**
     * Creates an invitation for the supplied sister and returns the one-time
     * raw code to be delivered to the sister.
     */
    suspend fun createInvitation(
        sisterId: String,
        name: String,
        email: String
    ): InvitationCreationResult

    /**
     * Verifies the supplied email + code against the remote invitation state.
     * A successful verification consumes the invitation atomically.
     */
    suspend fun verifyAndConsumeInvitation(
        email: String,
        code: String
    ): InvitationVerificationResult
}

sealed interface InvitationCreationResult {
    data class Success(
        val invitation: Invitation,
        val rawCode: String
    ) : InvitationCreationResult

    data class Failure(val reason: InvitationFailureReason) : InvitationCreationResult
}

sealed interface InvitationVerificationResult {
    data class Success(val invitation: Invitation) : InvitationVerificationResult
    data class Failure(val reason: InvitationFailureReason) : InvitationVerificationResult
}

enum class InvitationFailureReason {
    INVALID_CODE,
    EXPIRED,
    ALREADY_CONSUMED,
    NETWORK_ERROR,
    SERVER_ERROR
}
