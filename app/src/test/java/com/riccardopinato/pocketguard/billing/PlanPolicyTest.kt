package com.riccardopinato.pocketguard.billing

import com.riccardopinato.pocketguard.model.GuardMode
import com.riccardopinato.pocketguard.model.GuardSettings
import com.riccardopinato.pocketguard.model.JourneyDuration
import com.riccardopinato.pocketguard.model.PocketSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlanPolicyTest {
    @Test
    fun freePlanKeepsCoreProtectionButRemovesPremiumControls() {
        val source =
            GuardSettings(
                mode = GuardMode.BAG,
                journeyDuration = JourneyDuration.UNTIL_STOPPED,
                sensitivity = PocketSensitivity.HIGH,
                soundEnabled = true,
                vibrationEnabled = true,
                flashEnabled = true,
                biometricEnabled = true,
                enhancedReliability = true
            )

        val effective =
            PlanPolicy.effectiveSettings(
                source = source,
                isPro = false
            )

        assertEquals(GuardMode.POCKET, effective.mode)
        assertEquals(
            JourneyDuration.MINUTES_30,
            effective.journeyDuration
        )
        assertEquals(
            PocketSensitivity.NORMAL,
            effective.sensitivity
        )
        assertEquals(true, effective.soundEnabled)
        assertEquals(true, effective.vibrationEnabled)
        assertFalse(effective.flashEnabled)
        assertFalse(effective.biometricEnabled)
        assertFalse(effective.enhancedReliability)
    }

    @Test
    fun proPlanLeavesSettingsUntouched() {
        val source =
            GuardSettings(
                mode = GuardMode.BAG,
                journeyDuration = JourneyDuration.UNTIL_STOPPED,
                sensitivity = PocketSensitivity.HIGH,
                flashEnabled = true,
                biometricEnabled = true,
                enhancedReliability = true
            )

        assertEquals(
            source,
            PlanPolicy.effectiveSettings(source, isPro = true)
        )
    }
}
