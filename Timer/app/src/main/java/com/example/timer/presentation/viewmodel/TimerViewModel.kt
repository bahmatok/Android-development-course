package com.example.timer.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.domain.model.TimerState
import com.example.timer.domain.repository.TimerSequenceRepository
import com.example.timer.services.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TimerSequenceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sequenceId: String? = savedStateHandle.get<String>("sequenceId")
    private var timerService: TimerService? = null
    private var isServiceBound = false
    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isServiceBound = true
            viewModelScope.launch {
                timerService?.timerState?.collect { state ->
                    _uiState.value = state
                }
            }
            val currentServiceState = timerService?.timerState?.value
            val needsStart = sequenceId != null &&
                    (currentServiceState?.sequenceId.isNullOrEmpty() || currentServiceState?.sequenceId != sequenceId)
            if (needsStart) {
                startNewSequence(sequenceId!!)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isServiceBound = false
        }
    }
    init {
        val intent = Intent(context, TimerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    private fun startNewSequence(id: String) {
        viewModelScope.launch {
            val sequence = repository.getSequenceById(id)
            sequence?.let {
                timerService?.setSequenceAndStart(it, id)
            }
        }
    }
    fun togglePlayPause() = timerService?.let {
        if (_uiState.value.isRunning) it.pauseTimer() else it.resumeTimer()
    }
    fun nextPhase() = timerService?.nextPhase()
    fun previousPhase() = timerService?.previousPhase()
    fun cancelTimer() = timerService?.stopTimer()
    override fun onCleared() {
        super.onCleared()
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}