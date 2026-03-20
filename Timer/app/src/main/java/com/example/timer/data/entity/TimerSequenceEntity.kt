package com.example.timer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.timer.data.converter.PhaseTypeConverter
import com.example.timer.data.converter.TimerPhaseListConverter

@Entity(tableName = "timer_sequences")
@TypeConverters(TimerPhaseListConverter::class, PhaseTypeConverter::class)
data class TimerSequenceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: Int,
    val phases: List<TimerPhaseEntity>
)

data class TimerPhaseEntity(
    val id: String,
    val type: String,
    val durationSeconds: Int,
    val repetitions: Int
)