package com.example.tracktogether.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.data.AttendanceResponse
import com.example.tracktogether.data.Employee
import com.example.tracktogether.repository.AttendanceRepository
import com.example.tracktogether.repository.AuthenticationRepository
import com.example.tracktogether.repository.ImageRepository
import com.example.tracktogether.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EmployeeMainViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthenticationRepository,
    private val imageRepository: ImageRepository,
    private val attendanceRepository: AttendanceRepository,
) : ViewModel() {
    // Image Repo Access
    var IEmployee: IEmployee? = null

    // User pref repo access
    val userPrefFlow = userPreferencesRepository.userPreferencesFlow.asLiveData()





    fun getEmpRemoteCheckinStatus(uid: String) = viewModelScope.launch(Dispatchers.IO) {
        IEmployee?.let { imageRepository.getEmpRemoteCheckinStatus(uid, it) }
    }


    fun updateRemoteState(state: String) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.updateRemoteApproved(state)
    }



    fun currentUserID(): String {
        return authRepository.currentUserID()
    }


    /***
     * Channels for handling one time events
     */
    sealed class EmployeeMainViewEvent {
        data class GetAttendanceEvent(val response: AttendanceResponse) : EmployeeMainViewEvent()
    }

    private val eventChannel = Channel<EmployeeMainViewEvent>()

    val eventFlow = eventChannel.receiveAsFlow()

    /**
     * Check attendance of employee today
     */
    fun checkAttendanceToday() = viewModelScope.launch(Dispatchers.IO) {
        eventChannel.send(
            EmployeeMainViewEvent.GetAttendanceEvent(
                (attendanceRepository.getAttendanceDocToday(authRepository.currentUser()?.uid!!))
            )
        )
    }

    /***
     * Checkout by updating outTime in firestore
     * @param documentId from getAttendanceDocument
     */
    fun checkOut(documentId: String) = viewModelScope.launch(Dispatchers.IO) {
        attendanceRepository.updateOutTime(documentId)
    }


}

class EmployeeMainViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthenticationRepository,
    private val imageRepository: ImageRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeMainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployeeMainViewModel(
                userPreferencesRepository,
                authRepository,
                imageRepository, attendanceRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

