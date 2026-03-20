package com.example.timer.data.repository

import com.example.timer.data.dao.TimerSequenceDao
import com.example.timer.data.mapper.TimerSequenceMapper
import com.example.timer.domain.model.TimerSequence
import com.example.timer.domain.repository.TimerSequenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TimerSequenceRepositoryImpl @Inject constructor(
    private val dao: TimerSequenceDao
) : TimerSequenceRepository {

    override fun getAllSequences(): Flow<List<TimerSequence>> {
        return dao.getAllSequences().map { TimerSequenceMapper.toDomainList(it) }
    }

    override suspend fun getSequenceById(id: String): TimerSequence? {
        return dao.getSequenceById(id)?.let { TimerSequenceMapper.toDomain(it) }
    }

    override suspend fun insertSequence(sequence: TimerSequence) {
        val entity = TimerSequenceMapper.toEntity(sequence)
        dao.insertSequence(entity)
    }

    override suspend fun updateSequence(sequence: TimerSequence) {
        val entity = TimerSequenceMapper.toEntity(sequence)
        dao.updateSequence(entity)
    }

    override suspend fun deleteSequence(sequence: TimerSequence) {
        val entity = TimerSequenceMapper.toEntity(sequence)
        dao.deleteSequence(entity)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}