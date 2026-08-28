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
import androidx.compose.runtime.saveable.rememberSaveable
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "❤️", modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)).padding(12.dp))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = "Rakhi Bandhan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Your special day", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(text = "Hello, ${data.name} ❤️", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = "Wishing you a very Happy Raksha Bandhan!", style = MaterialTheme.typography.bodyLarge)
        GiftCard(data.gift, data.claim, viewModel)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Text(text = "Your account is protected", fontWeight = FontWeight.SemiBold)
                Text(text = "Trusted device authentication is active.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun GiftCard(gift: com.rajnish.rakshabandhan.features.home.data.GiftData?, claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData?, viewModel: HomeViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "🎁  Your Rakhi Gift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(18.dp))
            when {
                claim?.status == "PAID" -> PaidClaimState(claim)
                claim?.status == "PENDING" -> PendingClaimState(claim)
                gift == null -> {
                    Text(text = "Gift details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Your gift will appear here once it is configured.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
                }
                gift.claimEligible -> EligibleClaimState(gift, viewModel)
                else -> {
                    val amount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(gift.amount)
                    Text(text = "${gift.currency} $amount", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text(text = "Gift status: ${gift.status.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "This gift is not currently eligible for claiming.", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun EligibleClaimState(gift: com.rajnish.rakshabandhan.features.home.data.GiftData, viewModel: HomeViewModel) {
    var upiId by rememberSaveable { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val successData = (state as? HomeUiState.Success)?.data
    val error = successData?.claimError
    val submitting = successData?.claimSubmitting == true
    val amount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(gift.amount)

    Text(text = "${gift.currency} $amount", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
    Text(text = "Gift status: ${gift.status.replace('_', ' ')}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(18.dp))
    Text(text = "Claim your gift", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = "Enter the UPI ID where you want to receive the gift.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp, bottom = 14.dp))
    OutlinedTextField(
        value = upiId,
        onValueChange = { upiId = it; viewModel.clearClaimError() },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("UPI ID") },
        placeholder = { Text("yourname@upi") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        enabled = !submitting,
        isError = error != null,
        supportingText = { Text("Example: name@okaxis or mobile@upi") },
    )
    error?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = { viewModel.submitClaim(upiId) },
        enabled = !submitting && upiId.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (submitting) {
            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
        }
        Text(if (submitting) "Submitting..." else "Claim Gift")
    }
    Text(text = "Payment will be made manually by the admin.", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun PendingClaimState(claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData) {
    Text(text = "✅ Claim submitted", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = "Your gift claim has been received and is waiting for payment.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "Amount: ${claim.currency} ${claim.amount}", fontWeight = FontWeight.SemiBold)
    Text(text = "UPI ID: ${claim.upiId}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    Text(text = "Status: PENDING", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    Text(text = "The admin will complete the payment manually.", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun PaidClaimState(claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData) {
    Text(text = "🎉 Gift Paid!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(text = "Happy Raksha Bandhan! Your gift has been paid successfully.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "Amount paid: ${claim.currency} ${claim.amount}", fontWeight = FontWeight.SemiBold)
    claim.paidAt?.let { Text(text = "Paid at: $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp)) }
}

@Composable
private fun LoadingHome() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Text(text = "Loading your Rakhi home...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ActionHome(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = message, modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) { Text(text = action) }
    }
}