package com.example.timer.data.mapper

import com.example.timer.data.entity.TimerPhaseEntity
import com.example.timer.data.entity.TimerSequenceEntity
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhase
import com.example.timer.domain.model.TimerSequence

object TimerSequenceMapper {

    // Entity → Domain (из базы в приложение)
    fun toDomain(entity: TimerSequenceEntity): TimerSequence {
        return TimerSequence(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            phases = entity.phases.map { toDomain(it) }
        )
    }

    // Domain → Entity (из приложения в базу)
    fun toEntity(sequence: TimerSequence): TimerSequenceEntity {
        return TimerSequenceEntity(
            id = sequence.id,
            name = sequence.name,
            color = sequence.color,
            phases = sequence.phases.map { toEntity(it) }
        )
    }

    private fun toDomain(phase: TimerPhaseEntity): TimerPhase {
        return TimerPhase(
            id = phase.id,
            type = PhaseType.valueOf(phase.type),
            durationSeconds = phase.durationSeconds,
            repetitions = phase.repetitions
        )
    }

    private fun toEntity(phase: TimerPhase): TimerPhaseEntity {
        return TimerPhaseEntity(
            id = phase.id,
            type = phase.type.name,
            durationSeconds = phase.durationSeconds,
            repetitions = phase.repetitions
        )
    }

    // Для списка
    fun toDomainList(entities: List<TimerSequenceEntity>): List<TimerSequence> =
        entities.map { toDomain(it) }
}