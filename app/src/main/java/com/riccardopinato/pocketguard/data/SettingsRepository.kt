package com.riccardopinato.pocketguard.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.riccardopinato.pocketguard.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(
    name = "pocket_guard_settings"
)
private val Context.runtimeDataStore by preferencesDataStore(
    name = "pocket_guard_runtime"
)

class SettingsRepository(
    private val context: Context
) {
    private object K {
        val MODE = stringPreferencesKey("mode")
        val ARM_DELAY = intPreferencesKey("arm_delay")
        val SENSITIVITY = stringPreferencesKey("sensitivity")
        val JOURNEY = stringPreferencesKey("journey")
        val STABLE_MS = longPreferencesKey("stable_ms")
        val REMOVAL_MS = longPreferencesKey("removal_ms")
        val OWNER_GRACE_MS = longPreferencesKey("owner_grace_ms")
        val SOUND = booleanPreferencesKey("sound")
        val VIBRATION = booleanPreferencesKey("vibration")
        val FLASH = booleanPreferencesKey("flash")
        val BIOMETRIC = booleanPreferencesKey("biometric")
        val RELIABILITY = booleanPreferencesKey("reliability")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    private object R {
        val ARMED = booleanPreferencesKey("armed")
        val STATE = stringPreferencesKey("state")
        val SESSION_END = longPreferencesKey("session_end")
        val CONFIDENCE = intPreferencesKey("confidence")
        val LAST_ALARM = longPreferencesKey("last_alarm")
    }

    val settingsFlow: Flow<GuardSettings> =
        context.settingsDataStore.data.map { p ->
            GuardSettings(
                mode = enumValue(p[K.MODE], GuardMode.POCKET),
                armDelaySeconds = p[K.ARM_DELAY] ?: 5,
                sensitivity = enumValue(
                    p[K.SENSITIVITY],
                    PocketSensitivity.NORMAL
                ),
                journeyDuration = enumValue(
                    p[K.JOURNEY],
                    JourneyDuration.UNTIL_STOPPED
                ),
                pocketStableMs = p[K.STABLE_MS] ?: 1_200L,
                removalWindowMs = p[K.REMOVAL_MS] ?: 1_600L,
                ownerGraceMs = p[K.OWNER_GRACE_MS] ?: 2_500L,
                soundEnabled = p[K.SOUND] ?: true,
                vibrationEnabled = p[K.VIBRATION] ?: true,
                flashEnabled = p[K.FLASH] ?: false,
                biometricEnabled = p[K.BIOMETRIC] ?: true,
                enhancedReliability = p[K.RELIABILITY] ?: false
            )
        }

    val onboardingCompletedFlow: Flow<Boolean> =
        context.settingsDataStore.data.map { it[K.ONBOARDING_DONE] ?: false }

    val isArmedFlow: Flow<Boolean> =
        context.runtimeDataStore.data.map { it[R.ARMED] ?: false }

    val guardStateFlow: Flow<GuardState> =
        context.runtimeDataStore.data.map {
            enumValue(it[R.STATE], GuardState.DISARMED)
        }

    val sessionEndFlow: Flow<Long> =
        context.runtimeDataStore.data.map { it[R.SESSION_END] ?: 0L }

    val confidenceFlow: Flow<Int> =
        context.runtimeDataStore.data.map { it[R.CONFIDENCE] ?: 0 }

    val lastAlarmFlow: Flow<Long> =
        context.runtimeDataStore.data.map { it[R.LAST_ALARM] ?: 0L }

    fun calibrationFlow(mode: GuardMode): Flow<CalibrationProfile?> =
        context.settingsDataStore.data.map { readCalibration(it, mode) }

    suspend fun setMode(value: GuardMode) =
        context.settingsDataStore.edit { it[K.MODE] = value.name }

    suspend fun setArmDelay(value: Int) =
        context.settingsDataStore.edit { it[K.ARM_DELAY] = value }

    suspend fun setSensitivity(value: PocketSensitivity) =
        context.settingsDataStore.edit { it[K.SENSITIVITY] = value.name }

    suspend fun setJourney(value: JourneyDuration) =
        context.settingsDataStore.edit { it[K.JOURNEY] = value.name }

    suspend fun setSound(value: Boolean) =
        context.settingsDataStore.edit { it[K.SOUND] = value }

    suspend fun setVibration(value: Boolean) =
        context.settingsDataStore.edit { it[K.VIBRATION] = value }

    suspend fun setFlash(value: Boolean) =
        context.settingsDataStore.edit { it[K.FLASH] = value }

    suspend fun setBiometric(value: Boolean) =
        context.settingsDataStore.edit { it[K.BIOMETRIC] = value }

    suspend fun setReliability(value: Boolean) =
        context.settingsDataStore.edit { it[K.RELIABILITY] = value }

    suspend fun setOnboardingCompleted(value: Boolean) =
        context.settingsDataStore.edit { it[K.ONBOARDING_DONE] = value }

    suspend fun saveCalibration(profile: CalibrationProfile) {
        context.settingsDataStore.edit {
            val prefix = profile.mode.name.lowercase()
            it[floatPreferencesKey("${prefix}_enter_lux")] = profile.enterLux
            it[floatPreferencesKey("${prefix}_exit_lux")] = profile.exitLux
            it[floatPreferencesKey("${prefix}_motion_noise")] = profile.motionNoise
            it[floatPreferencesKey("${prefix}_rotation_noise")] = profile.rotationNoise
            it[longPreferencesKey("${prefix}_cal_created")] = profile.createdAt
            it[intPreferencesKey("${prefix}_cal_samples")] = profile.sampleCount
            it[booleanPreferencesKey("${prefix}_cal_light")] = profile.hasLightData
            it[booleanPreferencesKey("${prefix}_cal_gyro")] = profile.hasGyroscopeData
        }
    }

    suspend fun setArmed(value: Boolean) =
        context.runtimeDataStore.edit { it[R.ARMED] = value }

    suspend fun setState(value: GuardState) =
        context.runtimeDataStore.edit { it[R.STATE] = value.name }

    suspend fun setSessionEnd(value: Long) =
        context.runtimeDataStore.edit { it[R.SESSION_END] = value }

    suspend fun setConfidence(value: Int) =
        context.runtimeDataStore.edit {
            it[R.CONFIDENCE] = value.coerceIn(0, 100)
        }

    suspend fun setLastAlarm(value: Long) =
        context.runtimeDataStore.edit { it[R.LAST_ALARM] = value }

    private fun readCalibration(
        prefs: Preferences,
        mode: GuardMode
    ): CalibrationProfile? {
        val prefix = mode.name.lowercase()
        val created =
            prefs[longPreferencesKey("${prefix}_cal_created")] ?: return null

        return CalibrationProfile(
            mode = mode,
            createdAt = created,
            enterLux =
                prefs[floatPreferencesKey("${prefix}_enter_lux")] ?: 5f,
            exitLux =
                prefs[floatPreferencesKey("${prefix}_exit_lux")] ?: 12f,
            motionNoise =
                prefs[floatPreferencesKey("${prefix}_motion_noise")] ?: 0.03f,
            rotationNoise =
                prefs[floatPreferencesKey("${prefix}_rotation_noise")] ?: 0.05f,
            sampleCount =
                prefs[intPreferencesKey("${prefix}_cal_samples")] ?: 0,
            hasLightData =
                prefs[booleanPreferencesKey("${prefix}_cal_light")] ?: false,
            hasGyroscopeData =
                prefs[booleanPreferencesKey("${prefix}_cal_gyro")] ?: false
        )
    }

    private inline fun <reified T : Enum<T>> enumValue(
        raw: String?,
        fallback: T
    ): T =
        raw?.let {
            runCatching { enumValueOf<T>(it) }.getOrNull()
        } ?: fallback
}
