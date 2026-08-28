package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onRetry: () -> Unit,
) {
    LaunchedEffect(uiState.authState) {
        if (uiState.authState == AuthState.Unauthenticated) {
            onAuthenticate()
        }
    }

    when (val state = uiState.authState) {
        AuthState.Authenticated -> AuthenticatedAppShell()
        AuthState.Initializing -> LoadingScreen("Checking authentication...")
        AuthState.Authenticating -> LoadingScreen("Authenticating...")
        AuthState.Enrolling -> LoadingScreen("Verifying invitation...")
        AuthState.EnrollmentRequired -> EnrollmentScreen(
            email = uiState.email,
            code = uiState.code,
            onEmailChanged = onEmailChanged,
            onCodeChanged = onCodeChanged,
            onVerifyInvitation = onVerifyInvitation,
        )
        AuthState.Unauthenticated -> LoadingScreen("Authentication required...")
        AuthState.Offline -> CenteredActionScreen(
            title = "You're Offline",
            message = "Connect to the internet and try again.",
            actionLabel = "Retry",
            onAction = onRetry,
        )
        is AuthState.Error -> CenteredActionScreen(
            title = "Authentication Error",
            message = state.message,
            actionLabel = "Try Again",
            onAction = onRetry,
        )
    }
}

@Composable
private fun EnrollmentScreen(
    email: String,
    code: String,
    onEmailChanged: (String) -> Unit,
    onCodeChanged: (String) -> Unit,
    onVerifyInvitation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Join Rakhi Bandhan")
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            label = { Text("Invitation code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        )
        Button(
            onClick = onVerifyInvitation,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Verify Invitation")
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun CenteredActionScreen(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    message: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title)
        if (message != null) {
            Text(
                text = message,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Button(
            onClick = onAction,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(actionLabel)
        }
    }
}
