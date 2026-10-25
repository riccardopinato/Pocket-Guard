package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        modifier = Modifier
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Compatibilità sensori",
                    style = MaterialTheme.typography.titleMedium
                )

                DiagnosticRow(
                    "Prossimità",
                    capabilities.hasProximity,
                    capabilities.proximityWakeUp
                )

                DiagnosticRow(
                    "Luce",
                    capabilities.hasLight,
                    capabilities.lightWakeUp
                )

                DiagnosticRow(
                    "Accelerometro",
                    capabilities.hasAccelerometer,
                    capabilities.accelerometerWakeUp
                )

                DiagnosticRow(
                    "Giroscopio",
                    capabilities.hasGyroscope,
                    capabilities.gyroscopeWakeUp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Flash")
                    Text(if (capabilities.hasFlash) "Sì" else "No")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Profilo ${if (mode == GuardMode.POCKET) "Tasca" else "Borsa"}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (calibration == null) {
                    Text("Nessuna calibrazione salvata.")
                } else {
                    Text("Campioni: ${calibration.sampleCount}")
                    Text("Lux ingresso: ${"%.1f".format(calibration.enterLux)}")
                    Text("Lux uscita: ${"%.1f".format(calibration.exitLux)}")
                    Text(
                        "Rumore movimento: ${"%.3f".format(calibration.motionNoise)}"
                    )
                    Text(
                        "Rumore rotazione: ${"%.3f".format(calibration.rotationNoise)}"
                    )
                    Text(
                        "Giroscopio calibrato: " +
                            if (calibration.hasGyroscopeData) "Sì" else "No"
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    name: String,
    available: Boolean,
    wakeUp: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name)

        Text(
            if (!available) {
                "Assente"
            } else if (wakeUp) {
                "Disponibile • Wake-up"
            } else {
                "Disponibile"
            }
        )
    }
}
