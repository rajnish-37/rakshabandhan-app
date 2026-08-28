package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        if (uiState.authState == AuthState.Unauthenticated) onAuthenticate()
    }

    when (val state = uiState.authState) {
        AuthState.Authenticated -> AuthenticatedAppShell()
        AuthState.Initializing -> LoadingScreen("Checking authentication...")
        AuthState.Authenticating -> LoadingScreen("Authenticating...")
        AuthState.Enrolling -> LoadingScreen("Verifying invitation...")
        AuthState.EnrollmentRequired -> EnrollmentScreen(email = uiState.email, code = uiState.code, onEmailChanged = onEmailChanged, onCodeChanged = onCodeChanged, onVerifyInvitation = onVerifyInvitation)
        AuthState.Unauthenticated -> LoadingScreen("Authentication required...")
        AuthState.Offline -> CenteredActionScreen("You're Offline", "Connect to the internet and try again.", "Retry", onRetry)
        is AuthState.Error -> CenteredActionScreen("Authentication Error", state.message, "Try Again", onRetry)
    }
}

@Composable
private fun EnrollmentScreen(email: String, code: String, onEmailChanged: (String) -> Unit, onCodeChanged: (String) -> Unit, onVerifyInvitation: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxSize(), color = colors.background) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = 44.dp, end = 18.dp).size(110.dp).clip(CircleShape).background(colors.primaryContainer.copy(alpha = .45f)))
            Box(Modifier.align(Alignment.BottomStart).padding(bottom = 64.dp, start = 14.dp).size(82.dp).clip(CircleShape).background(colors.secondaryContainer.copy(alpha = .35f)))

            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.size(62.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text("आपके लिए कुछ रखा है…", style = MaterialTheme.typography.headlineSmall, color = colors.onBackground, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("इस बार की राखी\nथोड़ी सी खास है।", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 25.sp)
                Spacer(Modifier.height(22.dp))

                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = colors.surface, contentColor = colors.onSurface, tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Column(Modifier.padding(20.dp)) {
                        Text("शुरू करने के लिए", style = MaterialTheme.typography.titleMedium, color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("आपको भेजा गया email और invitation code डालें।", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 5.dp, bottom = 16.dp))
                        OutlinedTextField(
                            value = email, onValueChange = onEmailChanged, modifier = Modifier.fillMaxWidth(), label = { Text("Email address") },
                            leadingIcon = { Icon(Icons.Outlined.Email, null) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary, focusedLabelColor = colors.primary, cursorColor = colors.primary, unfocusedBorderColor = colors.outline, unfocusedLabelColor = colors.onSurfaceVariant, focusedLeadingIconColor = colors.primary, unfocusedLeadingIconColor = colors.onSurfaceVariant)
                        )
                        OutlinedTextField(
                            value = code, onValueChange = onCodeChanged, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Invitation code") },
                            leadingIcon = { Icon(Icons.Outlined.Key, null) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary, focusedLabelColor = colors.primary, cursorColor = colors.primary, unfocusedBorderColor = colors.outline, unfocusedLabelColor = colors.onSurfaceVariant, focusedLeadingIconColor = colors.primary, unfocusedLeadingIconColor = colors.onSurfaceVariant)
                        )
                        Button(onClick = onVerifyInvitation, modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(54.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) {
                            Text("मेरी राखी खोलें", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(colors.secondary))
                    Text("  भैया की तरफ़ से, प्यार के साथ ❤️", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(message, modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun CenteredActionScreen(title: String, message: String, actionLabel: String, onAction: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(message, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text(actionLabel) }
        }
    }
}
