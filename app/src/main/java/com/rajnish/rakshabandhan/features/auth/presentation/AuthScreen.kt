package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rajnish.rakshabandhan.features.auth.domain.AuthState

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onAuthenticate: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        when (val state = uiState.authState) {

            AuthState.Initializing -> {
                Text("Checking authentication...")
            }

            AuthState.Unauthenticated -> {
                Text("Authentication Required")

                Button(
                    onClick = onAuthenticate
                ) {
                    Text("Authenticate")
                }
            }

            AuthState.Authenticating -> {
                Text("Authenticating...")
            }

            AuthState.Authenticated -> {
                Text("Authenticated")
            }

            is AuthState.Error -> {
                Text(state.message)

                Button(
                    onClick = onRetry
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}