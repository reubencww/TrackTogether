package com.example.tracktogether.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class OfficeParams(
    val postalCode: String,
    val nfcTag: String,
    val officeName: String
)

class OfficeCRUDRepository(context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = OFFICECRUDREPO
    )

    private val dataStore = context.dataStore

    object PreferencesKeys {
        val OFFICE_NAME = stringPreferencesKey("office_name")
    }

    val officeNameFlow: Flow<String> = dataStore.data
        .catch { e ->
            e.printStackTrace()
            emit(emptyPreferences())
        }
        .map { pref ->
            pref[OfficeCRUDRepository.PreferencesKeys.OFFICE_NAME] ?: ""
        }

    suspend fun saveOfficeName(officeName: String) {
        dataStore.edit { preferences ->
            preferences[OfficeCRUDRepository.PreferencesKeys.OFFICE_NAME] = officeName
        }
    }

    companion object {
        // Constant for naming our DataStore - you can change this if you want
        private const val OFFICECRUDREPO = "office_crud"

        // The usual for debugging
        private val TAG: String = "OfficeCRUDRepo"

        // Boilerplate-y code for singleton: the private reference to this self
        @Volatile
        private var INSTANCE: OfficeCRUDRepository? = null

        fun getInstance(context: Context): OfficeCRUDRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = OfficeCRUDRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }

}