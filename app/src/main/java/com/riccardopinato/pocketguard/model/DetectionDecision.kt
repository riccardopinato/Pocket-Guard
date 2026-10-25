package com.riccardopinato.pocketguard.model

data class DetectionDecision(
    val state: GuardState,
    val changed: Boolean,
    val confidenceScore: Int,
    val reason: String
)
