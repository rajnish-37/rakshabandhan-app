package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import com.rajnish.rakshabandhan.features.home.presentation.HomeScreen

@Composable
fun AuthenticatedAppShell() {
    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        HomeScreen()
    }
}
