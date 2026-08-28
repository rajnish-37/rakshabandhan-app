package com.rajnish.rakshabandhan.features.auth.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.rajnish.rakshabandhan.features.home.presentation.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedAppShell() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rakhi Bandhan") },
            )
        },
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.padding(paddingValues),
        ) {
            HomeScreen()
        }
    }
}
