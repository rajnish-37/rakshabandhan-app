package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajnish.rakshabandhan.features.auth.domain.AuthRepository
import com.rajnish.rakshabandhan.features.auth.domain.AuthState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthenticationSession()
    }

    fun onAppForegrounded() {
        checkAuthenticationSession(refreshToken = true)
    }

    fun checkAuthenticationSession(refreshToken: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authState = AuthState.Initializing)

            try {
                val hasFirebaseSession = authRepository.hasFirebaseSession()
                val hasDeviceKey = authRepository.hasDeviceKey()

                when {
                    hasFirebaseSession && hasDeviceKey -> {
                        if (refreshToken) {
                            authRepository.refreshFirebaseSession(forceRefresh = false)
                        }
                        _uiState.value = _uiState.value.copy(authState = AuthState.Authenticated)
                    }

                    hasFirebaseSession -> {
                        authRepository.clearFirebaseSession()
                        _uiState.value = _uiState.value.copy(
                            authState = AuthState.EnrollmentRequired,
                            email = "",
                            code = "",
                        )
                    }

                    hasDeviceKey -> {
                        _uiState.value = _uiState.value.copy(authState = AuthState.Unauthenticated)
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(authState = AuthState.EnrollmentRequired)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    authState = if (isOffline(e)) {
                        AuthState.Offline
                    } else {
                        AuthState.Error(
                            message = e.message ?: "Unable to check authentication session"
                        )
                    }
                )
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateCode(code: String) {
        _uiState.value = _uiState.value.copy(code = code.uppercase())
    }

    fun verifyInvitation() {
        val state = _uiState.value
        val email = state.email.trim()
        val code = state.code.trim()

        if (email.isBlank() || !email.contains("@")) {
            onAuthenticationError("Enter a valid email address")
            return
        }

        if (code.isBlank()) {
            onAuthenticationError("Enter the invitation code")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(authState = AuthState.Enrolling)

            authRepository.verifyInvitation(email, code)
                .onSuccess {
                    // Invitation verification only enrolls the trusted public key.
                    // The Firebase session is created only after biometric proof of
                    // possession of the corresponding private key.
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Unauthenticated,
                        email = "",
                        code = "",
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        authState = if (isOffline(error)) {
                            AuthState.Offline
                        } else {
                            AuthState.Error(
                                message = error.message ?: "Unable to verify invitation"
                            )
                        }
                    )
                }
        }
    }

    fun prepareDeviceLogin(onChallengeReady: (challengeId: String, challenge: String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authState = AuthState.Authenticating)

            authRepository.requestDeviceChallenge()
                .onSuccess { challenge ->
                    onChallengeReady(challenge.challengeId, challenge.challenge)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        authState = if (isOffline(error)) {
                            AuthState.Offline
                        } else {
                            AuthState.Error(
                                message = error.message ?: "Unable to start biometric sign in"
                            )
                        }
                    )
                }
        }
    }

    fun completeDeviceLogin(challengeId: String, signature: ByteArray) {
        viewModelScope.launch {
            authRepository.completeDeviceLogin(challengeId, signature)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(authState = AuthState.Authenticated)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        authState = if (isOffline(error)) {
                            AuthState.Offline
                        } else {
                            AuthState.Error(
                                message = error.message ?: "Unable to complete biometric sign in"
                            )
                        }
                    )
                }
        }
    }

    fun onAuthenticationError(message: String) {
        _uiState.value = _uiState.value.copy(authState = AuthState.Error(message))
    }

    fun resetError() {
        checkAuthenticationSession()
    }

    private fun isOffline(error: Throwable): Boolean =
        generateSequence(error) { it.cause }.any { cause ->
            cause is IOException ||
                cause::class.simpleName in setOf(
                    "UnknownHostException",
                    "ConnectException",
                    "SocketTimeoutException",
                    "NoRouteToHostException",
                )
        }
}
