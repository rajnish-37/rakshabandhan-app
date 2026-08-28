package com.rajnish.rakshabandhan.features.identity.data

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.rajnish.rakshabandhan.features.identity.domain.DeviceBinding
import com.rajnish.rakshabandhan.features.identity.domain.SisterIdentity
import kotlinx.coroutines.flow.first

private val Context.identityDataStore by preferencesDataStore(
    name = "identity_preferences"
)

private val SISTER_ID_KEY = stringPreferencesKey("sister_id")
private val SISTER_NAME_KEY = stringPreferencesKey("sister_name")
private val SISTER_EMAIL_KEY = stringPreferencesKey("sister_email")
private val BOUND_AT_KEY = longPreferencesKey("bound_at_epoch_millis")

/**
 * DataStore-backed implementation for the local identity/device-binding state.
 *
 * Invitation codes and cryptographic secrets are intentionally not persisted
 * by this data source. They belong to later, security-specific flows.
 */
class IdentityLocalDataSourceImpl(
    private val context: Context
) : IdentityLocalDataSource {

    override suspend fun getDeviceBinding(): DeviceBinding? {
        val preferences = context.identityDataStore.data.first()

        val sisterId = preferences[SISTER_ID_KEY] ?: return null
        val name = preferences[SISTER_NAME_KEY] ?: return null
        val email = preferences[SISTER_EMAIL_KEY] ?: return null
        val boundAt = preferences[BOUND_AT_KEY] ?: return null

        return DeviceBinding(
            sisterIdentity = SisterIdentity(
                sisterId = sisterId,
                name = name,
                email = email
            ),
            boundAtEpochMillis = boundAt
        )
    }

    override suspend fun saveDeviceBinding(binding: DeviceBinding) {
        context.identityDataStore.edit { preferences ->
            preferences[SISTER_ID_KEY] = binding.sisterIdentity.sisterId
            preferences[SISTER_NAME_KEY] = binding.sisterIdentity.name
            preferences[SISTER_EMAIL_KEY] = binding.sisterIdentity.email
            preferences[BOUND_AT_KEY] = binding.boundAtEpochMillis
        }
    }

    override suspend fun clearDeviceBinding() {
        context.identityDataStore.edit { preferences ->
            preferences.remove(SISTER_ID_KEY)
            preferences.remove(SISTER_NAME_KEY)
            preferences.remove(SISTER_EMAIL_KEY)
            preferences.remove(BOUND_AT_KEY)
        }
    }
}
