package com.rajnish.rakshabandhan.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AdminScreen() }
    }

    private fun loadSisters(onComplete: (SisterListResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { SisterApi.getSisters() }) }
    }
    private fun loadGift(sisterId: String, onComplete: (GiftResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { GiftApi.getGift(sisterId) }) }
    }
    private fun sendInvitation(sisterId: String, email: String, onComplete: (InvitationResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { InvitationApi.createInvitation(sisterId, email) }) }
    }
    private fun configureGift(sisterId: String, amount: String, eligible: Boolean, onComplete: (GiftResult) -> Unit) {
        lifecycleScope.launch { onComplete(withContext(Dispatchers.IO) { GiftApi.configureGift(sisterId, amount, eligible) }) }
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
        var message by remember { mutableStateOf<String?>(null) }

        fun loadSelectedGift(sister: SisterOption) {
            selected = sister
            email = sister.email
            expanded = false
            amount = ""
            currency = "INR"
            giftStatus = "PENDING"
            eligible = false
            message = null
            loadingGift = true
            loadGift(sister.id) { result ->
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

        LaunchedEffect(Unit) {
            loadSisters { result ->
                loadingSisters = false
                if (result.success) {
                    sisters = result.sisters
                    result.sisters.firstOrNull()?.let(::loadSelectedGift)
                } else message = result.message
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text("Raksha Bandhan Admin", style = MaterialTheme.typography.headlineMedium)
                Text("Manage sisters, invitations and gifts.", style = MaterialTheme.typography.bodyLarge)

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
                            val value = email.trim()
                            if (!value.contains("@")) {
                                message = "Enter a valid email address."
                                return@Button
                            }
                            sendingInvitation = true
                            message = null
                            sendInvitation(current.id, value) { result ->
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
                                        Text(
                                            if (eligible) "Sister can claim this gift" else "Keep gift pending",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Switch(
                                        checked = eligible,
                                        onCheckedChange = { eligible = it; giftStatus = if (it) "ELIGIBLE" else "PENDING"; message = null },
                                        enabled = !savingGift && !sendingInvitation,
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (amount.toDoubleOrNull()?.let { it > 0 } != true) {
                                            message = "Enter a valid gift amount."
                                            return@Button
                                        }
                                        savingGift = true
                                        message = null
                                        configureGift(current.id, amount.trim(), eligible) { result ->
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
                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
