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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Surface(modifier = Modifier.fillMaxSize()) {
        when (val current = state) {
            HomeUiState.Loading -> LoadingHome()
            is HomeUiState.Success -> SuccessHome(current.data, viewModel)
            HomeUiState.Offline -> ActionHome("You're offline", "Please reconnect and try again.", "Retry", viewModel::load)
            is HomeUiState.Error -> ActionHome("Something went wrong", current.message, "Try again", viewModel::load)
            HomeUiState.Unauthorized -> ActionHome("Authentication required", "Your session is no longer available.", "Retry", viewModel::load)
        }
    }
}

@Composable
private fun SuccessHome(data: HomeData, viewModel: HomeViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp)) {
        Text("Rakhi Bandhan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Hello, ${data.name} ❤️", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Wishing you a very Happy Raksha Bandhan!", modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(28.dp))
        GiftCard(data.gift, data.claim, data.name, viewModel)
        Spacer(modifier = Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Your account is protected", fontWeight = FontWeight.SemiBold)
                Text("Trusted device authentication is active.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun GiftCard(
    gift: com.rajnish.rakshabandhan.features.home.data.GiftData?,
    claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData?,
    sisterName: String,
    viewModel: HomeViewModel,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("🎁  Your Rakhi Gift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(18.dp))

            when {
                claim?.status == "PAID" -> PaidClaimState(claim)
                claim?.status == "PENDING" -> PendingClaimState(claim)
                gift == null -> {
                    Text("Gift details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Your gift will appear here once it is configured.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
                }
                else -> {
                    val amount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
                        minimumFractionDigits = 2
                        maximumFractionDigits = 2
                    }.format(gift.amount)
                    Text("${gift.currency} $amount", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("Gift status: ${gift.status.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    Spacer(modifier = Modifier.height(18.dp))
                    if (gift.claimEligible) ClaimForm(sisterName, viewModel) else {
                        Text("This gift is not currently eligible for claiming.", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClaimForm(sisterName: String, viewModel: HomeViewModel) {
    var upiId by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val current by viewModel.uiState.collectAsState()
    val error = (current as? HomeUiState.Success)?.data?.claimError

    OutlinedTextField(
        value = upiId,
        onValueChange = { upiId = it; viewModel.clearClaimError() },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("UPI ID") },
        placeholder = { Text("yourname@upi") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        enabled = !submitting,
        supportingText = { Text("Enter the UPI ID where your gift should be sent.") },
        isError = error != null,
    )
    error?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
    Spacer(modifier = Modifier.height(14.dp))
    Button(
        onClick = {
            submitting = true
            viewModel.submitClaim(upiId)
            submitting = false
        },
        enabled = !submitting && upiId.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (submitting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
        Text(if (submitting) "Submitting..." else "Claim Gift")
    }
    Text(
        "Your claim will be reviewed and paid manually.",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
    )
}

@Composable
private fun PendingClaimState(claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData) {
    Text("Claim submitted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("We have received your gift claim.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text("Amount: ${claim.currency} ${claim.amount}", fontWeight = FontWeight.SemiBold)
    Text("UPI ID: ${claim.upiId}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    Text("Status: Payment pending", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun PaidClaimState(claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData) {
    Text("🎉 Gift Paid!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("Happy Raksha Bandhan! Your gift has been paid successfully.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text("Amount paid: ${claim.currency} ${claim.amount}", fontWeight = FontWeight.SemiBold)
    claim.paidAt?.let { Text("Paid on: $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp)) }
}

@Composable
private fun LoadingHome() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Text("Loading your Rakhi home...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ActionHome(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) { Text(action) }
    }
}
