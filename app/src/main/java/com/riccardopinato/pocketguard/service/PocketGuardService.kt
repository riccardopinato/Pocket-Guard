package com.riccardopinato.pocketguard.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.riccardopinato.pocketguard.PocketGuardApplication
import com.riccardopinato.pocketguard.alarm.AlarmController
import com.riccardopinato.pocketguard.billing.CommercialAccess
import com.riccardopinato.pocketguard.calibration.CalibrationCollector
import com.riccardopinato.pocketguard.data.SettingsRepository
import com.riccardopinato.pocketguard.detection.SmartProtectionEngine
import com.riccardopinato.pocketguard.history.HistoryRepository
import com.riccardopinato.pocketguard.model.*
import com.riccardopinato.pocketguard.notification.PocketGuardNotificationManager
import com.riccardopinato.pocketguard.power.ReliabilityWakeLock
import com.riccardopinato.pocketguard.sensor.SamplingMode
import com.riccardopinato.pocketguard.sensor.SensorController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class PocketGuardService : Service() {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var notifications: PocketGuardNotificationManager
    private lateinit var alarm: AlarmController
    private lateinit var wakeLock: ReliabilityWakeLock
    private lateinit var sensors: SensorController

    private var settings = GuardSettings()
    private var profile: CalibrationProfile? = null
    private var engine: SmartProtectionEngine? = null

    private var activationJob: Job? = null
    private var ownerCheckJob: Job? = null
    private var journeyJob: Job? = null
    private var calibrationJob: Job? = null
    private var calibrationCollector: CalibrationCollector? = null

    private var receiverRegistered = false

    private val ownerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                ownerVerified("Android ACTION_USER_PRESENT")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val app = application as PocketGuardApplication
        settingsRepository = app.settingsRepository
        historyRepository = app.historyRepository

        notifications = PocketGuardNotificationManager(this)
        alarm = AlarmController(this, scope)
        wakeLock = ReliabilityWakeLock(this)
        sensors = SensorController(this, ::handleSnapshot)

        notifications.createChannels()
        registerOwnerReceiver()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START -> startProtection()
            ACTION_CALIBRATE ->
                startCalibration(parseMode(intent.getStringExtra(EXTRA_MODE)))
            ACTION_STOP -> stopProtection("Stopped by authenticated UI")
            ACTION_OWNER_VERIFIED ->
                ownerVerified("Authenticated in app")
            ACTION_TEST_ALARM -> testAlarm()
            null -> restoreIfNeeded()
        }

        return START_STICKY
    }

    private fun startProtection() {
        cancelJobs()
        startForegroundNow(GuardState.ARMING)

        scope.launch {
            settings =
                applyCommercialPolicy(
                    settingsRepository.settingsFlow.first()
                )

            if (!sensors.capabilities.canRun(settings.mode)) {
                historyRepository.add(
                    "ERROR",
                    "Required sensors unavailable for ${settings.mode}"
                )
                stopProtection("Sensor requirements not met")
                return@launch
            }

            profile =
                settingsRepository.calibrationFlow(settings.mode).first()
                    ?: defaultProfile(settings.mode)

            settingsRepository.setArmed(true)
            settingsRepository.setState(GuardState.ARMING)

            historyRepository.add(
                "ARM",
                "Mode=${settings.mode}; Journey=${settings.journeyDuration}"
            )

            scheduleJourneyEnd()

            activationJob = scope.launch {
                delay(settings.armDelaySeconds * 1_000L)

                engine = SmartProtectionEngine(
                    settings = settings,
                    profile = requireNotNull(profile)
                )

                sensors.start(SamplingMode.NORMAL)

                transitionTo(
                    GuardState.WAITING_FOR_POCKET,
                    0,
                    "Armed"
                )
            }
        }
    }

    private fun startCalibration(mode: GuardMode) {
        cancelJobs()
        startForegroundNow(GuardState.CALIBRATING)

        scope.launch {
            if (!sensors.capabilities.canRun(mode)) {
                historyRepository.add(
                    "CALIBRATION_ERROR",
                    "Sensors unavailable for $mode"
                )
                stopProtection("Calibration unsupported")
                return@launch
            }

            settingsRepository.setArmed(false)
            settingsRepository.setState(GuardState.CALIBRATING)

            calibrationCollector = CalibrationCollector(mode)
            sensors.start(SamplingMode.NORMAL)

            historyRepository.add("CALIBRATION_START", "Mode=$mode")

            calibrationJob = scope.launch {
                delay(CALIBRATION_MS)

                val collector = calibrationCollector ?: return@launch
                val result = collector.build(System.currentTimeMillis())

                settingsRepository.saveCalibration(result)

                historyRepository.add(
                    "CALIBRATION_OK",
                    "Mode=$mode; samples=${result.sampleCount}; lux=${result.enterLux}/${result.exitLux}"
                )

                calibrationCollector = null
                sensors.stop()

                settingsRepository.setState(GuardState.DISARMED)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun handleSnapshot(snapshot: SensorSnapshot) {
        calibrationCollector?.add(snapshot)

        val activeEngine = engine ?: return
        val decision = activeEngine.process(snapshot)

        scope.launch {
            settingsRepository.setConfidence(decision.confidenceScore)
        }

        if (decision.changed) {
            handleDecision(decision)
        }
    }

    private fun handleDecision(decision: DetectionDecision) {
        when (decision.state) {
            GuardState.POCKET_CONFIRMED -> {
                sensors.setSamplingMode(SamplingMode.LOW_POWER)

                if (settings.enhancedReliability) {
                    wakeLock.acquire()
                }

                transitionTo(
                    decision.state,
                    decision.confidenceScore,
                    decision.reason
                )
            }

            GuardState.REMOVAL_SUSPECTED -> {
                wakeLock.release()
                sensors.setSamplingMode(SamplingMode.ALERT)

                transitionTo(
                    decision.state,
                    decision.confidenceScore,
                    decision.reason
                )
            }

            GuardState.OWNER_CHECK -> {
                wakeLock.release()
                sensors.setSamplingMode(SamplingMode.ALERT)

                transitionTo(
                    decision.state,
                    decision.confidenceScore,
                    decision.reason
                )

                beginOwnerCheck()
            }

            GuardState.WAITING_FOR_POCKET -> {
                wakeLock.release()
                sensors.setSamplingMode(SamplingMode.NORMAL)

                transitionTo(
                    decision.state,
                    decision.confidenceScore,
                    decision.reason
                )
            }

            GuardState.ALARM -> triggerAlarm(decision.reason)

            else ->
                transitionTo(
                    decision.state,
                    decision.confidenceScore,
                    decision.reason
                )
        }
    }

    private fun beginOwnerCheck() {
        ownerCheckJob?.cancel()

        ownerCheckJob = scope.launch {
            delay(settings.ownerGraceMs)

            val decision =
                engine?.ownerCheckExpired() ?: return@launch

            triggerAlarm(decision.reason)
        }
    }

    private fun ownerVerified(source: String) {
        if (engine?.currentState() != GuardState.OWNER_CHECK) return

        ownerCheckJob?.cancel()
        ownerCheckJob = null

        alarm.stop()
        notifications.cancelAlarm()
        wakeLock.release()

        val decision = engine?.ownerVerified() ?: return

        sensors.setSamplingMode(SamplingMode.NORMAL)

        scope.launch {
            historyRepository.add("OWNER_OK", source)
        }

        transitionTo(
            decision.state,
            0,
            decision.reason
        )
    }

    private fun triggerAlarm(reason: String) {
        ownerCheckJob?.cancel()
        ownerCheckJob = null

        engine?.forceState(GuardState.ALARM)
        wakeLock.release()
        sensors.setSamplingMode(SamplingMode.ALERT)

        alarm.start(settings)
        notifications.showAlarm()
        notifications.update(GuardState.ALARM, 100)

        scope.launch {
            settingsRepository.setLastAlarm(System.currentTimeMillis())
            settingsRepository.setState(GuardState.ALARM)
            settingsRepository.setConfidence(100)
            historyRepository.add("ALARM", reason)
        }
    }

    private fun testAlarm() {
        scope.launch {
            val currentSettings =
                applyCommercialPolicy(
                    settingsRepository.settingsFlow.first()
                )
            val armed = settingsRepository.isArmedFlow.first()

            if (armed) {
                historyRepository.add(
                    "TEST_SKIPPED",
                    "Cannot test while protection is armed"
                )
                return@launch
            }

            alarm.test(
                settings = currentSettings,
                durationMs = 3_000L,
                onFinished = {
                    stopSelf()
                }
            )

            historyRepository.add("TEST_ALARM", "3 second test")
        }
    }

    private fun scheduleJourneyEnd() {
        journeyJob?.cancel()

        val duration = settings.journeyDuration.durationMs
        if (duration == null) {
            scope.launch {
                settingsRepository.setSessionEnd(0L)
            }
            return
        }

        val endAt = System.currentTimeMillis() + duration

        scope.launch {
            settingsRepository.setSessionEnd(endAt)
        }

        journeyJob = scope.launch {
            delay(duration)
            historyRepository.add(
                "JOURNEY_END",
                "Protection session expired"
            )
            stopProtection("Journey session completed")
        }
    }

    private fun restoreIfNeeded() {
        scope.launch {
            if (settingsRepository.isArmedFlow.first()) {
                startProtection()
            } else {
                stopSelf()
            }
        }
    }

    private fun stopProtection(reason: String) {
        cancelJobs()

        sensors.stop()
        engine = null
        calibrationCollector = null

        alarm.stop()
        wakeLock.release()
        notifications.cancelAlarm()

        scope.launch {
            settingsRepository.setArmed(false)
            settingsRepository.setState(GuardState.DISARMED)
            settingsRepository.setConfidence(0)
            settingsRepository.setSessionEnd(0L)
            historyRepository.add("DISARM", reason)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun transitionTo(
        state: GuardState,
        confidence: Int,
        reason: String
    ) {
        notifications.update(state, confidence)

        scope.launch {
            settingsRepository.setState(state)
            settingsRepository.setConfidence(confidence)
            historyRepository.add(
                state.name,
                "confidence=$confidence; $reason"
            )
        }
    }

    private fun startForegroundNow(state: GuardState) {
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }

        ServiceCompat.startForeground(
            this,
            PocketGuardNotificationManager.SERVICE_NOTIFICATION_ID,
            notifications.buildService(state),
            type
        )
    }

    private fun registerOwnerReceiver() {
        if (receiverRegistered) return

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        ContextCompat.registerReceiver(
            this,
            ownerReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        receiverRegistered = true
    }

    private fun unregisterOwnerReceiver() {
        if (!receiverRegistered) return
        runCatching { unregisterReceiver(ownerReceiver) }
        receiverRegistered = false
    }

    private fun cancelJobs() {
        activationJob?.cancel()
        ownerCheckJob?.cancel()
        journeyJob?.cancel()
        calibrationJob?.cancel()

        activationJob = null
        ownerCheckJob = null
        journeyJob = null
        calibrationJob = null
    }

    private fun defaultProfile(mode: GuardMode): CalibrationProfile =
        CalibrationProfile(
            mode = mode,
            createdAt = 0L,
            enterLux = 5f,
            exitLux = 12f,
            motionNoise = 0.03f,
            rotationNoise = 0.05f,
            sampleCount = 0,
            hasLightData = sensors.capabilities.hasLight,
            hasGyroscopeData = sensors.capabilities.hasGyroscope
        )

    private fun parseMode(raw: String?): GuardMode =
        raw?.let {
            runCatching { GuardMode.valueOf(it) }.getOrNull()
        } ?: GuardMode.POCKET

    override fun onDestroy() {
        cancelJobs()
        sensors.stop()
        alarm.stop()
        wakeLock.release()
        unregisterOwnerReceiver()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.riccardopinato.pocketguard.START"
        const val ACTION_STOP = "com.riccardopinato.pocketguard.STOP"
        const val ACTION_CALIBRATE = "com.riccardopinato.pocketguard.CALIBRATE"
        const val ACTION_OWNER_VERIFIED =
            "com.riccardopinato.pocketguard.OWNER_VERIFIED"
        const val ACTION_TEST_ALARM =
            "com.riccardopinato.pocketguard.TEST_ALARM"
        const val EXTRA_MODE = "extra_mode"
        private const val CALIBRATION_MS = 6_000L
    }
}
