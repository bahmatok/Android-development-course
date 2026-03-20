package com.example.timer.data.dao

import androidx.room.*
import com.example.timer.data.entity.TimerSequenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSequenceDao {

    @Query("SELECT * FROM timer_sequences")
    fun getAllSequences(): Flow<List<TimerSequenceEntity>>

    @Query("SELECT * FROM timer_sequences WHERE id = :id")
    suspend fun getSequenceById(id: String): TimerSequenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequence(sequence: TimerSequenceEntity)

    @Update
    suspend fun updateSequence(sequence: TimerSequenceEntity)

    @Delete
    suspend fun deleteSequence(sequence: TimerSequenceEntity)

    @Query("DELETE FROM timer_sequences")
    suspend fun deleteAll()
}