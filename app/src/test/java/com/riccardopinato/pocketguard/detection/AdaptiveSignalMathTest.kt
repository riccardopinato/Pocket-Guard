package com.riccardopinato.pocketguard.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveSignalMathTest {
    @Test
    fun dartReferenceTransitionVectorsMatch() {
        var state = AdaptiveSignalState(baseline = 10.0)
        state = AdaptiveSignalMath.step(state, 11.0)
        assertFalse(state.triggered)
        assertEquals(10.2, state.baseline, 0.0000001)
        state = AdaptiveSignalMath.step(state, 25.0)
        assertFalse(state.triggered)
        state = AdaptiveSignalMath.step(state, 24.0)
        assertTrue(state.triggered)
        state = AdaptiveSignalMath.step(state, 20.0)
        assertTrue(state.triggered)
        state = AdaptiveSignalMath.step(state, 10.0)
        assertTrue(state.triggered)
        state = AdaptiveSignalMath.step(state, 10.0)
        assertFalse(state.triggered)
    }

    @Test
    fun invalidSignalInputFailsClosed() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach {
            try {
                AdaptiveSignalMath.step(AdaptiveSignalState(0.0), it)
                throw AssertionError("Nonfinite signal accepted")
            } catch (expected: IllegalArgumentException) {
                assertEquals("INVALID_SIGNAL_CONFIG", expected.message)
            }
        }
        try {
            AdaptiveSignalMath.step(
                AdaptiveSignalState(0.0), 4.0,
                enterDelta = 2.0, exitDelta = 3.0
            )
            throw AssertionError("Invalid hysteresis accepted")
        } catch (expected: IllegalArgumentException) {
            assertEquals("INVALID_SIGNAL_CONFIG", expected.message)
        }
    }

    @Test
    fun consecutiveNoisySamplesDoNotTriggerWithoutSustainedIncrease() {
        var state = AdaptiveSignalState(baseline = 0.03)
        state = AdaptiveSignalMath.step(
            state, 0.50, enterDelta = 0.12, exitDelta = 0.04
        )
        assertFalse(state.triggered)
        state = AdaptiveSignalMath.step(
            state, 0.02, enterDelta = 0.12, exitDelta = 0.04
        )
        assertEquals(0, state.streak)
        assertFalse(state.triggered)
    }
}
