package com.rajnish.rakshabandhan.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Authenticated sister home entry point.
 *
 * Phase 2A intentionally keeps this feature presentation-only. Server-backed
 * identity and gift data are introduced in later Phase 2 milestones.
 */
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Rakhi Bandhan")
        Text(
            text = "Your home is ready.",
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
