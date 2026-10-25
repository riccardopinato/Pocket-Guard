package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.model.GuardMode
import com.riccardopinato.pocketguard.model.GuardSettings
import com.riccardopinato.pocketguard.model.JourneyDuration
import com.riccardopinato.pocketguard.model.PocketSensitivity
import com.riccardopinato.pocketguard.model.SensorCapabilities

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    settings: GuardSettings,
    capabilities: SensorCapabilities,
    isPro: Boolean,
    onMode: (GuardMode) -> Unit,
    onJourney: (JourneyDuration) -> Unit,
    onSensitivity: (PocketSensitivity) -> Unit,
    onDelay: (Int) -> Unit,
    onSound: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onFlash: (Boolean) -> Unit,
    onBiometric: (Boolean) -> Unit,
    onReliability: (Boolean) -> Unit,
    onTestAlarm: () -> Unit,
    onPro: () -> Unit,
    onPrivacy: () -> Unit
) {
    var showFlashDisclosure by remember { mutableStateOf(false) }

    if (showFlashDisclosure) {
        AlertDialog(
            onDismissRequest = { showFlashDisclosure = false },
            title = { Text("Uso del flash") },
            text = {
                Text(
                    "PockSecure richiede il permesso Fotocamera esclusivamente per accendere e spegnere il flash durante l'allarme. Non scatta, registra o salva foto o video e non invia dati a server PockSecure."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFlashDisclosure = false
                        onFlash(true)
                    }
                ) {
                    Text("Continua")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFlashDisclosure = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = "Controlli rapidi",
            subtitle = "Configura PockSecure in base a come usi davvero il telefono.",
            badge = if (isPro) "PRO" else "Free",
            accent = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.primary
            )
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    "Modalità",
                    if (settings.mode == GuardMode.POCKET) "Tasca" else "Borsa",
                    Modifier.weight(1f)
                )
                StatPill(
                    "Delay",
                    settings.armDelaySeconds.toString() + "s",
                    Modifier.weight(1f)
                )
            }
        }

        SettingSectionCard(
            "Protezione",
            "Il core anti-pickpocket in modalità Tasca resta disponibile anche nel piano Free."
        ) {
            ChoiceRow(
                values = GuardMode.entries,
                selected = settings.mode,
                label = { if (it == GuardMode.POCKET) "Tasca" else "Borsa • Pro" },
                enabled = { it == GuardMode.POCKET || isPro },
                onSelected = onMode,
                onLocked = onPro
            )

            ChoiceRow(
                values = PocketSensitivity.entries,
                selected = settings.sensitivity,
                label = {
                    when (it) {
                        PocketSensitivity.LOW -> "Bassa"
                        PocketSensitivity.NORMAL -> "Normale"
                        PocketSensitivity.HIGH -> "Alta • Pro"
                    }
                },
                enabled = { it != PocketSensitivity.HIGH || isPro },
                onSelected = onSensitivity,
                onLocked = onPro
            )

            ChoiceRow(
                values = listOf(3, 5, 10, 15),
                selected = settings.armDelaySeconds,
                label = { it.toString() + "s" },
                enabled = { true },
                onSelected = onDelay,
                onLocked = onPro
            )
        }

        SettingSectionCard(
            "Journey Guard",
            "Il Free include sessioni da 30 minuti. Le sessioni lunghe e continue sono Pro."
        ) {
            ChoiceRow(
                values = JourneyDuration.entries,
                selected = settings.journeyDuration,
                label = {
                    when (it) {
                        JourneyDuration.MINUTES_30 -> "30m"
                        JourneyDuration.HOUR_1 -> "1h • Pro"
                        JourneyDuration.HOURS_2 -> "2h • Pro"
                        JourneyDuration.UNTIL_STOPPED -> "∞ • Pro"
                    }
                },
                enabled = {
                    it == JourneyDuration.MINUTES_30 || isPro
                },
                onSelected = onJourney,
                onLocked = onPro
            )
        }

        SettingSectionCard(
            "Allarme",
            "Sirena e vibrazione restano Free. Flash è una funzione Pro."
        ) {
            ToggleRow(
                "Sirena",
                "Riproduce il suono di allarme.",
                settings.soundEnabled,
                onSound
            )
            ToggleRow(
                "Vibrazione",
                "Fa vibrare il telefono durante l'allarme.",
                settings.vibrationEnabled,
                onVibration
            )
            ToggleRow(
                "Flash • Pro",
                if (capabilities.hasFlash) {
                    "Lampeggia durante l'allarme."
                } else {
                    "Non disponibile su questo dispositivo."
                },
                settings.flashEnabled && isPro,
                { enabled ->
                    if (enabled) {
                        showFlashDisclosure = true
                    } else {
                        onFlash(false)
                    }
                },
                enabled = capabilities.hasFlash && isPro
            )

            if (!isPro && capabilities.hasFlash) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPro
                ) {
                    Text("Sblocca flash con Pro")
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTestAlarm
            ) {
                Text("Test allarme 3 secondi")
            }
        }

        SettingSectionCard(
            "Sicurezza e affidabilità",
            "PIN sempre disponibile. Biometria e affidabilità avanzata sono Pro."
        ) {
            ToggleRow(
                "Biometria • Pro",
                "Usa impronta o volto per il disarmo.",
                settings.biometricEnabled && isPro,
                onBiometric,
                enabled = isPro
            )
            ToggleRow(
                "Affidabilità avanzata • Pro",
                "Migliora la continuità della protezione con un maggiore consumo.",
                settings.enhancedReliability && isPro,
                onReliability,
                enabled = isPro
            )

            if (!isPro) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPro
                ) {
                    Text("Scopri PockSecure Pro")
                }
            }
        }

        SettingSectionCard(
            "Privacy",
            "Trasparenza su sensori, dati locali, backup e acquisti."
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPrivacy
            ) {
                Text("Apri Privacy")
            }
        }
    }
}

@Composable
private fun SettingSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    SectionCard(
        title = title,
        subtitle = subtitle
    ) {
        content()
    }
}

@Composable
private fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    enabled: (T) -> Boolean,
    onSelected: (T) -> Unit,
    onLocked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        values.forEach { item ->
            val available = enabled(item)

            FilterChip(
                selected = item == selected,
                onClick = {
                    if (available) onSelected(item) else onLocked()
                },
                label = {
                    Text(
                        label(item),
                        fontWeight =
                            if (item == selected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                    )
                }
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onChanged,
            enabled = enabled
        )
    }
}
