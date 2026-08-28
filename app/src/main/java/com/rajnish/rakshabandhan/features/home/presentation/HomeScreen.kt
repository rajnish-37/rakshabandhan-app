package com.rajnish.rakshabandhan.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajnish.rakshabandhan.ui.theme.RakhiMaroon
import com.rajnish.rakshabandhan.ui.theme.RakhiMuted
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val current = state) {
            HomeUiState.Loading -> LoadingHome()
            is HomeUiState.Success -> SuccessHome(current.data, viewModel)
            HomeUiState.Offline -> ActionHome("अभी connection नहीं है", "Internet से जुड़कर फिर कोशिश कीजिए।", "फिर कोशिश करें", viewModel::load)
            is HomeUiState.Error -> ActionHome("कुछ देर बाद फिर कोशिश करें", current.message, "फिर कोशिश करें", viewModel::load)
            HomeUiState.Unauthorized -> ActionHome("Session समाप्त हो गया", "अपनी पहचान फिर से verify कीजिए।", "फिर कोशिश करें", viewModel::load)
        }
    }
}

private fun greetingFor(name: String): String = when (name.trim().lowercase(Locale.ROOT)) {
    "nisha" -> "Happy Rakhi, Nisha Didi ❤️"
    "neha" -> "Happy Rakhi, Neha Didi ❤️"
    "mona" -> "Happy Rakhi, Mona ❤️"
    "khushi" -> "Happy Rakhi, Khushi ❤️"
    else -> "Happy Rakhi, ${name.trim()} ❤️"
}

@Composable
private fun SuccessHome(data: HomeData, viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp).width(24.dp))
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("राखी का दिन", style = MaterialTheme.typography.labelLarge, color = RakhiMuted)
                Text(greetingFor(data.name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                Text("कुछ रिश्ते", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("वक़्त के साथ पुराने नहीं होते…", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 6.dp))
                Text("बस उनकी यादें और भी खूबसूरत हो जाती हैं।", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 4.dp))
                Text("बचपन की लड़ाइयाँ, छोटी-छोटी शरारतें… और आज भी वही अपनापन। ❤️", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f), lineHeight = 22.sp, modifier = Modifier.padding(top = 18.dp))
            }
        }

        GiftCard(data.gift, data.claim, viewModel)

        Text("राखी बाँधने को हाथ दूर हों तो क्या…\nदिलों की डोर तो पास ही रहती है। ❤️", modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), style = MaterialTheme.typography.bodyMedium, color = RakhiMuted, textAlign = TextAlign.Center, lineHeight = 22.sp)
    }
}

@Composable
private fun GiftCard(gift: com.rajnish.rakshabandhan.features.home.data.GiftData?, claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData?, viewModel: HomeViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(Icons.Outlined.Redeem, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(10.dp))
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("तुम्हारे लिए एक तोहफ़ा 🎁", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("दिल से भेजा गया है", style = MaterialTheme.typography.bodySmall, color = RakhiMuted, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            when {
                claim?.status == "PAID" -> PaidClaimState(claim)
                claim?.status == "PENDING" -> PendingClaimState(claim)
                gift == null -> {
                    Text("अभी तोहफ़ा तैयार हो रहा है…", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("जब भैया इसे configure करेंगे, यहीं दिखाई देगा।", style = MaterialTheme.typography.bodyMedium, color = RakhiMuted, modifier = Modifier.padding(top = 6.dp))
                }
                gift.claimEligible -> EligibleClaimState(gift, viewModel)
                else -> {
                    val amount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(gift.amount)
                    Text("${gift.currency} $amount", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("अभी claim के लिए उपलब्ध नहीं है।", style = MaterialTheme.typography.bodyMedium, color = RakhiMuted, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun EligibleClaimState(gift: com.rajnish.rakshabandhan.features.home.data.GiftData, viewModel: HomeViewModel) {
    var upiId by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val successData = (state as? HomeUiState.Success)?.data
    val error = successData?.claimError
    val submitting = successData?.claimSubmitting == true
    val amount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(gift.amount)

    Text("${gift.currency} $amount", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
    Text("तुम्हारी खुशी के नाम ❤️", style = MaterialTheme.typography.bodyLarge, color = RakhiMuted, modifier = Modifier.padding(top = 4.dp))
    Spacer(modifier = Modifier.height(18.dp))
    Text("तोहफ़ा कहाँ भेजूँ?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("अपनी UPI ID डाल दो, बाकी हम संभाल लेंगे।", style = MaterialTheme.typography.bodyMedium, color = RakhiMuted, modifier = Modifier.padding(top = 5.dp, bottom = 14.dp))
    OutlinedTextField(value = upiId, onValueChange = { upiId = it; viewModel.clearClaimError() }, modifier = Modifier.fillMaxWidth(), label = { Text("UPI ID") }, placeholder = { Text("yourname@upi") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), enabled = !submitting, isError = error != null, supportingText = { Text("जैसे: name@okaxis या mobile@upi") })
    error?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
    Spacer(modifier = Modifier.height(10.dp))
    Button(onClick = { viewModel.submitClaim(upiId) }, enabled = !submitting && upiId.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = RakhiMaroon)) {
        if (submitting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
        Text(if (submitting) "बस भेज रहे हैं…" else "मेरा तोहफ़ा भेज दो", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PendingClaimState(claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData) {
    Text("तुम्हारा तोहफ़ा रास्ते में है 💌", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("Claim मिल गया है। अब बस payment का इंतज़ार है।", style = MaterialTheme.typography.bodyMedium, color = RakhiMuted, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text("${claim.currency} ${claim.amount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("UPI: ${claim.upiId}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
    Text("PENDING", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun PaidClaimState(claim: com.rajnish.rakshabandhan.features.home.data.GiftClaimData) {
    Text("तोहफ़ा पहुँच गया! 🎉", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("पैसे पहुँच गए हैं… दुआएँ तो पहले ही पहुँच चुकी थीं। ❤️", style = MaterialTheme.typography.bodyLarge, color = RakhiMuted, modifier = Modifier.padding(top = 6.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text("${claim.currency} ${claim.amount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    claim.paidAt?.let { Text("Paid: $it", style = MaterialTheme.typography.bodySmall, color = RakhiMuted, modifier = Modifier.padding(top = 6.dp)) }
}

@Composable
private fun LoadingHome() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Text("आपकी राखी तैयार हो रही है…", modifier = Modifier.padding(top = 12.dp), color = RakhiMuted)
    }
}

@Composable
private fun ActionHome(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(message, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center, color = RakhiMuted)
        Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp), shape = RoundedCornerShape(16.dp)) { Text(action) }
    }
}
