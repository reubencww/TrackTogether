package com.example.tracktogether.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Data class to store any user preference that the application wants to store
 * User role store user role to manage fragment display
 * Enable biometric stores user preference to show biometric
 * Author: Reuben, Ze Quan
 * Updated: 12 March 2022
 */
data class UserPreferences(
    val userRole: String,
    val enableBiometric: Boolean,
    val serializedImageSaved: Boolean,
    val remoteApproved: String,
    val firstLogin: Boolean,
)

/**
 * User Preference repository to act as a preference data store
 * Provides access to UserPreference dataclass for ViewModels to access data
 * Author: Reuben , Ze Quan
 * Updated: 12 March 2022
 */
class UserPreferencesRepository(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )
    private val dataStore = context.dataStore

    object PreferencesKeys {
        val USER_ROLE = stringPreferencesKey("user_role")
        val FIRST_LOGIN = booleanPreferencesKey("is_first_time")
        val IS_PROFILE_EMPTY = booleanPreferencesKey("is_profile_empty")
        val BIOMETRIC_PREF = booleanPreferencesKey("enable_biometric")
        val SERIALIZED_DATA_STORED = booleanPreferencesKey("is_serialized_data_stored")
        val REMOTE_STATE = stringPreferencesKey("is_remote_approved")
        val DEVICE_TOKEN = stringPreferencesKey("device_token")
    }

    /**
     * Flow for user role live data
     */
    val userRoleFlow: Flow<String> = dataStore.data
        .catch { e ->
            e.printStackTrace()
            emit(emptyPreferences())
        }
        .map { pref ->
            pref[PreferencesKeys.USER_ROLE] ?: "Employee"
        }

    /**
     * Flow for biometric preference live data
     */
    val biometricFlow: Flow<Boolean> = dataStore.data
        .catch { e ->
            e.printStackTrace()
            emit(emptyPreferences())
        }
        .map { pref ->
            pref[PreferencesKeys.BIOMETRIC_PREF] ?: false
        }

    //Other version to get biometricFlow using uid
    fun getBiometricDataFlow(key: String): Flow<Boolean> {
        val convertedKey = booleanPreferencesKey(key)
        return dataStore.data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[convertedKey] ?: false
        }
    }



    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val userRole = preferences[PreferencesKeys.USER_ROLE] ?: "Employee"
        val biometricPref = preferences[PreferencesKeys.BIOMETRIC_PREF] ?: false
        val serializedPref = preferences[PreferencesKeys.SERIALIZED_DATA_STORED] ?: false
        val remotePref = preferences[PreferencesKeys.REMOTE_STATE] ?: "Waiting"
        val firstLogin = preferences[PreferencesKeys.FIRST_LOGIN] ?: true
        return UserPreferences(userRole, biometricPref, serializedPref, remotePref, firstLogin)
    }

    /**
     * Helper function to store user role
     */
    suspend fun saveProfileStatus(userRole: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ROLE] = userRole
        }
    }

    /**
     * Helper function to store profile status
     */
    suspend fun saveProfileStatus(isProfileEmpty: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PROFILE_EMPTY] = isProfileEmpty
        }
    }

    /**
     * helper function to store biometric preference
     */
    suspend fun saveBiometricPref(biometricPref: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_PREF] = biometricPref
        }
    }

    /**
     * helper function to store device token id preference
     */
    suspend fun storeDeviceToken(tokenID: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_TOKEN] = tokenID
        }
    }

    suspend fun saveLoginPref(firstLogin: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LOGIN] = firstLogin
        }
    }
    /***
     * Helper function to
     */

    suspend fun updateSerialDataState(boolean: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERIALIZED_DATA_STORED] = boolean
        }
    }

    suspend fun updateRemoteApproved(state: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMOTE_STATE] = state
        }
    }
    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())

    suspend fun clearPref(){
        dataStore.edit {
            it.clear()
        }
    }


    companion object {
        // Constant for naming our DataStore - you can change this if you want
        private const val USER_PREFERENCES_NAME = "user_preferences"

        // The usual for debugging
        private val TAG: String = "UserPreferencesRepository"

        // Boilerplate-y code for singleton: the private reference to this self
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = UserPreferencesRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}