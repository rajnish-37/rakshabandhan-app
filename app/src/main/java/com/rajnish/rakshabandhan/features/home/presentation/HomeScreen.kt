package com.rajnish.rakshabandhan.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Surface(modifier = Modifier.fillMaxSize()) {
        when (val current = state) {
            HomeUiState.Loading -> LoadingHome()
            is HomeUiState.Success -> SuccessHome(current.data)
            HomeUiState.Offline -> ActionHome("You're offline", "Please reconnect and try again.", "Retry", viewModel::load)
            is HomeUiState.Error -> ActionHome("Something went wrong", current.message, "Try again", viewModel::load)
            HomeUiState.Unauthorized -> ActionHome("Authentication required", "Your session is no longer available.", "Retry", viewModel::load)
        }
    }
}

@Composable
private fun SuccessHome(data: HomeData) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("❤️", modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)).padding(12.dp))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("Rakhi Bandhan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Your special day", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Hello, ${data.name} ❤️", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Wishing you a very Happy Raksha Bandhan!", modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        GiftCard()
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Your account is protected", fontWeight = FontWeight.SemiBold)
                Text("Trusted device authentication is active.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun GiftCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("🎁  Your Rakhi Gift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(18.dp))
            Text("Gift details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Your gift will appear here once it is configured.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(20.dp))
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Claim Gift") }
            Text("Gift status: Not configured", style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp))
        }
    }
}

@Composable
private fun LoadingHome() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Text("Loading your Rakhi home...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ActionHome(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) { Text(action) }
    }
}
