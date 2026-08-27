package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajnish.rakshabandhan.features.auth.domain.AuthRepository
import com.rajnish.rakshabandhan.features.auth.domain.AuthState
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

    fun checkAuthenticationSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                authState = AuthState.Initializing
            )

            try {
                when {
                    authRepository.hasAuthenticatedSession() -> {
                        _uiState.value = _uiState.value.copy(
                            authState = AuthState.Authenticated
                        )
                    }

                    authRepository.hasFirebaseSession() -> {
                        _uiState.value = _uiState.value.copy(
                            authState = AuthState.Unauthenticated
                        )
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(
                            authState = AuthState.EnrollmentRequired
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    authState = AuthState.Error(
                        message = e.message
                            ?: "Unable to check authentication session"
                    )
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

            val result = authRepository.verifyInvitation(email, code)

            result.onSuccess {
                authRepository.saveAuthenticatedSession()
                _uiState.value = _uiState.value.copy(
                    authState = AuthState.Authenticated
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    authState = AuthState.Error(
                        message = error.message ?: "Unable to verify invitation"
                    )
                )
            }
        }
    }

    fun setAuthenticating() {
        _uiState.value = _uiState.value.copy(
            authState = AuthState.Authenticating
        )
    }

    fun onAuthenticationSuccess() {
        viewModelScope.launch {
            try {
                if (!authRepository.hasFirebaseSession()) {
                    _uiState.value = _uiState.value.copy(
                        authState = AuthState.Error(
                            message = "Firebase session is unavailable. Complete invitation verification first."
                        )
                    )
                    return@launch
                }

                authRepository.saveAuthenticatedSession()

                _uiState.value = _uiState.value.copy(
                    authState = AuthState.Authenticated
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    authState = AuthState.Error(
                        message = e.message
                            ?: "Unable to save authentication session"
                    )
                )
            }
        }
    }

    fun onAuthenticationError(message: String) {
        _uiState.value = _uiState.value.copy(
            authState = AuthState.Error(
                message = message
            )
        )
    }

    fun resetError() {
        checkAuthenticationSession()
    }

    fun clearSession() {
        viewModelScope.launch {
            try {
                authRepository.clearAuthenticatedSession()

                _uiState.value = _uiState.value.copy(
                    authState = AuthState.EnrollmentRequired,
                    email = "",
                    code = "",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    authState = AuthState.Error(
                        message = e.message
                            ?: "Unable to clear authentication session"
                    )
                )
            }
        }
    }
}
