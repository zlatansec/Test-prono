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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.pronoagg.aggregator.data.local.PronoOutcome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronoFeedScreen(
    onOpenChannels: () -> Unit,
    onOpenStats: () -> Unit,
    viewModel: PronoFeedViewModel = viewModel()
) {
    val pronos by viewModel.pronos.collectAsState()
    val visiblePronos by viewModel.visiblePronos.collectAsState()
    val showOnlyPronos by viewModel.showOnlyPronos.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            Column {
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
                        TextButton(onClick = onOpenStats) {
                            Text("📊 Stats")
                        }
                        IconButton(onClick = onOpenChannels) {
                            Icon(Icons.Default.Settings, contentDescription = "Canaux")
                        }
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pronos uniquement",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = showOnlyPronos,
                        onCheckedChange = { viewModel.toggleShowOnlyPronos() }
                    )
                }
            }
        }
    ) { padding ->
        if (visiblePronos.isEmpty()) {
            EmptyFeed(
                modifier = Modifier.padding(padding),
                onOpenChannels = onOpenChannels,
                hiddenByFilter = pronos.isNotEmpty() && showOnlyPronos
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, padding.calculateTopPadding(), 16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visiblePronos, key = { it.id }) { prono ->
                    PronoCard(prono, onSetOutcome = { outcome -> viewModel.setOutcome(prono.id, outcome) })
                }
            }
        }
    }
}

@Composable
private fun EmptyFeed(modifier: Modifier = Modifier, onOpenChannels: () -> Unit, hiddenByFilter: Boolean) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (hiddenByFilter) {
                Text(
                    "Aucun post ne ressemble à un prono",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Désactive \"Pronos uniquement\" pour voir tous les posts des canaux.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
                )
            } else {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PronoCard(prono: PronoEntity, onSetOutcome: (PronoOutcome) -> Unit) {
    val context = LocalContext.current
    val outcome = PronoOutcome.fromStorage(prono.outcome)

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
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = outcome == PronoOutcome.WON,
                    onClick = { onSetOutcome(if (outcome == PronoOutcome.WON) PronoOutcome.UNKNOWN else PronoOutcome.WON) },
                    label = { Text("Gagné") }
                )
                FilterChip(
                    selected = outcome == PronoOutcome.LOST,
                    onClick = { onSetOutcome(if (outcome == PronoOutcome.LOST) PronoOutcome.UNKNOWN else PronoOutcome.LOST) },
                    label = { Text("Perdu") }
                )
                FilterChip(
                    selected = outcome == PronoOutcome.VOID,
                    onClick = { onSetOutcome(if (outcome == PronoOutcome.VOID) PronoOutcome.UNKNOWN else PronoOutcome.VOID) },
                    label = { Text("Annulé") }
                )
            }
            TextButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(prono.link)))
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(if (prono.link.startsWith("https://t.me/")) "Ouvrir sur Telegram" else "Voir la source")
            }
        }
    }
}
