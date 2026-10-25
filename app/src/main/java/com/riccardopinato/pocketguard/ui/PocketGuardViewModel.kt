package com.riccardopinato.pocketguard.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.riccardopinato.pocketguard.data.SettingsRepository
import com.riccardopinato.pocketguard.history.HistoryRepository
import com.riccardopinato.pocketguard.model.*
import com.riccardopinato.pocketguard.security.PinSecurityManager
import com.riccardopinato.pocketguard.security.SecurityRepository
import com.riccardopinato.pocketguard.sensor.SensorController
import com.riccardopinato.pocketguard.service.PocketGuardService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PocketGuardViewModel(
    private val appContext: Context,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val securityRepository: SecurityRepository,
    private val pinManager: PinSecurityManager
) : ViewModel() {

    val settings =
        settingsRepository.settingsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GuardSettings()
        )

    val onboardingCompleted =
        settingsRepository.onboardingCompletedFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    val guardState =
        settingsRepository.guardStateFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GuardState.DISARMED
        )

    val isArmed =
        settingsRepository.isArmedFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    val confidence =
        settingsRepository.confidenceFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val sessionEnd =
        settingsRepository.sessionEndFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0L
        )

    val hasPin =
        securityRepository.hasPinFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    val history =
        historyRepository.eventsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val calibration =
        settings
            .flatMapLatest {
                settingsRepository.calibrationFlow(it.mode)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )

    val capabilities =
        SensorController.detectCapabilities(appContext)

    fun startGuard() {
        val current = settings.value

        if (
            !hasPin.value ||
            !capabilities.canRun(current.mode)
        ) {
            return
        }

        startServiceAction(
            PocketGuardService.ACTION_START,
            foreground = true
        )
    }

    fun startCalibration() {
        val intent =
            Intent(appContext, PocketGuardService::class.java).apply {
                action = PocketGuardService.ACTION_CALIBRATE
                putExtra(
                    PocketGuardService.EXTRA_MODE,
                    settings.value.mode.name
                )
            }

        ContextCompat.startForegroundService(
            appContext,
            intent
        )
    }

    fun stopGuard() {
        startServiceAction(
            PocketGuardService.ACTION_STOP,
            foreground = false
        )
    }

    fun testAlarm() {
        startServiceAction(
            PocketGuardService.ACTION_TEST_ALARM,
            foreground = false
        )
    }

    fun verifyPinAndDisarm(
        pin: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok = pinManager.verify(pin)
            if (ok) stopGuard()
            onResult(ok)
        }
    }

    fun setPin(
        pin: String,
        confirmation: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok =
                pin == confirmation &&
                    pinManager.setPin(pin)
            onResult(ok)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clear()
        }
    }

    fun setMode(value: GuardMode) {
        viewModelScope.launch {
            settingsRepository.setMode(value)
        }
    }

    fun setJourney(value: JourneyDuration) {
        viewModelScope.launch {
            settingsRepository.setJourney(value)
        }
    }

    fun setSensitivity(value: PocketSensitivity) {
        viewModelScope.launch {
            settingsRepository.setSensitivity(value)
        }
    }

    fun setArmDelay(value: Int) {
        viewModelScope.launch {
            settingsRepository.setArmDelay(value)
        }
    }

    fun setSound(value: Boolean) {
        if (
            !value &&
            !settings.value.vibrationEnabled &&
            !settings.value.flashEnabled
        ) return

        viewModelScope.launch {
            settingsRepository.setSound(value)
        }
    }

    fun setVibration(value: Boolean) {
        if (
            !value &&
            !settings.value.soundEnabled &&
            !settings.value.flashEnabled
        ) return

        viewModelScope.launch {
            settingsRepository.setVibration(value)
        }
    }

    fun setFlash(value: Boolean) {
        if (
            !value &&
            !settings.value.soundEnabled &&
            !settings.value.vibrationEnabled
        ) return

        viewModelScope.launch {
            settingsRepository.setFlash(value)
        }
    }

    fun setBiometric(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometric(value)
        }
    }

    fun setReliability(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReliability(value)
        }
    }

    private fun startServiceAction(
        action: String,
        foreground: Boolean
    ) {
        val intent =
            Intent(appContext, PocketGuardService::class.java).apply {
                this.action = action
            }

        if (foreground) {
            ContextCompat.startForegroundService(appContext, intent)
        } else {
            appContext.startService(intent)
        }
    }

    class Factory(
        private val appContext: Context,
        private val settingsRepository: SettingsRepository,
        private val historyRepository: HistoryRepository,
        private val securityRepository: SecurityRepository,
        private val pinManager: PinSecurityManager
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(
                    PocketGuardViewModel::class.java
                )
            ) {
                return PocketGuardViewModel(
                    appContext = appContext.applicationContext,
                    settingsRepository = settingsRepository,
                    historyRepository = historyRepository,
                    securityRepository = securityRepository,
                    pinManager = pinManager
                ) as T
            }

            error("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
