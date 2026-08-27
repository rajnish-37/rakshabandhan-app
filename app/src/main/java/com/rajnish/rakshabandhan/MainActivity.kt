package com.rajnish.rakshabandhan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajnish.rakshabandhan.core.security.BiometricAuthenticator
import com.rajnish.rakshabandhan.features.auth.data.AuthRepositoryImpl
import com.rajnish.rakshabandhan.features.auth.presentation.AuthScreen
import com.rajnish.rakshabandhan.features.auth.presentation.AuthViewModel
import com.rajnish.rakshabandhan.features.auth.presentation.AuthViewModelFactory
import com.rajnish.rakshabandhan.ui.theme.RakshaBandhanTheme

class MainActivity : FragmentActivity() {

    private lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        biometricAuthenticator = BiometricAuthenticator()

        val authRepository = AuthRepositoryImpl()

        val authViewModelFactory = AuthViewModelFactory(
            authRepository = authRepository
        )

        setContent {
            RakshaBandhanTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = authViewModelFactory
                )

                val uiState by authViewModel.uiState.collectAsState()

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

                        authViewModel.setAuthenticating()

                        biometricAuthenticator.authenticate(
                            activity = this,
                            onSuccess = {
                                authViewModel.onAuthenticationSuccess()
                            },
                            onError = { errorMessage ->
                                authViewModel.onAuthenticationError(errorMessage)
                            }
                        )
                    },
                    onRetry = authViewModel::resetError
                )
            }
        }
    }
}
