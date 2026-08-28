package com.rajnish.rakshabandhan.core.security

import android.os.Build
import android.util.Base64
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

class DeviceKeyManager {

    data class RegistrationKey(
        val keyId: String,
        val publicKey: String,
    )

    private val alias = "raksha_device_signing_key"

    fun hasKey(): Boolean = loadKeyPair() != null

    fun ensureKey(): RegistrationKey {
        val keyPair = loadKeyPair() ?: generateKeyPair()
        return RegistrationKey(
            keyId = keyId(keyPair.public),
            publicKey = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP),
        )
    }

    fun createSignature(): Signature {
        val privateKey = loadKeyPair()?.private
            ?: throw IllegalStateException("Device signing key is not enrolled")

        return Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
        }
    }

    private fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore",
        )

        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN,
        )
            .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG,
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(0)
        }

        generator.initialize(builder.build())
        return generator.generateKeyPair()
    }

    private fun loadKeyPair(): KeyPair? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        val privateKey = keyStore.getKey(alias, null) as? PrivateKey ?: return null
        val publicKey = keyStore.getCertificate(alias)?.publicKey ?: return null
        return KeyPair(publicKey, privateKey)
    }

    private fun keyId(publicKey: PublicKey): String =
        MessageDigest.getInstance("SHA-256")
            .digest(publicKey.encoded)
            .joinToString("") { "%02x".format(it) }
}
