package com.rajnish.rakshabandhan.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        HomeUiState.Loading -> LoadingHome()
        is HomeUiState.Success -> SuccessHome(current.data)
        HomeUiState.Offline -> ActionHome(
            title = "You're offline",
            message = "Please reconnect and try again.",
            action = "Retry",
            onAction = viewModel::load,
        )
        is HomeUiState.Error -> ActionHome(
            title = "Something went wrong",
            message = current.message,
            action = "Try again",
            onAction = viewModel::load,
        )
        HomeUiState.Unauthorized -> ActionHome(
            title = "Authentication required",
            message = "Your session is no longer available.",
            action = "Retry",
            onAction = viewModel::load,
        )
    }
}

@Composable
private fun LoadingHome() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text("Loading your Rakhi home...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun SuccessHome(data: HomeData) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Hello, ${data.name} ❤️")
        Text("Your Rakhi Bandhan home", modifier = Modifier.padding(top = 8.dp))
        Text("Sister ID: ${data.sisterId}", modifier = Modifier.padding(top = 20.dp))
        Text("Status: ${data.enrollmentStatus}", modifier = Modifier.padding(top = 4.dp))
        Text("Your gift details will appear here.", modifier = Modifier.padding(top = 20.dp))
    }
}

@Composable
private fun ActionHome(
    title: String,
    message: String,
    action: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title)
        Text(message, modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) {
            Text(action)
        }
    }
}
