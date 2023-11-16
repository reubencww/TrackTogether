package com.example.tracktogether.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.example.tracktogether.data.Employee
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.repository.AuthenticationRepository
import com.example.tracktogether.repository.ImageRepository
import com.example.tracktogether.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthenticationRepository,
    private val imageRepository: ImageRepository,


    ) : ViewModel() {

    var checkinI: ISuccessFlag? = null
    var IEmployee: IEmployee? = null
    val biometricPref = userPreferencesRepository.biometricFlow.asLiveData()
    val userPrefFlow = userPreferencesRepository.userPreferencesFlow.asLiveData()

    fun saveBiometricPref(switchIsChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.saveBiometricPref(switchIsChecked)
    }

    fun uploadImageToFirebase(fileUri: Uri, employee: Employee) =
        viewModelScope.launch(Dispatchers.IO) {
            checkinI?.let { imageRepository.uploadImageToFirebase(fileUri, employee, it) }
        }

    fun currentUserEmail(): String {
        return authRepository.currentUserEmail()
    }

    fun currentUserID(): String {
        return authRepository.currentUserID()
    }

    fun updateRemoteState(state: String) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.updateRemoteApproved(state)
    }
}

class SettingViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthenticationRepository,
    private val imageRepository: ImageRepository,


    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(userPreferencesRepository, authRepository, imageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}