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
            _uiState.value = AuthUiState(
                authState = AuthState.Initializing
            )

            try {
                val hasSession = authRepository.hasAuthenticatedSession()

                _uiState.value = AuthUiState(
                    authState = if (hasSession) {
                        AuthState.Authenticated
                    } else {
                        AuthState.Unauthenticated
                    }
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    authState = AuthState.Error(
                        message = e.message
                            ?: "Unable to check authentication session"
                    )
                )
            }
        }
    }

    fun setAuthenticating() {
        _uiState.value = AuthUiState(
            authState = AuthState.Authenticating
        )
    }

    fun onAuthenticationSuccess() {
        viewModelScope.launch {
            try {
                authRepository.saveAuthenticatedSession()

                _uiState.value = AuthUiState(
                    authState = AuthState.Authenticated
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    authState = AuthState.Error(
                        message = e.message
                            ?: "Unable to save authentication session"
                    )
                )
            }
        }
    }

    fun onAuthenticationError(message: String) {
        _uiState.value = AuthUiState(
            authState = AuthState.Error(
                message = message
            )
        )
    }

    fun resetError() {
        _uiState.value = AuthUiState(
            authState = AuthState.Unauthenticated
        )
    }

    fun clearSession() {
        viewModelScope.launch {
            try {
                authRepository.clearAuthenticatedSession()

                _uiState.value = AuthUiState(
                    authState = AuthState.Unauthenticated
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    authState = AuthState.Error(
                        message = e.message
                            ?: "Unable to clear authentication session"
                    )
                )
            }
        }
    }
}