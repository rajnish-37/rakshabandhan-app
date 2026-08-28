package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rajnish.rakshabandhan.features.auth.domain.AuthState

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onCodeChanged: (String) -> Unit,
    onVerifyInvitation: () -> Unit,
    onAuthenticate: () -> Unit,
    onSignOut: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = uiState.authState) {
            AuthState.Initializing -> {
                Text("Checking authentication...")
            }

            AuthState.EnrollmentRequired -> {
                Text("Join Rakhi Bandhan")
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = onCodeChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text("Invitation code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
                Button(
                    onClick = onVerifyInvitation,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Verify Invitation")
                }
            }

            AuthState.Unauthenticated -> {
                Text("Authentication Required")
                Button(
                    onClick = onAuthenticate,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Authenticate")
                }
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("DEV: Sign Out")
                }
            }

            AuthState.Authenticating -> {
                Text("Authenticating...")
            }

            AuthState.Enrolling -> {
                Text("Verifying invitation...")
            }

            AuthState.Authenticated -> {
                Text("Authenticated")
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("DEV: Sign Out")
                }
            }

            is AuthState.Error -> {
                Text(state.message)
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}
