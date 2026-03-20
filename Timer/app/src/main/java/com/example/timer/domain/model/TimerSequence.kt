package com.example.timer.domain.model

import java.util.UUID

data class TimerSequence(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Int,
    val phases: List<TimerPhase>
)

data class TimerPhase(
    val id: String = UUID.randomUUID().toString(),
    val type: PhaseType,
    val durationSeconds: Int,
    val repetitions: Int = 1
)

enum class PhaseType {
    WARMUP, WORK, REST, COOLDOWN
}