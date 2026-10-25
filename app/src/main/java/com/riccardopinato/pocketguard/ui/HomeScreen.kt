package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.model.CalibrationProfile
import com.riccardopinato.pocketguard.model.GuardMode
import com.riccardopinato.pocketguard.model.GuardSettings
import com.riccardopinato.pocketguard.model.GuardState
import com.riccardopinato.pocketguard.model.JourneyDuration
import com.riccardopinato.pocketguard.model.SensorCapabilities
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    state: GuardState,
    armed: Boolean,
    confidence: Int,
    sessionEnd: Long,
    settings: GuardSettings,
    capabilities: SensorCapabilities,
    hasPin: Boolean,
    calibration: CalibrationProfile?,
    onStart: () -> Unit,
    onCalibrate: () -> Unit,
    onSecurity: () -> Unit,
    onDiagnostics: () -> Unit,
    onAuthenticatedDisarm: () -> Unit
) {
    val compatible = capabilities.canRun(settings.mode)

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = titleFor(state),
            subtitle = subtitleFor(state),
            badge = heroBadge(state),
            accent = heroColors(state)
        ) {
            if (state == GuardState.REMOVAL_SUSPECTED || state == GuardState.OWNER_CHECK) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { confidence.coerceIn(0, 100) / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                    Text(
                        "Controllo estrazione: $confidence%",
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    "Modalità",
                    if (settings.mode == GuardMode.POCKET) "Tasca" else "Borsa",
                    Modifier.weight(1f)
                )
                StatPill(
                    "Journey",
                    journeyLabel(settings.journeyDuration),
                    Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    "Sensibilità",
                    settings.sensitivity.name.lowercase().replaceFirstChar { it.uppercase() },
                    Modifier.weight(1f)
                )
                StatPill(
                    "Delay",
                    settings.armDelaySeconds.toString() + "s",
                    Modifier.weight(1f)
                )
            }

            if (sessionEnd > 0L) {
                Text(
                    "Sessione attiva fino alle " +
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(sessionEnd)),
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (armed) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                onClick = onAuthenticatedDisarm,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (state == GuardState.ALARM) {
                        "Autenticati e ferma allarme"
                    } else {
                        "Autenticati e disarma"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = compatible && hasPin,
                onClick = onStart,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    "Attiva Journey Guard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(
                modifier = Modifier.weight(1f),
                enabled = compatible,
                onClick = onCalibrate,
                shape = RoundedCornerShape(18.dp)
            ) { Text("Calibra") }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onDiagnostics,
                shape = RoundedCornerShape(18.dp)
            ) { Text("Diagnostica") }
        }

        if (!hasPin) {
            QuickActionCard(
                title = "Configura il PIN",
                text = "Ti serve un PIN locale prima di attivare la protezione.",
                accent = listOf(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.primaryContainer
                ),
                button = "Apri sicurezza",
                onAction = onSecurity
            )
        }

        if (calibration == null) {
            QuickActionCard(
                title = "Calibrazione consigliata",
                text = if (settings.mode == GuardMode.POCKET) {
                    "Metti il telefono nella tasca reale che usi di solito."
                } else {
                    "Metti il telefono nella borsa reale che usi di solito."
                },
                accent = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.tertiaryContainer
                ),
                button = "Calibra ora",
                onAction = onCalibrate
            )
        }

        if (!compatible) {
            QuickActionCard(
                title = "Modalità non compatibile",
                text = "Il dispositivo non ha tutti i sensori minimi richiesti.",
                accent = listOf(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.20f),
                    MaterialTheme.colorScheme.secondaryContainer
                ),
                button = "Apri diagnostica",
                onAction = onDiagnostics
            )
        }

        SectionCard(
            title = "Configurazione attiva",
            subtitle = "Le opzioni principali che userai durante il viaggio."
        ) {
            OverviewRow("Allarme", alarmLabel(settings))
            OverviewRow("Riarmo automatico", "Attivo dopo estrazione legittima")
            OverviewRow("Elaborazione", "Solo sul dispositivo")
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    text: String,
    accent: List<Color>,
    button: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(accent), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(16.dp)
            ) { Text(button) }
        }
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun titleFor(state: GuardState): String =
    when (state) {
        GuardState.DISARMED -> "Protezione spenta"
        GuardState.ARMING -> "Attivazione in corso"
        GuardState.CALIBRATING -> "Calibrazione in corso"
        GuardState.WAITING_FOR_POCKET -> "In attesa della tasca"
        GuardState.POCKET_CONFIRMED -> "Protetto"
        GuardState.REMOVAL_SUSPECTED -> "Possibile estrazione"
        GuardState.OWNER_CHECK -> "Verifica proprietario"
        GuardState.ALARM -> "Allarme attivo"
    }

private fun subtitleFor(state: GuardState): String =
    when (state) {
        GuardState.DISARMED -> "Pocket Guard è pronto, ma non sta monitorando il telefono."
        GuardState.ARMING -> "Hai ancora qualche secondo per riporre il telefono."
        GuardState.CALIBRATING -> "Mantieni il telefono nella posizione reale di utilizzo."
        GuardState.WAITING_FOR_POCKET -> "Sto aspettando una condizione stabile di tasca o borsa."
        GuardState.POCKET_CONFIRMED -> "La protezione smart è attiva e sta monitorando l'estrazione."
        GuardState.REMOVAL_SUSPECTED -> "Sono stati rilevati più segnali compatibili con un'estrazione."
        GuardState.OWNER_CHECK -> "Sei tu? Sblocca o autenticati per fermare l'allarme."
        GuardState.ALARM -> "Allarme attivo. Apri Sicurezza per disattivarlo."
    }

private fun heroBadge(state: GuardState): String =
    when (state) {
        GuardState.DISARMED -> "Stand-by"
        GuardState.ARMING -> "Ready"
        GuardState.CALIBRATING -> "Setup"
        GuardState.WAITING_FOR_POCKET -> "Watch"
        GuardState.POCKET_CONFIRMED -> "Safe"
        GuardState.REMOVAL_SUSPECTED -> "Alert"
        GuardState.OWNER_CHECK -> "Check"
        GuardState.ALARM -> "Alarm"
    }

private fun heroColors(state: GuardState): List<Color> =
    when (state) {
        GuardState.ALARM -> listOf(Color(0xFFFF5F6D), Color(0xFFFF9966))
        GuardState.REMOVAL_SUSPECTED,
        GuardState.OWNER_CHECK -> listOf(Color(0xFFFFA63D), Color(0xFFFF5F6D))
        GuardState.POCKET_CONFIRMED -> listOf(Color(0xFF12C2E9), Color(0xFF39D98A))
        else -> listOf(Color(0xFF4F6BFF), Color(0xFF12C2E9))
    }

private fun journeyLabel(duration: JourneyDuration): String =
    when (duration) {
        JourneyDuration.MINUTES_30 -> "30 min"
        JourneyDuration.HOUR_1 -> "1 ora"
        JourneyDuration.HOURS_2 -> "2 ore"
        JourneyDuration.UNTIL_STOPPED -> "∞"
    }

private fun alarmLabel(settings: GuardSettings): String {
    val active = mutableListOf<String>()
    if (settings.soundEnabled) active += "Sirena"
    if (settings.vibrationEnabled) active += "Vibrazione"
    if (settings.flashEnabled) active += "Flash"
    return if (active.isEmpty()) "Off" else active.joinToString(" + ")
}
