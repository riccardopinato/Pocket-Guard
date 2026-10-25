package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.model.*

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    settings: GuardSettings,
    capabilities: SensorCapabilities,
    onMode: (GuardMode) -> Unit,
    onJourney: (JourneyDuration) -> Unit,
    onSensitivity: (PocketSensitivity) -> Unit,
    onDelay: (Int) -> Unit,
    onSound: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onFlash: (Boolean) -> Unit,
    onBiometric: (Boolean) -> Unit,
    onReliability: (Boolean) -> Unit,
    onTestAlarm: () -> Unit
) {
    Column(
        modifier = Modifier.padding(contentPadding).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = "Controlli rapidi",
            subtitle = "Tutto ciò che serve, senza l'aspetto da menu di sistema.",
            badge = "Smart",
            accent = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("Modalità", if (settings.mode == GuardMode.POCKET) "Tasca" else "Borsa", Modifier.weight(1f))
                StatPill("Delay", settings.armDelaySeconds.toString() + "s", Modifier.weight(1f))
            }
        }

        SettingSectionCard("Protezione", "Scegli il comportamento base di Pocket Guard.") {
            ChoiceRow(GuardMode.entries, settings.mode, { if (it == GuardMode.POCKET) "Tasca" else "Borsa" }, onMode)
            ChoiceRow(PocketSensitivity.entries, settings.sensitivity, {
                when (it) {
                    PocketSensitivity.LOW -> "Bassa"
                    PocketSensitivity.NORMAL -> "Normale"
                    PocketSensitivity.HIGH -> "Alta"
                }
            }, onSensitivity)
            ChoiceRow(listOf(3, 5, 10, 15), settings.armDelaySeconds, { it.toString() + "s" }, onDelay)
        }

        SettingSectionCard("Journey Guard", "Decidi per quanto tempo mantenere attiva la protezione.") {
            ChoiceRow(JourneyDuration.entries, settings.journeyDuration, {
                when (it) {
                    JourneyDuration.MINUTES_30 -> "30m"
                    JourneyDuration.HOUR_1 -> "1h"
                    JourneyDuration.HOURS_2 -> "2h"
                    JourneyDuration.UNTIL_STOPPED -> "∞"
                }
            }, onJourney)
        }

        SettingSectionCard("Allarme", "Scegli come reagire a una possibile estrazione.") {
            ToggleRow("Sirena", "Riproduce il suono di allarme.", settings.soundEnabled, onSound)
            ToggleRow("Vibrazione", "Fa vibrare il telefono durante l'allarme.", settings.vibrationEnabled, onVibration)
            ToggleRow(
                "Flash",
                if (capabilities.hasFlash) "Lampeggia durante l'allarme." else "Non disponibile su questo dispositivo.",
                settings.flashEnabled,
                onFlash,
                capabilities.hasFlash
            )
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onTestAlarm) { Text("Test allarme 3 secondi") }
        }

        SettingSectionCard("Sicurezza e affidabilità", "Controlli proprietario e stabilità in background.") {
            ToggleRow("Biometria", "Usa impronta o volto per il disarmo quando disponibili.", settings.biometricEnabled, onBiometric)
            ToggleRow("Affidabilità avanzata", "Può migliorare la continuità della protezione con un maggiore consumo.", settings.enhancedReliability, onReliability)
        }
    }
}

@Composable
private fun SettingSectionCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    SectionCard(title = title, subtitle = subtitle) { content() }
}

@Composable
private fun <T> ChoiceRow(values: List<T>, selected: T, label: (T) -> String, onSelected: (T) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        values.forEach { item ->
            FilterChip(
                selected = item == selected,
                onClick = { onSelected(item) },
                label = {
                    Text(label(item), fontWeight = if (item == selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            )
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChanged: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChanged, enabled = enabled)
    }
}
