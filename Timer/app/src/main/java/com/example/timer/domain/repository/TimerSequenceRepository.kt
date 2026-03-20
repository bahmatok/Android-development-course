package com.example.timer.domain.repository

import com.example.timer.domain.model.TimerSequence
import kotlinx.coroutines.flow.Flow

interface TimerSequenceRepository {
    fun getAllSequences(): Flow<List<TimerSequence>>
    suspend fun getSequenceById(id: String): TimerSequence?
    suspend fun insertSequence(sequence: TimerSequence)
    suspend fun updateSequence(sequence: TimerSequence)
    suspend fun deleteSequence(sequence: TimerSequence)
    suspend fun deleteAll()
}