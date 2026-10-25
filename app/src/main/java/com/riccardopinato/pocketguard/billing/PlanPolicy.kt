package com.riccardopinato.pocketguard.billing

import com.riccardopinato.pocketguard.model.GuardMode
import com.riccardopinato.pocketguard.model.GuardSettings
import com.riccardopinato.pocketguard.model.JourneyDuration
import com.riccardopinato.pocketguard.model.PocketSensitivity

object PlanPolicy {
    fun effectiveSettings(
        source: GuardSettings,
        isPro: Boolean
    ): GuardSettings {
        if (isPro) return source

        return source.copy(
            mode = GuardMode.POCKET,
            journeyDuration = JourneyDuration.MINUTES_30,
            sensitivity =
                if (source.sensitivity == PocketSensitivity.HIGH) {
                    PocketSensitivity.NORMAL
                } else {
                    source.sensitivity
                },
            flashEnabled = false,
            biometricEnabled = false,
            enhancedReliability = false
        )
    }
}
