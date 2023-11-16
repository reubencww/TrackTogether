package com.example.tracktogether.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main View Model, mainly used to get user role to display appropriate fragment
 * Author: Reuben
 * Updated: 9 March 2022
 */
class MainViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // User pref repo access
    val userRole = userPreferencesRepository.userRoleFlow.asLiveData()

    fun clearPref() = viewModelScope.launch(Dispatchers.IO){
        userPreferencesRepository.clearPref()
    }
}

class MainViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                userPreferencesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}