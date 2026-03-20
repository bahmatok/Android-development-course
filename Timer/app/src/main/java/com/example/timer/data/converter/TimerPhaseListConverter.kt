package com.example.timer.data.converter

import androidx.room.TypeConverter
import com.example.timer.data.entity.TimerPhaseEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TimerPhaseListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromList(phases: List<TimerPhaseEntity>): String {
        return gson.toJson(phases)
    }

    @TypeConverter
    fun toList(json: String): List<TimerPhaseEntity> {
        val type = object : TypeToken<List<TimerPhaseEntity>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}