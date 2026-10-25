package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.model.CalibrationProfile
import com.riccardopinato.pocketguard.model.GuardMode
import com.riccardopinato.pocketguard.model.SensorCapabilities

@Composable
fun DiagnosticsScreen(
    contentPadding: PaddingValues,
    capabilities: SensorCapabilities,
    calibration: CalibrationProfile?,
    mode: GuardMode
) {
    Column(
        modifier = Modifier.padding(contentPadding).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            "Diagnostica dispositivo",
            "Compatibilità sensori e profilo di calibrazione.",
            if (mode == GuardMode.POCKET) "Tasca" else "Borsa",
            accent = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
        ) {}

        SectionCard("Compatibilità sensori", "Disponibilità hardware del dispositivo.") {
            DiagnosticRow("Prossimità", capabilities.hasProximity, capabilities.proximityWakeUp)
            DiagnosticRow("Luce", capabilities.hasLight, capabilities.lightWakeUp)
            DiagnosticRow("Accelerometro", capabilities.hasAccelerometer, capabilities.accelerometerWakeUp)
            DiagnosticRow("Giroscopio", capabilities.hasGyroscope, capabilities.gyroscopeWakeUp)
            DiagnosticValueRow("Flash", if (capabilities.hasFlash) "Sì" else "No")
        }

        SectionCard(
            title = "Profilo " + if (mode == GuardMode.POCKET) "Tasca" else "Borsa",
            subtitle = "Valori ottenuti dalla calibrazione reale."
        ) {
            if (calibration == null) {
                Text("Nessuna calibrazione salvata.")
            } else {
                DiagnosticValueRow("Campioni", calibration.sampleCount.toString())
                DiagnosticValueRow("Lux ingresso", "%.1f".format(calibration.enterLux))
                DiagnosticValueRow("Lux uscita", "%.1f".format(calibration.exitLux))
                DiagnosticValueRow("Rumore movimento", "%.3f".format(calibration.motionNoise))
                DiagnosticValueRow("Rumore rotazione", "%.3f".format(calibration.rotationNoise))
                DiagnosticValueRow("Giroscopio calibrato", if (calibration.hasGyroscopeData) "Sì" else "No")
            }
        }
    }
}

@Composable
private fun DiagnosticRow(name: String, available: Boolean, wakeUp: Boolean) {
    DiagnosticValueRow(
        name,
        if (!available) "Assente" else if (wakeUp) "Disponibile • Wake-up" else "Disponibile"
    )
}

@Composable
private fun DiagnosticValueRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
