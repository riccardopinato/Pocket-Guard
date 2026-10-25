package com.riccardopinato.pocketguard.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.riccardopinato.pocketguard.model.GuardSettings
import kotlinx.coroutines.*

class AlarmController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var flashJob: Job? = null
    private var testStopJob: Job? = null
    private val flash = FlashController(context)

    fun start(settings: GuardSettings) {
        stop()

        if (settings.soundEnabled) startSiren()
        if (settings.vibrationEnabled) startVibration()

        if (settings.flashEnabled && flash.hasPermission()) {
            flashJob = scope.launch(Dispatchers.Default) {
                var on = false
                while (isActive) {
                    on = !on
                    flash.setEnabled(on)
                    delay(180L)
                }
            }
        }
    }

    fun stop() {
        testStopJob?.cancel()
        testStopJob = null

        flashJob?.cancel()
        flashJob = null
        flash.forceOff()

        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null

        getVibrator()?.cancel()
    }

    fun test(
        settings: GuardSettings,
        durationMs: Long = 3_000L,
        onFinished: () -> Unit = {}
    ) {
        start(settings)

        testStopJob = scope.launch {
            delay(durationMs)
            stop()
            onFinished()
        }
    }

    private fun startSiren() {
        runCatching {
            val file = SirenToneFactory.getOrCreate(context)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(
                            AudioAttributes.CONTENT_TYPE_SONIFICATION
                        )
                        .build()
                )
                setDataSource(file.absolutePath)
                isLooping = true
                prepare()
                start()
            }
        }
    }

    private fun startVibration() {
        getVibrator()?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(
                    0L,
                    500L,
                    150L,
                    500L,
                    150L,
                    900L
                ),
                0
            )
        )
    }

    @Suppress("DEPRECATION")
    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)
                ?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
