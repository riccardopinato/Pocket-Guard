package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.model.CalibrationProfile
import com.riccardopinato.pocketguard.model.GuardMode

@Composable
fun OnboardingScreen(
    mode: GuardMode,
    hasPin: Boolean,
    calibration: CalibrationProfile?,
    onMode: (GuardMode) -> Unit,
    onSetPin: (String, String, (Boolean) -> Unit) -> Unit,
    onCalibrate: () -> Unit,
    onComplete: () -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var pin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("") }

    PocketGuardBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Pocket Guard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Configurazione iniziale",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (step + 1) / 4f },
                modifier = Modifier.fillMaxWidth()
            )

            when (step) {
                0 -> {
                    HeroCard(
                        title = "Protezione smart, senza complicazioni",
                        subtitle = "Controlla tasca o borsa e reagisce quando rileva un'estrazione sospetta.",
                        badge = "1/4"
                    ) {}
                    SectionCard(
                        title = "Pensata per essere veloce",
                        subtitle = "Attivala prima di un viaggio, un evento affollato o quando vuoi più tranquillità."
                    ) {
                        Text("Elaborazione locale • niente GPS • niente registrazioni audio • nessun account obbligatorio")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { step = 1 }
                    ) { Text("Inizia") }
                }

                1 -> {
                    HeroCard(
                        title = "Dove tieni il telefono?",
                        subtitle = "Scegli la modalità che userai più spesso. Potrai cambiarla quando vuoi.",
                        badge = "2/4"
                    ) {}
                    SectionCard(title = "Modalità predefinita") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = mode == GuardMode.POCKET,
                                onClick = { onMode(GuardMode.POCKET) },
                                label = { Text("Tasca") }
                            )
                            FilterChip(
                                selected = mode == GuardMode.BAG,
                                onClick = { onMode(GuardMode.BAG) },
                                label = { Text("Borsa") }
                            )
                        }
                        Text(
                            if (mode == GuardMode.POCKET) {
                                "Ottimizzata per telefono vicino al corpo."
                            } else {
                                "Ottimizzata per movimento e variazioni dentro una borsa."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { step = 0 }
                        ) { Text("Indietro") }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { step = 2 }
                        ) { Text("Continua") }
                    }
                }

                2 -> {
                    HeroCard(
                        title = "Proteggi il disarmo",
                        subtitle = if (hasPin) "Il PIN è già configurato."
                        else "Crea un PIN locale da 4 a 6 cifre.",
                        badge = "3/4"
                    ) {}
                    SectionCard(title = if (hasPin) "PIN pronto" else "Crea PIN") {
                        if (!hasPin) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = pin,
                                onValueChange = { pin = it.filter(Char::isDigit).take(6) },
                                label = { Text("PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = confirm,
                                onValueChange = { confirm = it.filter(Char::isDigit).take(6) },
                                label = { Text("Conferma PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true
                            )
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onSetPin(pin, confirm) { ok ->
                                        pinMessage = if (ok) "PIN salvato." else "Controlla PIN e conferma."
                                    }
                                }
                            ) { Text("Salva PIN") }
                            if (pinMessage.isNotBlank()) {
                                Text(
                                    pinMessage,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text("Il disarmo è protetto e la biometria può essere usata quando disponibile.")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { step = 1 }
                        ) { Text("Indietro") }
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = hasPin,
                            onClick = { step = 3 }
                        ) { Text("Continua") }
                    }
                }

                else -> {
                    HeroCard(
                        title = "Quasi pronto",
                        subtitle = "La calibrazione rende Pocket Guard più adatto alla tua tasca o borsa reale.",
                        badge = "4/4",
                        accent = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ) {}
                    SectionCard(
                        title = if (calibration == null) "Calibrazione consigliata" else "Calibrazione pronta",
                        subtitle = if (calibration == null) {
                            "Puoi farla ora oppure completare il setup e farla dalla Home."
                        } else {
                            "Il profilo di questa modalità è già stato salvato."
                        }
                    ) {
                        if (calibration == null) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onCalibrate
                            ) { Text("Calibra adesso") }
                        } else {
                            Text("Profilo di calibrazione già disponibile.")
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasPin,
                        onClick = onComplete
                    ) { Text("Apri Pocket Guard") }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { step = 2 }
                    ) { Text("Indietro") }
                }
            }
        }
    }
}
