package com.rajnish.rakshabandhan.features.identity.data

import com.rajnish.rakshabandhan.features.identity.domain.DeviceBinding

/**
 * Local persistence boundary for the provisioned identity/device binding.
 *
 * The concrete encrypted/secure storage implementation will be introduced
 * in the next step. Keeping the boundary separate prevents identity logic
 * from depending directly on the storage mechanism.
 */
interface IdentityLocalDataSource {

    suspend fun getDeviceBinding(): DeviceBinding?

    suspend fun saveDeviceBinding(binding: DeviceBinding)

    suspend fun clearDeviceBinding()
}
