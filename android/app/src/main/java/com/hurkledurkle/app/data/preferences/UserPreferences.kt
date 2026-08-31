package com.hurkledurkle.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.TimeZone

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val TIMEZONE = stringPreferencesKey("timezone")
        val BEDTIME_RESET_HOUR = intPreferencesKey("bedtime_reset_hour")
        val BEDTIME_RESET_MINUTE = intPreferencesKey("bedtime_reset_minute")
        val GRAPH_DAYS = intPreferencesKey("graph_days")
    }

    val timezone: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[TIMEZONE] ?: TimeZone.getDefault().id
    }

    val bedtimeResetHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BEDTIME_RESET_HOUR] ?: 12
    }

    val bedtimeResetMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BEDTIME_RESET_MINUTE] ?: 0
    }

    val graphDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GRAPH_DAYS] ?: 30
    }

    suspend fun setTimezone(tz: String) {
        context.dataStore.edit { it[TIMEZONE] = tz }
    }

    suspend fun setBedtimeReset(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[BEDTIME_RESET_HOUR] = hour
            prefs[BEDTIME_RESET_MINUTE] = minute
        }
    }

    suspend fun setGraphDays(days: Int) {
        context.dataStore.edit { it[GRAPH_DAYS] = days }
    }
}
