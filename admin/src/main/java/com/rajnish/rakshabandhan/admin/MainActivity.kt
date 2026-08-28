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

private data class SisterOption(val id: String, val name: String)
private val sisters = listOf(
    SisterOption("Sister_01", "Nisha"), SisterOption("Sister_02", "Neha"),
    SisterOption("Sister_03", "Mona"), SisterOption("Sister_04", "Khushi"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AdminScreen() }
    }

    private fun sendInvitation(sisterId: String, email: String, onComplete: (InvitationResult) -> Unit) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { InvitationApi.createInvitation(sisterId, email) }
            onComplete(result)
        }
    }

    private fun configureGift(sisterId: String, amount: String, eligible: Boolean, onComplete: (GiftResult) -> Unit) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { GiftApi.configureGift(sisterId, amount, eligible) }
            onComplete(result)
        }
    }

    @Composable
    private fun AdminScreen() {
        var selected by remember { mutableStateOf(sisters.first()) }
        var expanded by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var eligible by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        var savingGift by remember { mutableStateOf(false) }
        var sendingInvitation by remember { mutableStateOf(false) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text("Raksha Bandhan Admin", style = MaterialTheme.typography.headlineMedium)
                Text("Manage a sister's invitation and gift.", style = MaterialTheme.typography.bodyLarge)

                Column {
                    Text("Sister", style = MaterialTheme.typography.labelLarge)
                    OutlinedButton(
                        onClick = { expanded = true },
                        enabled = !savingGift && !sendingInvitation,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(selected.name)
                            Text(selected.id)
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sisters.forEach { sister ->
                            DropdownMenuItem(
                                text = { Text("${sister.name} (${sister.id})") },
                                onClick = {
                                    selected = sister
                                    expanded = false
                                    message = null
                                },
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
                        sendInvitation(selected.id, value) { result ->
                            sendingInvitation = false
                            message = result.message
                            if (result.success) email = ""
                        }
                    },
                    enabled = !sendingInvitation && !savingGift,
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
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it; message = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Gift amount (INR)") },
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
                                onCheckedChange = { eligible = it; message = null },
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
                                configureGift(selected.id, amount.trim(), eligible) { result ->
                                    savingGift = false
                                    message = result.message
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
                    }
                }

                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
