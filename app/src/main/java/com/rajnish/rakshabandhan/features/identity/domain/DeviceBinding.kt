package com.rajnish.rakshabandhan.features.identity.domain

/**
 * Represents the local binding between a provisioned sister identity and
 * this app installation.
 */
data class DeviceBinding(
    val sisterIdentity: SisterIdentity,
    val boundAtEpochMillis: Long
)
