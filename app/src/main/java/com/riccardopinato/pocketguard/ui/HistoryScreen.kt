package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp,
            start = 20.dp,
            end = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = events.isNotEmpty(),
                onClick = onClear
            ) {
                Text("CANCELLA CRONOLOGIA")
            }
        }

        if (events.isEmpty()) {
            item {
                Text("Nessun evento registrato.")
            }
        }

        items(
            items = events,
            key = { "${it.timestamp}_${it.type}_${it.detail}" }
        ) { event ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        event.type,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        DateFormat.getDateTimeInstance(
                            DateFormat.SHORT,
                            DateFormat.MEDIUM
                        ).format(Date(event.timestamp))
                    )

                    Text(event.detail)
                }
            }
        }
    }
}
