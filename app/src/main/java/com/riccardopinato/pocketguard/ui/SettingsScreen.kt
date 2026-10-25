package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = Modifier
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Section("Modalità") {
            EnumChips(
                values = GuardMode.entries,
                selected = settings.mode,
                label = {
                    if (it == GuardMode.POCKET) "Tasca" else "Borsa"
                },
                onSelected = onMode
            )

            Text(
                if (settings.mode == GuardMode.POCKET) {
                    "Tasca: prossimità + luce + movimento."
                } else {
                    "Borsa: luce + movimento + rotazione; prossimità come segnale extra."
                }
            )
        }

        Section("Journey Guard") {
            EnumChips(
                values = JourneyDuration.entries,
                selected = settings.journeyDuration,
                label = {
                    when (it) {
                        JourneyDuration.MINUTES_30 -> "30m"
                        JourneyDuration.HOUR_1 -> "1h"
                        JourneyDuration.HOURS_2 -> "2h"
                        JourneyDuration.UNTIL_STOPPED -> "∞"
                    }
                },
                onSelected = onJourney
            )
        }

        Section("Sensibilità") {
            EnumChips(
                values = PocketSensitivity.entries,
                selected = settings.sensitivity,
                label = {
                    when (it) {
                        PocketSensitivity.LOW -> "Bassa"
                        PocketSensitivity.NORMAL -> "Normale"
                        PocketSensitivity.HIGH -> "Alta"
                    }
                },
                onSelected = onSensitivity
            )
        }

        Section("Countdown") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 5, 10, 15).forEach { seconds ->
                    FilterChip(
                        selected = settings.armDelaySeconds == seconds,
                        onClick = { onDelay(seconds) },
                        label = { Text("${seconds}s") }
                    )
                }
            }
        }

        Section("Allarme") {
            ToggleRow("Sirena", settings.soundEnabled, onSound)
            ToggleRow("Vibrazione", settings.vibrationEnabled, onVibration)
            ToggleRow(
                "Flash",
                settings.flashEnabled,
                onFlash,
                enabled = capabilities.hasFlash
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTestAlarm
            ) {
                Text("TEST ALLARME 3 SECONDI")
            }
        }

        Section("Sicurezza") {
            ToggleRow(
                "Biometria per disarmo",
                settings.biometricEnabled,
                onBiometric
            )
        }

        Section("Affidabilità") {
            ToggleRow(
                "Affidabilità avanzata",
                settings.enhancedReliability,
                onReliability
            )

            Text(
                "Usa un Partial WakeLock solo quando la posizione protetta è stata " +
                    "confermata. Può aumentare il consumo batteria.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
private fun <T> EnumChips(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        values.forEach {
            FilterChip(
                selected = it == selected,
                onClick = { onSelected(it) },
                label = { Text(label(it)) }
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onChanged
        )
    }
}
