package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riccardopinato.pocketguard.model.GuardState
import com.riccardopinato.pocketguard.security.BiometricDisarmHelper

private enum class Page {
    HOME,
    SETTINGS,
    SECURITY,
    HISTORY,
    DIAGNOSTICS,
    PRO,
    PRIVACY
}

@Composable
fun PocketGuardApp(
    viewModel: PocketGuardViewModel,
    biometricHelper: BiometricDisarmHelper,
    onStartRequested: () -> Unit,
    onBuyProRequested: () -> Unit,
    onFlashEnableRequested: () -> Unit
) {
    var page by rememberSaveable { mutableStateOf(Page.HOME) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val billing by viewModel.billing.collectAsStateWithLifecycle()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val state by viewModel.guardState.collectAsStateWithLifecycle()
    val armed by viewModel.isArmed.collectAsStateWithLifecycle()
    val confidence by viewModel.confidence.collectAsStateWithLifecycle()
    val sessionEnd by viewModel.sessionEnd.collectAsStateWithLifecycle()
    val hasPin by viewModel.hasPin.collectAsStateWithLifecycle()
    val calibration by viewModel.calibration.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    if (!onboardingCompleted) {
        OnboardingScreen(
            mode = settings.mode,
            hasPin = hasPin,
            calibration = calibration,
            onMode = viewModel::setMode,
            onSetPin = viewModel::setPin,
            onCalibrate = viewModel::startCalibration,
            onComplete = viewModel::completeOnboarding
        )
        return
    }

    Scaffold { padding ->
        PocketGuardBackground(contentPadding = padding) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PockaraHeader(
                    currentPage = page,
                    isPro = billing.isPro,
                    onPageSelected = { page = it }
                )

                when (page) {
                    Page.HOME -> HomeScreen(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
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
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        settings = settings,
                        capabilities = viewModel.capabilities,
                        isPro = billing.isPro,
                        onMode = viewModel::setMode,
                        onJourney = viewModel::setJourney,
                        onSensitivity = viewModel::setSensitivity,
                        onDelay = viewModel::setArmDelay,
                        onSound = viewModel::setSound,
                        onVibration = viewModel::setVibration,
                        onFlash = { enabled ->
                            if (enabled) onFlashEnableRequested()
                            else viewModel.setFlash(false)
                        },
                        onBiometric = viewModel::setBiometric,
                        onReliability = viewModel::setReliability,
                        onTestAlarm = viewModel::testAlarm,
                        onPro = { page = Page.PRO },
                        onPrivacy = { page = Page.PRIVACY }
                    )

                    Page.SECURITY -> SecurityScreen(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        hasPin = hasPin,
                        alarmOrArmed = armed || state == GuardState.ALARM,
                        biometricEnabled = settings.biometricEnabled && billing.isPro,
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
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        events = history,
                        isPro = billing.isPro,
                        onClear = viewModel::clearHistory,
                        onPro = { page = Page.PRO }
                    )

                    Page.DIAGNOSTICS -> DiagnosticsScreen(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        capabilities = viewModel.capabilities,
                        calibration = calibration,
                        mode = settings.mode
                    )

                    Page.PRO -> PaywallScreen(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        billing = billing,
                        onBuy = onBuyProRequested,
                        onRestore = viewModel::restorePurchases
                    )

                    Page.PRIVACY -> PrivacyScreen(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PockaraHeader(
    currentPage: Page,
    isPro: Boolean,
    onPageSelected: (Page) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Pockara",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    pageSubtitle(currentPage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (currentPage != Page.HOME) {
                TextButton(onClick = { onPageSelected(Page.HOME) }) {
                    Text("Home")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PageChip("Home", currentPage == Page.HOME) {
                onPageSelected(Page.HOME)
            }
            PageChip("Impostazioni", currentPage == Page.SETTINGS) {
                onPageSelected(Page.SETTINGS)
            }
            PageChip("Sicurezza", currentPage == Page.SECURITY) {
                onPageSelected(Page.SECURITY)
            }
            PageChip("Cronologia", currentPage == Page.HISTORY) {
                onPageSelected(Page.HISTORY)
            }
            PageChip(
                if (isPro) "Pro ✓" else "Pro",
                currentPage == Page.PRO
            ) {
                onPageSelected(Page.PRO)
            }
            PageChip("Diagnostica", currentPage == Page.DIAGNOSTICS) {
                onPageSelected(Page.DIAGNOSTICS)
            }
        }
    }
}

private fun pageSubtitle(page: Page): String =
    when (page) {
        Page.HOME -> "Protezione smart anti-pickpocket"
        Page.SETTINGS -> "Protezione, Journey Guard e allarme"
        Page.SECURITY -> "PIN locale e biometria"
        Page.HISTORY -> "Eventi recenti della protezione"
        Page.DIAGNOSTICS -> "Sensori e calibrazione del dispositivo"
        Page.PRO -> "Sblocco Lifetime senza abbonamento"
        Page.PRIVACY -> "Dati locali e trasparenza"
    }
