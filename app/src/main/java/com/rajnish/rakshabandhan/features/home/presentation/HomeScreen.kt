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
import java.text.NumberFormat
import java.util.Locale

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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "❤️",
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)).padding(12.dp),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = "Rakhi Bandhan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Your special day", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Hello, ${data.name} ❤️", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            text = "Wishing you a very Happy Raksha Bandhan!",
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(28.dp))
        GiftCard(data.gift)
        Spacer(modifier = Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Text(text = "Your account is protected", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Trusted device authentication is active.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun GiftCard(gift: com.rajnish.rakshabandhan.features.home.data.GiftData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "🎁  Your Rakhi Gift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(18.dp))
            if (gift == null) {
                Text(text = "Gift details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Your gift will appear here once it is configured.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 5.dp),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text(text = "Claim Gift") }
                Text(
                    text = "Gift status: Not configured",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
                )
            } else {
                val amount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }.format(gift.amount)
                Text(text = "${gift.currency} $amount", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Gift status: ${gift.status.replace('_', ' ')}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = {}, enabled = gift.claimEligible, modifier = Modifier.fillMaxWidth()) { Text(text = "Claim Gift") }
                Text(
                    text = if (gift.claimEligible) "Your gift is ready to claim." else "This gift is not currently eligible for claiming.",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun LoadingHome() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(text = "Loading your Rakhi home...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ActionHome(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = message, modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) { Text(text = action) }
    }
}
