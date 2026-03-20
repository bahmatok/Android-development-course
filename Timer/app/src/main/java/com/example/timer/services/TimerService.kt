package com.example.timer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.timer.MainActivity
import com.example.timer.R
import com.example.timer.domain.model.TimerPhase
import com.example.timer.domain.model.TimerSequence
import com.example.timer.domain.model.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimerService : LifecycleService() {
    private val binder = TimerBinder()
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    private var timerJob: Job? = null
    private var flatPhases: List<TimerPhase> = emptyList()
    private var currentIndex = 0
    private var currentSequenceId: String = ""
    private val CHANNEL_ID = "timer_channel"
    private val NOTIFICATION_ID = 1

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notification = createNotification("Таймер", "Подготовка к работе...", false)
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    fun setSequenceAndStart(sequence: TimerSequence, sequenceId: String) {
        if (this.currentSequenceId == sequenceId && _timerState.value.isRunning) return
        this.currentSequenceId = sequenceId
        flatPhases = sequence.phases.flatMap { phase ->
            List(phase.repetitions) { phase }
        }
        currentIndex = 0
        _timerState.update {
            it.copy(
                sequenceId = currentSequenceId,
                sequenceName = sequence.name,
                totalPhases = flatPhases.size,
                isFinished = false,
                currentPhaseIndex = 1
            )
        }
        startPhase()
    }

    private fun startPhase() {
        val phase = flatPhases.getOrNull(currentIndex)
        if (phase == null) {
            finishSequence()
            return
        }
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            _timerState.update {
                it.copy(
                    sequenceId = currentSequenceId,
                    isRunning = true,
                    isFinished = false,
                    currentPhase = phase,
                    currentPhaseTotalSeconds = phase.durationSeconds,
                    timeLeftSeconds = phase.durationSeconds,
                    currentPhaseIndex = currentIndex + 1,
                    upcomingPhases = flatPhases.drop(currentIndex + 1)
                )
            }
            for (seconds in phase.durationSeconds downTo 0) {
                _timerState.update { it.copy(timeLeftSeconds = seconds) }
                updateNotification(
                    title = "Фаза: ${phase.type.name}",
                    text = "Осталось: $seconds сек. (${currentIndex + 1}/${flatPhases.size})"
                )
                if (seconds == 0) {
                    playSignal()
                    delay(500)
                } else {
                    delay(1000)
                }
            }
            currentIndex++
            startPhase()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
        updateNotification("Пауза", _timerState.value.sequenceName ?: "")
    }

    fun resumeTimer() {
        if (_timerState.value.isFinished) return
        startPhaseFromCurrent()
    }

    private fun startPhaseFromCurrent() {
        val currentSeconds = _timerState.value.timeLeftSeconds
        val phase = flatPhases.getOrNull(currentIndex) ?: return
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            _timerState.update { it.copy(isRunning = true) }
            for (seconds in currentSeconds downTo 0) {
                _timerState.update { it.copy(timeLeftSeconds = seconds) }
                updateNotification("Фаза: ${phase.type.name}", "Осталось: $seconds сек")
                if (seconds == 0) playSignal()
                if (seconds > 0) delay(1000)
            }
            currentIndex++
            startPhase()
        }
    }

    fun nextPhase() {
        if (currentIndex < flatPhases.size - 1) {
            currentIndex++
            startPhase()
        } else {
            finishSequence()
        }
    }

    fun previousPhase() {
        if (currentIndex > 0) {
            currentIndex--
            startPhase()
        }
    }

    private fun finishSequence() {
        timerJob?.cancel()
        _timerState.update {
            it.copy(
                sequenceId = currentSequenceId,
                isRunning = false,
                isFinished = true,
                currentPhase = null,
                timeLeftSeconds = 0,
                upcomingPhases = emptyList()
            )
        }
        updateNotification("Тренировка завершена!", "Отличная работа")
    }

    fun stopTimer() {
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun playSignal() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.timer_beep_sound)
            mediaPlayer?.setOnCompletionListener { it.release() }
            mediaPlayer?.start()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Работа таймера",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(title: String, text: String) {
        val isFinished = title == "Тренировка завершена!"
        val notification = createNotification(title, text, isFinished)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(title: String, text: String, isFinished: Boolean): Notification {
        val uriString = if (currentSequenceId.isNotEmpty()) "timerapp://timer/$currentSequenceId" else "timerapp://main"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString), this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(!isFinished)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
}