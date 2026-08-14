package com.pronoagg.aggregator.ui.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pronoagg.aggregator.data.local.ChannelEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageChannelsScreen(
    onBack: () -> Unit,
    viewModel: ManageChannelsViewModel = viewModel()
) {
    val channels by viewModel.channels.collectAsState()
    val isAdding by viewModel.isAdding.collectAsState()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canaux Telegram") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Ajoute un canal Telegram public (le canal doit être en accès public, pas privé).",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("@nom_du_canal") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    enabled = input.isNotBlank() && !isAdding,
                    onClick = {
                        viewModel.addChannel(input)
                        input = ""
                    }
                ) {
                    Text("Ajouter")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(channels, key = { it.id }) { channel ->
                    ChannelRow(channel, onRemove = { viewModel.removeChannel(channel) })
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(channel: ChannelEntity, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("@${channel.username}", style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}
