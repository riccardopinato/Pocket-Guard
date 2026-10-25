package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.model.*
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
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    titleFor(state),
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    subtitleFor(state),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (
                    state == GuardState.REMOVAL_SUSPECTED ||
                    state == GuardState.OWNER_CHECK
                ) {
                    LinearProgressIndicator(
                        progress = {
                            confidence.coerceIn(0, 100) / 100f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Extraction Confidence: $confidence%")
                }

                if (sessionEnd > 0L) {
                    Text(
                        "Sessione fino alle " +
                            DateFormat.getTimeInstance(DateFormat.SHORT)
                                .format(Date(sessionEnd))
                    )
                }
            }
        }

        if (!hasPin) {
            InfoCard(
                title = "Configura la sicurezza",
                text =
                    "Prima di armare Pocket Guard imposta un PIN locale di 4-6 cifre.",
                action = "CONFIGURA PIN",
                onAction = onSecurity
            )
        }

        if (calibration == null) {
            InfoCard(
                title = "Calibrazione consigliata",
                text =
                    "Metti il telefono nella " +
                        if (settings.mode == GuardMode.POCKET) {
                            "tasca che usi normalmente."
                        } else {
                            "borsa che usi normalmente."
                        },
                action = "CALIBRA",
                onAction = onCalibrate
            )
        }

        val compatible = capabilities.canRun(settings.mode)

        if (!compatible) {
            InfoCard(
                title = "Modalità non compatibile",
                text =
                    "Il dispositivo non dispone dei sensori minimi richiesti.",
                action = "DIAGNOSTICA",
                onAction = onDiagnostics
            )
        }

        if (armed) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAuthenticatedDisarm
            ) {
                Text(
                    if (state == GuardState.ALARM) {
                        "AUTENTICATI E FERMA ALLARME"
                    } else {
                        "AUTENTICATI E DISARMA"
                    }
                )
            }
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = compatible && hasPin,
                onClick = onStart
            ) {
                Text("ATTIVA JOURNEY GUARD")
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = compatible,
                onClick = onCalibrate
            ) {
                Text(
                    "RICALIBRA " +
                        if (settings.mode == GuardMode.POCKET) {
                            "TASCA"
                        } else {
                            "BORSA"
                        }
                )
            }
        }

        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDiagnostics
        ) {
            Text("DIAGNOSTICA DISPOSITIVO")
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    text: String,
    action: String,
    onAction: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text)
            OutlinedButton(onClick = onAction) {
                Text(action)
            }
        }
    }
}

private fun titleFor(state: GuardState): String =
    when (state) {
        GuardState.DISARMED -> "Protezione spenta"
        GuardState.ARMING -> "Attivazione in corso"
        GuardState.CALIBRATING -> "Calibrazione in corso"
        GuardState.WAITING_FOR_POCKET -> "In attesa"
        GuardState.POCKET_CONFIRMED -> "Protetto"
        GuardState.REMOVAL_SUSPECTED -> "Estrazione sospetta"
        GuardState.OWNER_CHECK -> "Verifica proprietario"
        GuardState.ALARM -> "ALLARME ATTIVO"
    }

private fun subtitleFor(state: GuardState): String =
    when (state) {
        GuardState.DISARMED ->
            "Pocket Guard non sta monitorando il telefono."
        GuardState.ARMING ->
            "Hai qualche secondo per riporre il telefono."
        GuardState.CALIBRATING ->
            "Mantieni il telefono nella posizione reale di utilizzo."
        GuardState.WAITING_FOR_POCKET ->
            "In attesa di una condizione stabile di tasca o borsa."
        GuardState.POCKET_CONFIRMED ->
            "Smart Protection Engine sta monitorando l'estrazione."
        GuardState.REMOVAL_SUSPECTED ->
            "Più sensori stanno indicando una possibile estrazione."
        GuardState.OWNER_CHECK ->
            "Sblocca il telefono nella finestra di grazia se sei tu."
        GuardState.ALARM ->
            "Autenticati nell'app per disattivare la protezione."
    }
