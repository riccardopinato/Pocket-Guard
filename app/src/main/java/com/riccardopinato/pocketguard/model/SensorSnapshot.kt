package com.riccardopinato.pocketguard.model

data class SensorSnapshot(
    val lux: Float?,
    val proximityNear: Boolean?,
    val motionScore: Float,
    val rotationScore: Float,
    val timestampMs: Long
)
