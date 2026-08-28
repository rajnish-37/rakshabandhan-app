package com.rajnish.rakshabandhan

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.rajnish.rakshabandhan.core.security.BiometricAuthenticator
import com.rajnish.rakshabandhan.core.security.DeviceKeyManager
import com.rajnish.rakshabandhan.features.auth.data.AuthRepositoryImpl
import com.rajnish.rakshabandhan.features.auth.domain.AuthState
import com.rajnish.rakshabandhan.features.auth.presentation.AuthScreen
import com.rajnish.rakshabandhan.features.auth.presentation.AuthViewModel
import com.rajnish.rakshabandhan.features.auth.presentation.AuthViewModelFactory
import com.rajnish.rakshabandhan.ui.theme.RakshaBandhanTheme

private const val SCREEN_TWO_PREVIEW = true

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthenticator: BiometricAuthenticator
    private lateinit var deviceKeyManager: DeviceKeyManager
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        biometricAuthenticator = BiometricAuthenticator()
        deviceKeyManager = DeviceKeyManager()

        val authRepository = AuthRepositoryImpl()
        val authViewModelFactory = AuthViewModelFactory(authRepository = authRepository)
        authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        setContent {
            RakshaBandhanTheme {
                val uiState by authViewModel.uiState.collectAsState()

                if (SCREEN_TWO_PREVIEW && uiState.authState == AuthState.Initializing) {
                    // The real auth state machine remains intact; this condition only
                    // waits for its initial state while the preview screen is rendered.
                }

                AuthScreen(
                    uiState = uiState,
                    onEmailChanged = authViewModel::updateEmail,
                    onCodeChanged = authViewModel::updateCode,
                    onVerifyInvitation = authViewModel::verifyInvitation,
                    onAuthenticate = {
                        if (!biometricAuthenticator.canAuthenticate(this)) {
                            authViewModel.onAuthenticationError(
                                "Strong biometric authentication is not available on this device."
                            )
                            return@AuthScreen
                        }

                        if (!deviceKeyManager.hasKey()) {
                            authViewModel.onAuthenticationError(
                                "This device is not enrolled. Complete invitation verification first."
                            )
                            return@AuthScreen
                        }

                        authViewModel.prepareDeviceLogin { challengeId, challenge ->
                            try {
                                val signature = deviceKeyManager.createSignature()
                                biometricAuthenticator.authenticateForSignature(
                                    activity = this,
                                    signature = signature,
                                    challenge = challenge,
                                    onSuccess = { signedChallenge ->
                                        authViewModel.completeDeviceLogin(
                                            challengeId = challengeId,
                                            signature = signedChallenge,
                                        )
                                    },
                                    onError = authViewModel::onAuthenticationError,
                                )
                            } catch (e: Exception) {
                                authViewModel.onAuthenticationError(
                                    e.message ?: "Unable to prepare device authentication"
                                )
                            }
                        }
                    },
                    onRetry = authViewModel::resetError,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::authViewModel.isInitialized) {
            authViewModel.onAppForegrounded()
        }
    }
}
