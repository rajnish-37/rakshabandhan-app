package com.rajnish.rakshabandhan.features.identity.domain

/**
 * Application-facing operations for the locally provisioned sister identity.
 */
interface IdentityRepository {

    suspend fun getDeviceBinding(): DeviceBinding?

    suspend fun bindDevice(identity: SisterIdentity)

    suspend fun clearDeviceBinding()
}
