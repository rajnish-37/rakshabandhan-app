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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class SisterOption(val id: String, val name: String)

private val sisters = listOf(
    SisterOption("Sister_01", "Nisha"),
    SisterOption("Sister_02", "Neha"),
    SisterOption("Sister_03", "Mona"),
    SisterOption("Sister_04", "Khushi"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AdminScreen() }
    }

    private fun sendInvitation(
        sisterId: String,
        email: String,
        onComplete: (InvitationResult) -> Unit,
    ) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                InvitationApi.createInvitation(sisterId, email)
            }
            onComplete(result)
        }
    }

    @Composable
    private fun AdminScreen() {
        var selected by remember { mutableStateOf(sisters.first()) }
        var expanded by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var message by remember { mutableStateOf<String?>(null) }
        var sending by remember { mutableStateOf(false) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text("Raksha Bandhan Admin", style = MaterialTheme.typography.headlineMedium)
                Text("Create an invitation for a sister.", style = MaterialTheme.typography.bodyLarge)

                Column {
                    Text("Sister", style = MaterialTheme.typography.labelLarge)
                    OutlinedButton(
                        onClick = { expanded = true },
                        enabled = !sending,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                    enabled = !sending,
                )

                Button(
                    onClick = {
                        val normalizedEmail = email.trim()
                        if (!normalizedEmail.contains("@")) {
                            message = "Enter a valid email address."
                            return@Button
                        }

                        sending = true
                        message = null
                        sendInvitation(selected.id, normalizedEmail) { result ->
                            sending = false
                            message = result.message
                            if (result.success) email = ""
                        }
                    },
                    enabled = !sending,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (sending) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                        Text("Sending...")
                    } else {
                        Text("Send Invitation")
                    }
                }

                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Selected sister", style = MaterialTheme.typography.labelLarge)
                        Text(selected.name, style = MaterialTheme.typography.titleMedium)
                        Text(selected.id, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
