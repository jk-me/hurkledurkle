package com.hurkledurkle.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hurkledurkle.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.TimeZone

data class SettingsUiState(
    val timezone: String = TimeZone.getDefault().id,
    val bedtimeResetHour: Int = 12,
    val bedtimeResetMinute: Int = 0,
    val graphDays: Int = 30
)

class SettingsViewModel(private val prefs: UserPreferences) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.timezone,
        prefs.bedtimeResetHour,
        prefs.bedtimeResetMinute,
        prefs.graphDays
    ) { tz, h, m, days -> SettingsUiState(tz, h, m, days) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTimezone(tz: String) = viewModelScope.launch { prefs.setTimezone(tz) }
    fun setBedtimeReset(hour: Int, minute: Int) =
        viewModelScope.launch { prefs.setBedtimeReset(hour, minute) }
    fun setGraphDays(days: Int) = viewModelScope.launch { prefs.setGraphDays(days) }

    class Factory(private val prefs: UserPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(prefs) as T
    }
}
