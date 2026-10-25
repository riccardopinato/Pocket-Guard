package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.history.GuardHistoryEvent
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    events: List<GuardHistoryEvent>,
    onClear: () -> Unit
) {
    LazyColumn(contentPadding = contentPadding, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HeroCard(
                "Cronologia protezione",
                "Gli ultimi eventi registrati localmente.",
                events.size.toString(),
                accent = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
            ) {}
        }
        item {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = events.isNotEmpty(), onClick = onClear) {
                Text("Cancella cronologia")
            }
        }
        if (events.isEmpty()) {
            item {
                SectionCard("Nessun evento", "La cronologia apparirà qui dopo il primo utilizzo.") {}
            }
        }
        items(items = events, key = { it.timestamp.toString() + "_" + it.type + "_" + it.detail }) { event ->
            SectionCard(title = event.type) {
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(event.timestamp)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(event.detail, fontWeight = FontWeight.Medium)
            }
        }
    }
}
