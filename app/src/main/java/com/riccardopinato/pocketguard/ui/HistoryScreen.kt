package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
    isPro: Boolean,
    onClear: () -> Unit,
    onPro: () -> Unit
) {
    val visibleEvents =
        if (isPro) events else events.take(FREE_HISTORY_LIMIT)

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                "Cronologia protezione",
                if (isPro) {
                    "Cronologia completa degli eventi locali."
                } else {
                    "Il piano Free mostra gli ultimi $FREE_HISTORY_LIMIT eventi."
                },
                if (isPro) "PRO" else visibleEvents.size.toString(),
                accent = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary
                )
            ) {}
        }

        if (!isPro) {
            item {
                SectionCard(
                    title = "Cronologia completa • Pro",
                    subtitle = "Sblocca tutti gli eventi per analizzare meglio falsi allarmi e sessioni reali."
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onPro
                    ) {
                        Text("Scopri Pro")
                    }
                }
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = visibleEvents.isNotEmpty(),
                onClick = onClear
            ) {
                Text("Cancella cronologia")
            }
        }

        if (visibleEvents.isEmpty()) {
            item {
                SectionCard(
                    "Nessun evento",
                    "La cronologia apparirà qui dopo il primo utilizzo."
                ) {}
            }
        }

        items(
            items = visibleEvents,
            key = {
                it.timestamp.toString() + "_" +
                    it.type + "_" +
                    it.detail
            }
        ) { event ->
            SectionCard(
                title = eventTitle(event)
            ) {
                Text(
                    DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.MEDIUM
                    ).format(Date(event.timestamp)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    eventDetail(event),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun eventTitle(event: GuardHistoryEvent): String =
    when (event.type) {
        "ARM" -> "Protezione attivata"
        "ALARM" -> "Allarme"
        "OWNER_OK" -> "Proprietario verificato"
        "CALIBRATION_START" -> "Calibrazione avviata"
        "CALIBRATION_OK" -> "Calibrazione completata"
        "CALIBRATION_ERROR" -> "Problema calibrazione"
        "TEST_ALARM" -> "Test allarme"
        "ERROR" -> "Problema"
        else -> "Evento protezione"
    }

private fun eventDetail(event: GuardHistoryEvent): String =
    when (event.type) {
        "ARM" -> "Sessione di protezione avviata."
        "ALARM" -> "È stata confermata un'estrazione sospetta."
        "OWNER_OK" -> "Il proprietario è stato verificato e la protezione può riarmarsi."
        "CALIBRATION_START" -> "Raccolta dati sensori in corso."
        "CALIBRATION_OK" -> "Profilo sensori aggiornato correttamente."
        "CALIBRATION_ERROR" -> "La calibrazione non è stata completata."
        "TEST_ALARM" -> "Test sirena completato."
        "ERROR" -> "Pockara ha rilevato una condizione non valida."
        else -> "Evento registrato localmente."
    }

private const val FREE_HISTORY_LIMIT = 5
