package com.example.timer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.domain.repository.SettingsRepository
import com.example.timer.domain.repository.TimerSequenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val timerRepository: TimerSequenceRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
    val languageCode: StateFlow<String> = settingsRepository.languageCode
    val fontSizeScale: StateFlow<Float> = settingsRepository.fontSizeScale

    fun toggleTheme(isDark: Boolean) {
        settingsRepository.setDarkMode(isDark)
    }

    fun changeLanguage(code: String) {
        settingsRepository.setLanguage(code)
    }

    fun setFontSize(scale: Float) = settingsRepository.setFontSizeScale(scale)

    fun clearAllData() {
        viewModelScope.launch {
            timerRepository.deleteAll()
            settingsRepository.clearSettings() // если надо
        }
    }
}