package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SecurityScreen(
    contentPadding: PaddingValues,
    hasPin: Boolean,
    alarmOrArmed: Boolean,
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    onSetPin: (String, String, (Boolean) -> Unit) -> Unit,
    onVerifyAndDisarm: (String, (Boolean) -> Unit) -> Unit,
    onBiometricDisarm: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(contentPadding).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard("Sicurezza locale", "PIN e biometria restano sul dispositivo.", "Safe") {}

        SectionCard(
            title = if (hasPin) "PIN configurato" else "Crea il tuo PIN",
            subtitle = "Usa da 4 a 6 cifre per proteggere il disarmo."
        ) {
            if (!hasPin) {
                PinField(pin, { pin = it.filter(Char::isDigit).take(6) }, "PIN")
                PinField(confirm, { confirm = it.filter(Char::isDigit).take(6) }, "Conferma PIN")
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    onSetPin(pin, confirm) { ok ->
                        message = if (ok) "PIN salvato." else "PIN non valido o conferma diversa."
                    }
                }) { Text("Salva PIN") }
            } else {
                Text("Il PIN è pronto per proteggere il disarmo di Pocket Guard.")
            }
        }

        if (hasPin && alarmOrArmed) {
            SectionCard("Disarmo protetto", "Conferma che sei il proprietario.") {
                PinField(pin, { pin = it.filter(Char::isDigit).take(6) }, "PIN")
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    onVerifyAndDisarm(pin) { ok -> message = if (ok) "Disarmo autorizzato." else "PIN errato." }
                }) { Text("Verifica e disarma") }

                if (biometricEnabled && biometricAvailable) {
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onBiometricDisarm) {
                        Text("Usa biometria")
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PinField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true
    )
}
