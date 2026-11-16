package com.example.minder.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minder.domain.model.UserSettings
import com.example.minder.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Ready(val settings: UserSettings, val saving: Boolean = false, val message: String? = null) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

/** Lama: Reads and updates the existing Room-backed user settings. */
class SettingsViewModel(private val repository: UserSettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState
    private var loadedUserId: Int? = null

    fun clearMessage() {
        val current = _uiState.value as? SettingsUiState.Ready ?: return
        if (current.message != null) _uiState.value = current.copy(message = null)
    }

    fun load(userId: Int) {
        if (loadedUserId == userId && _uiState.value is SettingsUiState.Ready) return
        loadedUserId = userId
        _uiState.value = SettingsUiState.Loading
        viewModelScope.launch {
            try {
                val settings = repository.getSettingsByUserId(userId)
                _uiState.value = if (settings != null) SettingsUiState.Ready(settings)
                else SettingsUiState.Error("Saved settings were not found for this user.")
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Unable to load settings.")
            }
        }
    }

    /** dailyLimitMinutes must come from the existing per-app/daily-limit source. */
    fun save(intervalMinutes: Int, difficulty: String, dailyLimitMinutes: Int?, onSaved: (Int) -> Unit = {}) {
        val ready = _uiState.value as? SettingsUiState.Ready ?: return
        if (ready.saving) return
        if (intervalMinutes !in 10..30 || difficulty !in setOf("easy", "medium", "hard")) {
            _uiState.value = ready.copy(message = "Invalid challenge settings.")
            return
        }
        // Do not bypass the onboarding rule: interval must be shorter than daily limit.
        if (dailyLimitMinutes == null || dailyLimitMinutes <= intervalMinutes) {
            _uiState.value = ready.copy(message = "Challenge interval must be shorter than the daily limit.")
            return
        }
        _uiState.value = ready.copy(saving = true, message = null)
        viewModelScope.launch {
            try {
                val updated = ready.settings.copy(
                    challengeInterval = intervalMinutes,
                    challengeDifficulty = difficulty
                )
                repository.updateSettings(updated)
                onSaved(intervalMinutes)
                _uiState.value = SettingsUiState.Ready(updated, message = "Changes saved successfully.")
            } catch (e: Exception) {
                _uiState.value = ready.copy(message = e.message ?: "Unable to save changes.")
            }
        }
    }
}
