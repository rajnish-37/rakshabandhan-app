package com.rajnish.rakshabandhan.features.identity.domain

/**
 * Identity provisioned for the sister during invitation enrollment.
 *
 * This model contains identity metadata only. Invitation secrets and
 * cryptographic material must not be stored in this model.
 */
data class SisterIdentity(
    val sisterId: String,
    val name: String,
    val email: String
)
