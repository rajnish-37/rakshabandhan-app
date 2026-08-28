package com.rajnish.rakshabandhan.features.identity.data

import com.rajnish.rakshabandhan.features.identity.domain.DeviceBinding
import com.rajnish.rakshabandhan.features.identity.domain.IdentityRepository
import com.rajnish.rakshabandhan.features.identity.domain.SisterIdentity

class IdentityRepositoryImpl(
    private val localDataSource: IdentityLocalDataSource
) : IdentityRepository {

    override suspend fun getDeviceBinding(): DeviceBinding? {
        return localDataSource.getDeviceBinding()
    }

    override suspend fun bindDevice(identity: SisterIdentity) {
        localDataSource.saveDeviceBinding(
            DeviceBinding(
                sisterIdentity = identity,
                boundAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearDeviceBinding() {
        localDataSource.clearDeviceBinding()
    }
}
