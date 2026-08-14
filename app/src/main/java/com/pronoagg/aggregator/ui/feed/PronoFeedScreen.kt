package com.pronoagg.aggregator.ui.feed

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pronoagg.aggregator.data.local.PronoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronoFeedScreen(
    onOpenChannels: () -> Unit,
    viewModel: PronoFeedViewModel = viewModel()
) {
    val pronos by viewModel.pronos.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pronos") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                        }
                    }
                    IconButton(onClick = onOpenChannels) {
                        Icon(Icons.Default.Settings, contentDescription = "Canaux")
                    }
                }
            )
        }
    ) { padding ->
        if (pronos.isEmpty()) {
            EmptyFeed(modifier = Modifier.padding(padding), onOpenChannels = onOpenChannels)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, padding.calculateTopPadding(), 16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pronos, key = { it.id }) { prono ->
                    PronoCard(prono)
                }
            }
        }
    }
}

@Composable
private fun EmptyFeed(modifier: Modifier = Modifier, onOpenChannels: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Aucun prono pour l'instant",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Ajoute un canal Telegram public pour commencer à agréger des pronos.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
            )
            TextButton(onClick = onOpenChannels, modifier = Modifier.padding(top = 12.dp)) {
                Text("Gérer les canaux")
            }
        }
    }
}

@Composable
private fun PronoCard(prono: PronoEntity) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = prono.channelDisplayName ?: "@${prono.channelUsername}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = DateUtils.getRelativeTimeSpanString(prono.timestampMillis).toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = prono.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(prono.link)))
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Ouvrir sur Telegram")
            }
        }
    }
}
