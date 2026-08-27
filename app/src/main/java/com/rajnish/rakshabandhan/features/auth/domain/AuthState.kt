package com.rajnish.rakshabandhan.features.auth.domain

sealed interface AuthState {

    data object Initializing : AuthState

    data object Unauthenticated : AuthState

    data object Authenticating : AuthState

    data object Authenticated : AuthState

    data class Error(
        val message: String
    ) : AuthState
}