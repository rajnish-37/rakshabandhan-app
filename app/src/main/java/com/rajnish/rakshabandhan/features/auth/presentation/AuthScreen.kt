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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Fingerprint
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
import com.rajnish.rakshabandhan.ui.theme.RakhiBlush
import com.rajnish.rakshabandhan.ui.theme.RakhiGold
import com.rajnish.rakshabandhan.ui.theme.RakhiInk
import com.rajnish.rakshabandhan.ui.theme.RakhiMaroon
import com.rajnish.rakshabandhan.ui.theme.RakhiMuted

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onCodeChanged: (String) -> Unit,
    onVerifyInvitation: () -> Unit,
    onAuthenticate: () -> Unit,
    onRetry: () -> Unit,
    previewScreenTwo: Boolean = false,
) {
    if (previewScreenTwo) {
        TrustedDeviceScreen()
        return
    }

    LaunchedEffect(uiState.authState) {
        if (uiState.authState == AuthState.Unauthenticated) {
            onAuthenticate()
        }
    }

    when (val state = uiState.authState) {
        AuthState.Authenticated -> AuthenticatedAppShell()
        AuthState.Initializing -> LoadingScreen("Checking authentication...")
        AuthState.Authenticating -> TrustedDeviceScreen()
        AuthState.Enrolling -> LoadingScreen("Verifying invitation...")
        AuthState.EnrollmentRequired -> EnrollmentScreen(
            email = uiState.email,
            code = uiState.code,
            onEmailChanged = onEmailChanged,
            onCodeChanged = onCodeChanged,
            onVerifyInvitation = onVerifyInvitation,
        )
        AuthState.Unauthenticated -> TrustedDeviceScreen()
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
private fun TrustedDeviceScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 22.dp)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(RakhiBlush.copy(alpha = 0.72f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 72.dp, start = 18.dp)
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(RakhiBlush.copy(alpha = 0.52f)),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(RakhiBlush),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "Secure sign in",
                        tint = RakhiMaroon,
                        modifier = Modifier.size(50.dp),
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "बस एक आख़िरी कदम…",
                    style = MaterialTheme.typography.headlineSmall,
                    color = RakhiInk,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "इस डिवाइस को पहचान लेने दें,\nफिर आपकी राखी खुल जाएगी।",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RakhiMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "आपकी पहचान सुरक्षित है",
                            style = MaterialTheme.typography.titleMedium,
                            color = RakhiInk,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Fingerprint या Face से बस एक बार पहचान कीजिए।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RakhiMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )

                        Row(
                            modifier = Modifier.padding(top = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(RakhiGold),
                            )
                            Text(
                                text = "  आपके डिवाइस की security के साथ",
                                style = MaterialTheme.typography.labelMedium,
                                color = RakhiMuted,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "भैया की तरफ़ से, प्यार के साथ ❤️",
                    style = MaterialTheme.typography.labelLarge,
                    color = RakhiMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 28.dp)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(RakhiBlush.copy(alpha = 0.75f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 84.dp, start = 22.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(RakhiBlush.copy(alpha = 0.55f)),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(RakhiBlush),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = RakhiMaroon,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "आपके लिए कुछ रखा है…",
                    style = MaterialTheme.typography.headlineSmall,
                    color = RakhiInk,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "इस बार की राखी\nथोड़ी सी खास है।",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RakhiMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp,
                )
                Spacer(modifier = Modifier.height(28.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "शुरू करने के लिए",
                            style = MaterialTheme.typography.titleMedium,
                            color = RakhiInk,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "आपको भेजा गया email और invitation code डालें।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RakhiMuted,
                            modifier = Modifier.padding(top = 5.dp, bottom = 18.dp),
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email address") },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RakhiMaroon,
                                focusedLabelColor = RakhiMaroon,
                                cursorColor = RakhiMaroon,
                            ),
                        )
                        OutlinedTextField(
                            value = code,
                            onValueChange = onCodeChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            label = { Text("Invitation code") },
                            leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RakhiMaroon,
                                focusedLabelColor = RakhiMaroon,
                                cursorColor = RakhiMaroon,
                            ),
                        )
                        Button(
                            onClick = onVerifyInvitation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                                .height(54.dp),
                            shape = RoundedCornerShape(17.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RakhiMaroon,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(text = "मेरी राखी खोलें", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(RakhiGold),
                    )
                    Text(
                        text = "  भैया की तरफ़ से, प्यार के साथ ❤️",
                        style = MaterialTheme.typography.labelMedium,
                        color = RakhiMuted,
                    )
                }
            }
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
        Text(text = message, modifier = Modifier.padding(top = 12.dp))
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
            Text(text = message, modifier = Modifier.padding(top = 8.dp))
        }
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) {
            Text(actionLabel)
        }
    }
}
