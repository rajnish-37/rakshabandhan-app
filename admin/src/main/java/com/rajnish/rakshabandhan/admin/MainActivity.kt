package com.rajnish.rakshabandhan.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AdminScreen() }
    }

    private fun loadSisters(key: String, onComplete: (SisterListResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { SisterApi.getSisters(key) }) }
    }
    private fun loadGift(sisterId: String, key: String, onComplete: (GiftResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { GiftApi.getGift(sisterId, key) }) }
    }
    private fun sendInvitation(sisterId: String, email: String, key: String, onComplete: (InvitationResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { InvitationApi.createInvitation(sisterId, email, key) }) }
    }
    private fun configureGift(sisterId: String, amount: String, eligible: Boolean, key: String, onComplete: (GiftResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { GiftApi.configureGift(sisterId, amount, eligible, key) }) }
    }
    private fun loadPendingClaims(key: String, onComplete: (ClaimsResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { ClaimApi.getPendingClaims(key) }) }
    }
    private fun markClaimPaid(key: String, claimId: String, onComplete: (ClaimsResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { ClaimApi.markPaid(key, claimId) }) }
    }

    @Composable
    private fun AdminScreen() {
        var sisters by remember { mutableStateOf<List<SisterOption>>(emptyList()) }
        var selected by remember { mutableStateOf<SisterOption?>(null) }
        var expanded by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var currency by remember { mutableStateOf("INR") }
        var giftStatus by remember { mutableStateOf("PENDING") }
        var eligible by remember { mutableStateOf(false) }
        var loadingSisters by remember { mutableStateOf(true) }
        var loadingGift by remember { mutableStateOf(false) }
        var savingGift by remember { mutableStateOf(false) }
        var sendingInvitation by remember { mutableStateOf(false) }
        var adminApiKey by remember { mutableStateOf(BuildConfig.ADMIN_API_KEY) }
        var claims by remember { mutableStateOf<List<AdminClaim>>(emptyList()) }
        var selectedClaim by remember { mutableStateOf<AdminClaim?>(null) }
        var loadingClaims by remember { mutableStateOf(false) }
        var markingPaid by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        fun requireAdminKey(): String? {
            val key = adminApiKey.trim()
            if (key.isBlank()) {
                message = "Admin API key is required for this build."
                return null
            }
            return key
        }

        fun loadSelectedGift(sister: SisterOption) {
            val key = requireAdminKey() ?: return
            selected = sister
            email = sister.email
            expanded = false
            amount = ""
            currency = "INR"
            giftStatus = "PENDING"
            eligible = false
            message = null
            loadingGift = true
            loadGift(sister.id, key) { result ->
                loadingGift = false
                if (result.success) {
                    result.gift?.let {
                        amount = it.amount
                        currency = it.currency
                        giftStatus = it.status
                        eligible = it.claimEligible
                        message = "Existing gift loaded."
                    } ?: run { message = result.message }
                } else message = result.message
            }
        }

        fun refreshSisters() {
            val key = requireAdminKey() ?: return
            loadingSisters = true
            message = null
            loadSisters(key) { result ->
                loadingSisters = false
                if (result.success) {
                    sisters = result.sisters
                    result.sisters.firstOrNull()?.let(::loadSelectedGift)
                } else message = result.message
            }
        }

        fun refreshClaims() {
            val key = requireAdminKey() ?: return
            loadingClaims = true
            message = null
            loadPendingClaims(key) { result ->
                loadingClaims = false
                if (result.success) {
                    claims = result.claims
                    selectedClaim = null
                    message = if (result.claims.isEmpty()) "No pending claims." else "${result.claims.size} pending claim(s) loaded."
                } else message = result.message
            }
        }

        LaunchedEffect(Unit) {
            refreshSisters()
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text("Raksha Bandhan Admin", style = MaterialTheme.typography.headlineMedium)
                Text("Manage sisters, invitations, gifts and manual payments.", style = MaterialTheme.typography.bodyLarge)

                OutlinedTextField(
                    value = adminApiKey,
                    onValueChange = { adminApiKey = it; message = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Admin API key") },
                    singleLine = true,
                )
                OutlinedButton(onClick = ::refreshSisters, enabled = !loadingSisters && !sendingInvitation && !savingGift, modifier = Modifier.fillMaxWidth()) {
                    Text(if (loadingSisters) "Loading sisters..." else "Load / Refresh Sisters")
                }

                if (loadingSisters) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                        Text("Loading sisters...")
                    }
                } else if (selected == null) {
                    Text(message ?: "No sisters are configured.")
                } else {
                    val current = selected!!
                    Column {
                        Text("Sister", style = MaterialTheme.typography.labelLarge)
                        OutlinedButton(
                            onClick = { expanded = true },
                            enabled = !savingGift && !sendingInvitation && !loadingGift,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(current.name)
                                Text(current.id)
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            sisters.forEach { sister ->
                                DropdownMenuItem(
                                    text = { Text("${sister.name} (${sister.id})") },
                                    onClick = { loadSelectedGift(sister) },
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; message = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Sister email") },
                        singleLine = true,
                        enabled = !savingGift && !sendingInvitation,
                    )
                    Button(
                        onClick = {
                            val key = requireAdminKey() ?: return@Button
                            val value = email.trim()
                            if (!value.contains("@")) {
                                message = "Enter a valid email address."
                                return@Button
                            }
                            sendingInvitation = true
                            message = null
                            sendInvitation(current.id, value, key) { result ->
                                sendingInvitation = false
                                message = result.message
                            }
                        },
                        enabled = !sendingInvitation && !savingGift && !loadingGift,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (sendingInvitation) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                            Text("Sending...")
                        } else Text("Send Invitation")
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Gift configuration", style = MaterialTheme.typography.titleMedium)
                            if (loadingGift) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                                    Text("Loading existing gift...")
                                }
                            } else {
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it; message = null },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Gift amount ($currency)") },
                                    singleLine = true,
                                    enabled = !savingGift && !sendingInvitation,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column {
                                        Text("Claim eligible", fontWeight = FontWeight.SemiBold)
                                        Text(if (eligible) "Sister can claim this gift" else "Keep gift pending", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Switch(
                                        checked = eligible,
                                        onCheckedChange = { eligible = it; giftStatus = if (it) "ELIGIBLE" else "PENDING"; message = null },
                                        enabled = !savingGift && !sendingInvitation,
                                    )
                                }
                                Button(
                                    onClick = {
                                        val key = requireAdminKey() ?: return@Button
                                        if (amount.toDoubleOrNull()?.let { it > 0 } != true) {
                                            message = "Enter a valid gift amount."
                                            return@Button
                                        }
                                        savingGift = true
                                        message = null
                                        configureGift(current.id, amount.trim(), eligible, key) { result ->
                                            savingGift = false
                                            message = result.message
                                            if (result.success) giftStatus = if (eligible) "ELIGIBLE" else "PENDING"
                                        }
                                    },
                                    enabled = !savingGift && !sendingInvitation,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    if (savingGift) {
                                        CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                                        Text("Saving...")
                                    } else Text("Save Gift")
                                }
                                Text("Status: $giftStatus", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pending claims", style = MaterialTheme.typography.titleMedium)
                        Text("Payment is manual: copy the UPI ID, pay externally in PhonePe, then mark the claim paid.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = ::refreshClaims, enabled = !loadingClaims && !markingPaid, modifier = Modifier.fillMaxWidth()) {
                            if (loadingClaims) CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                            Text(if (loadingClaims) "Loading..." else "Refresh Pending Claims")
                        }

                        if (claims.isEmpty() && !loadingClaims) {
                            Text("No pending claims loaded.", style = MaterialTheme.typography.bodySmall)
                        }

                        claims.forEach { claim ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(claim.sisterName, fontWeight = FontWeight.Bold)
                                    Text("Sister ID: ${claim.sisterId}", style = MaterialTheme.typography.bodySmall)
                                    val formattedAmount = NumberFormat.getNumberInstance(Locale("en", "IN")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(claim.amount)
                                    Text("Gift: ${claim.currency} $formattedAmount", fontWeight = FontWeight.SemiBold)
                                    Text("UPI ID: ${claim.upiId}")
                                    Text("Status: ${claim.status}", style = MaterialTheme.typography.labelMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", claim.upiId))
                                            message = "UPI ID copied. Pay ${claim.currency} $formattedAmount in PhonePe, then mark paid."
                                            selectedClaim = claim
                                        }) { Text("Copy UPI ID") }
                                        Button(onClick = { selectedClaim = claim }, enabled = !markingPaid) { Text("Select") }
                                    }
                                }
                            }
                        }

                        selectedClaim?.let { claim ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Selected claim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${claim.sisterName} • ${claim.sisterId}")
                                    Text("Pay ${claim.currency} ${claim.amount} to ${claim.upiId}")
                                    Text("Only mark this claim paid after the external PhonePe payment succeeds.", style = MaterialTheme.typography.bodySmall)
                                    Button(
                                        onClick = {
                                            val key = requireAdminKey() ?: return@Button
                                            markingPaid = true
                                            message = null
                                            markClaimPaid(key, claim.claimId) { result ->
                                                markingPaid = false
                                                if (result.success) {
                                                    claims = claims.filterNot { it.claimId == claim.claimId }
                                                    selectedClaim = null
                                                    message = "Claim marked PAID successfully."
                                                } else message = result.message
                                            }
                                        },
                                        enabled = !markingPaid && claim.status == "PENDING",
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        if (markingPaid) CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                                        Text(if (markingPaid) "Marking paid..." else "Mark Paid")
                                    }
                                }
                            }
                        }
                    }
                }

                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
