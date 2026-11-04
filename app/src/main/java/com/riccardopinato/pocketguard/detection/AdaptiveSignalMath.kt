package com.riccardopinato.pocketguard.detection

/**
 * Pure Kotlin port of CAP-0158 App-Factory-Core@ae76e4267b67fe0e913d650d8e89a010dd235a5c.
 * Mathematical contract only. Not proof of sensor quality, power or OEM runtime.
 * No Android dependencies. Kept in diagnostic shadow mode until physical QA.
 */
data class AdaptiveSignalState(
    val baseline: Double,
    val triggered: Boolean = false,
    val streak: Int = 0,
)

object AdaptiveSignalMath {
    fun step(
        state: AdaptiveSignalState,
        sample: Double,
        alpha: Double = 0.2,
        enterDelta: Double = 5.0,
        exitDelta: Double = 2.0,
        consecutive: Int = 2,
    ): AdaptiveSignalState {
        require(
            sample.isFinite() &&
                state.baseline.isFinite() &&
                alpha.isFinite() && alpha > 0.0 && alpha <= 1.0 &&
                enterDelta.isFinite() && exitDelta.isFinite() &&
                enterDelta > 0.0 && exitDelta >= 0.0 &&
                exitDelta < enterDelta && consecutive in 1..20 &&
                state.streak >= 0
        ) { "INVALID_SIGNAL_CONFIG" }

        if (!state.triggered) {
            if (sample >= state.baseline + enterDelta) {
                val count = state.streak + 1
                return AdaptiveSignalState(
                    baseline = state.baseline,
                    triggered = count >= consecutive,
                    streak = if (count >= consecutive) 0 else count,
                )
            }
            return AdaptiveSignalState(
                baseline = state.baseline * (1 - alpha) + sample * alpha
            )
        }
        if (sample <= state.baseline + exitDelta) {
            val count = state.streak + 1
            if (count >= consecutive) {
                return AdaptiveSignalState(
                    baseline = state.baseline * (1 - alpha) + sample * alpha
                )
            }
            return AdaptiveSignalState(
                baseline = state.baseline,
                triggered = true,
                streak = count,
            )
        }
        return AdaptiveSignalState(
            baseline = state.baseline,
            triggered = true,
        )
    }
}
