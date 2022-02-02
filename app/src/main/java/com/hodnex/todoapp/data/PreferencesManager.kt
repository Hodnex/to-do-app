package com.hodnex.todoapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreference(val sortOrder: SortOrder, val hideCompleted: Boolean)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    val preferenceFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error preferences ", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            val sortOrder = SortOrder.valueOf(
                preference[PreferenceKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preference[PreferenceKeys.HIDE_COMPLETED] ?: false
            FilterPreference(sortOrder, hideCompleted)
        }

    suspend fun updateSortOrderPreference(sortOrder: SortOrder, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompletedPreference(hideCompleted: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    private object PreferenceKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")
    }
}