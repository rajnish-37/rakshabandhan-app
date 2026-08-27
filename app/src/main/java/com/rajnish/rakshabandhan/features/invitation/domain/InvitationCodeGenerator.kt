package com.rajnish.rakshabandhan.features.invitation.domain

import java.security.SecureRandom

/**
 * Generates human-enterable invitation codes using a cryptographically
 * secure random source.
 *
 * The alphabet deliberately omits visually ambiguous characters such as
 * 0, O, 1 and I.
 */
class InvitationCodeGenerator(
    private val secureRandom: SecureRandom = SecureRandom()
) {

    companion object {
        private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private const val CODE_LENGTH = 16
    }

    fun generate(): String = buildString(CODE_LENGTH) {
        repeat(CODE_LENGTH) {
            append(ALPHABET[secureRandom.nextInt(ALPHABET.length)])
        }
    }
}
