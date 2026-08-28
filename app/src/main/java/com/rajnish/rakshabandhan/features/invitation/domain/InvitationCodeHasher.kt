package com.rajnish.rakshabandhan.features.invitation.domain

import java.security.MessageDigest

/**
 * Produces the canonical SHA-256 digest used to persist/compare invitation
 * codes without retaining the raw code.
 */
object InvitationCodeHasher {

    fun hash(code: String): String {
        val normalizedCode = code.trim().uppercase()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(normalizedCode.toByteArray(Charsets.UTF_8))

        return digest.joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    fun matches(code: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(
            hash(code).toByteArray(Charsets.UTF_8),
            expectedHash.lowercase().toByteArray(Charsets.UTF_8)
        )
}
