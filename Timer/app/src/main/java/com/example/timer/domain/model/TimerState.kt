package com.example.timer.domain.model

data class TimerState(
    val sequenceId: String = "",
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val sequenceName: String = "",
    val currentPhase: TimerPhase? = null,
    val timeLeftSeconds: Int = 0,
    val currentPhaseTotalSeconds: Int = 1,
    val currentPhaseIndex: Int = 0,
    val totalPhases: Int = 0,
    val upcomingPhases: List<TimerPhase> = emptyList()
)