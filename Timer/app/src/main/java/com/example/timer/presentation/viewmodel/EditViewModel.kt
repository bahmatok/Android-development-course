package com.example.timer.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.R
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhase
import com.example.timer.domain.model.TimerSequence
import com.example.timer.domain.repository.TimerSequenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: TimerSequenceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sequenceId: String? = savedStateHandle.get<String>("sequenceId")
    private val MAX_SECONDS = 86400 // 24 часа

    private val _sequenceState = MutableStateFlow<TimerSequence?>(null)
    val sequenceState: StateFlow<TimerSequence?> = _sequenceState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<Int>()
    val messageEvent: SharedFlow<Int> = _messageEvent.asSharedFlow()

    init {
        if (sequenceId != null && sequenceId != "null") {
            viewModelScope.launch {
                _sequenceState.value = repository.getSequenceById(sequenceId)
            }
        } else {
            _sequenceState.value = TimerSequence(
                id = "",
                name = "",
                color = 0xFFBB86FC.toInt(),
                phases = emptyList()
            )
        }
    }

    fun updateName(newName: String) {
        _sequenceState.value = _sequenceState.value?.copy(name = newName)
    }

    fun updateColor(newColor: Int) {
        _sequenceState.value = _sequenceState.value?.copy(color = newColor)
    }

    fun addPhase(type: PhaseType) {
        val currentSeq = _sequenceState.value ?: return
        val newPhase = TimerPhase(type = type, durationSeconds = 30)
        _sequenceState.value = currentSeq.copy(phases = currentSeq.phases + newPhase)
    }

    fun updatePhaseDuration(phaseId: String, deltaSeconds: Int) {
        val currentSeq = _sequenceState.value ?: return
        var exceeded = false

        val updatedPhases = currentSeq.phases.map { phase ->
            if (phase.id == phaseId) {
                val newDuration = (phase.durationSeconds + deltaSeconds)
                when {
                    newDuration > MAX_SECONDS -> {
                        exceeded = true
                        phase.copy(durationSeconds = MAX_SECONDS)
                    }
                    newDuration < 5 -> phase.copy(durationSeconds = 5)
                    else -> phase.copy(durationSeconds = newDuration)
                }
            } else phase
        }

        if (exceeded) sendLimitWarning()
        _sequenceState.value = currentSeq.copy(phases = updatedPhases)
    }

    fun setPhaseDuration(phaseId: String, secondsStr: String) {
        val currentSeq = _sequenceState.value ?: return
        val inputSeconds = secondsStr.toIntOrNull() ?: 0
        var exceeded = false

        val finalSeconds = if (inputSeconds > MAX_SECONDS) {
            exceeded = true
            MAX_SECONDS
        } else inputSeconds

        val updatedPhases = currentSeq.phases.map { phase ->
            if (phase.id == phaseId) {
                phase.copy(durationSeconds = finalSeconds)
            } else phase
        }

        if (exceeded) sendLimitWarning()
        _sequenceState.value = currentSeq.copy(phases = updatedPhases)
    }

    private fun sendLimitWarning() {
        viewModelScope.launch {
            _messageEvent.emit(R.string.limit_warning)
        }
    }

    fun removePhase(phaseId: String) {
        val currentSeq = _sequenceState.value ?: return
        _sequenceState.value = currentSeq.copy(
            phases = currentSeq.phases.filter { it.id != phaseId }
        )
    }

    fun saveSequence(onSaved: () -> Unit) {
        val seqToSave = _sequenceState.value ?: return

            viewModelScope.launch {
            if (seqToSave.name.trim().isEmpty()) {
                _messageEvent.emit(R.string.error_empty_name)
                return@launch
            }
            if (seqToSave.phases.isEmpty()) {
                _messageEvent.emit(R.string.error_no_phases)
                return@launch
            }

            if (seqToSave.id.isNotEmpty()) {
                repository.updateSequence(seqToSave)
            } else {
                repository.insertSequence(
                    seqToSave.copy(id = java.util.UUID.randomUUID().toString())
                )
            }
            onSaved()
        }
    }
}