package com.riccardopinato.pocketguard.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riccardopinato.pocketguard.model.GuardState
import com.riccardopinato.pocketguard.security.BiometricDisarmHelper

private enum class Page {
    HOME,
    SETTINGS,
    SECURITY,
    HISTORY,
    DIAGNOSTICS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketGuardApp(
    viewModel: PocketGuardViewModel,
    biometricHelper: BiometricDisarmHelper,
    onStartRequested: () -> Unit,
    onFlashEnableRequested: () -> Unit
) {
    var page by rememberSaveable {
        mutableStateOf(Page.HOME)
    }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val state by viewModel.guardState.collectAsStateWithLifecycle()
    val armed by viewModel.isArmed.collectAsStateWithLifecycle()
    val confidence by viewModel.confidence.collectAsStateWithLifecycle()
    val sessionEnd by viewModel.sessionEnd.collectAsStateWithLifecycle()
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle()
    val calibration by viewModel.calibration.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (page) {
                            Page.HOME -> "Pocket Guard"
                            Page.SETTINGS -> "Impostazioni"
                            Page.SECURITY -> "Sicurezza"
                            Page.HISTORY -> "Cronologia"
                            Page.DIAGNOSTICS -> "Diagnostica"
                        }
                    )
                },
                navigationIcon = {
                    if (page != Page.HOME) {
                        TextButton(onClick = { page = Page.HOME }) {
                            Text("Indietro")
                        }
                    }
                },
                actions = {
                    if (page == Page.HOME) {
                        TextButton(onClick = { page = Page.HISTORY }) {
                            Text("Cronologia")
                        }
                        TextButton(onClick = { page = Page.SECURITY }) {
                            Text("Sicurezza")
                        }
                        TextButton(onClick = { page = Page.SETTINGS }) {
                            Text("Impostazioni")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (page) {
            Page.HOME -> HomeScreen(
                contentPadding = padding,
                state = state,
                armed = armed,
                confidence = confidence,
                sessionEnd = sessionEnd,
                settings = settings,
                capabilities = viewModel.capabilities,
                hasPin = hasPin,
                calibration = calibration,
                onStart = onStartRequested,
                onCalibrate = viewModel::startCalibration,
                onSecurity = { page = Page.SECURITY },
                onDiagnostics = { page = Page.DIAGNOSTICS },
                onAuthenticatedDisarm = { page = Page.SECURITY }
            )

            Page.SETTINGS -> SettingsScreen(
                contentPadding = padding,
                settings = settings,
                capabilities = viewModel.capabilities,
                onMode = viewModel::setMode,
                onJourney = viewModel::setJourney,
                onSensitivity = viewModel::setSensitivity,
                onDelay = viewModel::setArmDelay,
                onSound = viewModel::setSound,
                onVibration = viewModel::setVibration,
                onFlash = { enabled ->
                    if (enabled) {
                        onFlashEnableRequested()
                    } else {
                        viewModel.setFlash(false)
                    }
                },
                onBiometric = viewModel::setBiometric,
                onReliability = viewModel::setReliability,
                onTestAlarm = viewModel::testAlarm
            )

            Page.SECURITY -> SecurityScreen(
                contentPadding = padding,
                hasPin = hasPin,
                alarmOrArmed = armed || state == GuardState.ALARM,
                biometricEnabled = settings.biometricEnabled,
                biometricAvailable = biometricHelper.canAuthenticate(),
                onSetPin = viewModel::setPin,
                onVerifyAndDisarm = viewModel::verifyPinAndDisarm,
                onBiometricDisarm = {
                    biometricHelper.authenticate(
                        onSuccess = { viewModel.stopGuard() },
                        onFailure = {}
                    )
                }
            )

            Page.HISTORY -> HistoryScreen(
                contentPadding = padding,
                events = history,
                onClear = viewModel::clearHistory
            )

            Page.DIAGNOSTICS -> DiagnosticsScreen(
                contentPadding = padding,
                capabilities = viewModel.capabilities,
                calibration = calibration,
                mode = settings.mode
            )
        }
    }
}
