package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        modifier = Modifier
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (hasPin) {
                        "PIN configurato"
                    } else {
                        "Crea PIN 4-6 cifre"
                    }
                )

                if (!hasPin) {
                    PinField(
                        value = pin,
                        onValueChange = {
                            pin = it.filter(Char::isDigit).take(6)
                        },
                        label = "PIN"
                    )

                    PinField(
                        value = confirm,
                        onValueChange = {
                            confirm = it.filter(Char::isDigit).take(6)
                        },
                        label = "Conferma PIN"
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSetPin(pin, confirm) { ok ->
                                message =
                                    if (ok) {
                                        "PIN salvato."
                                    } else {
                                        "PIN non valido o conferma diversa."
                                    }
                            }
                        }
                    ) {
                        Text("SALVA PIN")
                    }
                }
            }
        }

        if (hasPin && alarmOrArmed) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Autenticazione per disarmo")

                    PinField(
                        value = pin,
                        onValueChange = {
                            pin = it.filter(Char::isDigit).take(6)
                        },
                        label = "PIN"
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onVerifyAndDisarm(pin) { ok ->
                                message =
                                    if (ok) {
                                        "Disarmo autorizzato."
                                    } else {
                                        "PIN errato."
                                    }
                            }
                        }
                    ) {
                        Text("VERIFICA E DISARMA")
                    }

                    if (biometricEnabled && biometricAvailable) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onBiometricDisarm
                        ) {
                            Text("USA BIOMETRIA")
                        }
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            Text(message)
        }
    }
}

@Composable
private fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword
        ),
        singleLine = true
    )
}
