package com.fitlogpro.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fitlog_user_settings")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val KEY_WEIGHT_UNIT = stringPreferencesKey("pref_weight_unit")
        val KEY_DISTANCE_UNIT = stringPreferencesKey("pref_distance_unit")
        val KEY_DARK_THEME = booleanPreferencesKey("pref_dark_theme")
        val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("pref_keep_screen_on")
        val KEY_DEFAULT_REST_SECONDS = intPreferencesKey("pref_default_rest_seconds")
        val KEY_REST_VIBRATION = booleanPreferencesKey("pref_rest_vibration")
        val KEY_REST_SOUND = booleanPreferencesKey("pref_rest_sound")
        val KEY_USER_HEIGHT_CM = floatPreferencesKey("pref_user_height_cm")
    }

    val weightUnitFlow: Flow<WeightUnit> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_WEIGHT_UNIT] ?: WeightUnit.KG.name
        WeightUnit.fromString(name)
    }

    val distanceUnitFlow: Flow<DistanceUnit> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_DISTANCE_UNIT] ?: DistanceUnit.KM.name
        DistanceUnit.fromString(name)
    }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME] ?: false // Default to clean modern Light Theme
    }

    val keepScreenOnFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_KEEP_SCREEN_ON] ?: true // Keep screen on active workout default
    }

    val defaultRestSecondsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_REST_SECONDS] ?: 90
    }

    val restVibrationFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REST_VIBRATION] ?: true
    }

    val restSoundFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REST_SOUND] ?: true
    }

    val userHeightCmFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_HEIGHT_CM] ?: 175.0f
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { it[KEY_WEIGHT_UNIT] = unit.name }
    }

    suspend fun setDistanceUnit(unit: DistanceUnit) {
        context.dataStore.edit { it[KEY_DISTANCE_UNIT] = unit.name }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[KEY_KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_REST_SECONDS] = seconds }
    }

    suspend fun setRestVibration(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REST_VIBRATION] = enabled }
    }

    suspend fun setRestSound(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REST_SOUND] = enabled }
    }

    suspend fun setUserHeightCm(height: Float) {
        context.dataStore.edit { it[KEY_USER_HEIGHT_CM] = height }
    }
}
