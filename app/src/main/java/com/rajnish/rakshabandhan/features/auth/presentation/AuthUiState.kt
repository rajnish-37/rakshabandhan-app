package com.rajnish.rakshabandhan.features.auth.presentation

import com.rajnish.rakshabandhan.features.auth.domain.AuthState

data class AuthUiState(
    val authState: AuthState = AuthState.Initializing
)