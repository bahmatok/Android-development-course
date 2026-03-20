package com.example.timer.data.converter

import androidx.room.TypeConverter
import com.example.timer.domain.model.PhaseType

class PhaseTypeConverter {
    @TypeConverter
    fun fromPhaseType(type: PhaseType): String = type.name

    @TypeConverter
    fun toPhaseType(name: String): PhaseType = PhaseType.valueOf(name)
}