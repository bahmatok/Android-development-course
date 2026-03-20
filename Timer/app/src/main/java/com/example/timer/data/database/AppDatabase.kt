package com.example.timer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.timer.data.dao.TimerSequenceDao
import com.example.timer.data.entity.TimerSequenceEntity

@Database(
    entities = [TimerSequenceEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timerSequenceDao(): TimerSequenceDao

    companion object {
        const val DATABASE_NAME = "timer_database"
    }
}