package com.example.tracktogether.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.repository.AttendanceRepository
import com.example.tracktogether.repository.AuthenticationRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


/**
 * View Model for NFC
 * Author: Ong Ze Quan
 * Updated: 5 Mar 2022
 */
class NFCViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val authRepository: AuthenticationRepository
) : ViewModel() {

    /**
     * Uploads attendance to Firebase
     * @param location Location of office
     */
    fun uploadAttendanceToFirebase(location: String) = viewModelScope.launch(Dispatchers.IO) {
        authRepository.currentUser()?.let {
            val attendance = Attendance(
                employeeUID = it.uid,
                inTime = Timestamp.now(),
                remote = false,
                location = location
            )
            attendanceRepository.uploadAttendanceRecordToDB(
                attendance
            )
        }
    }

    /***
     * Channels for handling one time events
     */
    sealed class NFCEvent {
        data class ErrorEvent(val message: String) : NFCEvent()
        data class TagEvent(val tagId: String) : NFCEvent()
        data class LocationEvent(val locationTag: String) : NFCEvent()
    }

    private val eventChannel = Channel<NFCEvent>()

    val eventFlow = eventChannel.receiveAsFlow()

    /**
     * Check tag that was scanned by device
     */
    fun checkTag(tagId: ByteArray?) = viewModelScope.launch(Dispatchers.IO) {
        eventChannel.send(NFCEvent.TagEvent(tagId?.toHex()!!))
    }

    /**
     * Get NFC card ID for location
     * @param location Location of office
     */
    fun getLocationNFC(location: String) = viewModelScope.launch(Dispatchers.IO) {
        eventChannel.send(NFCEvent.LocationEvent(attendanceRepository.getLocationNFC(location).nfcId!!))
    }

    /***
     * Helper function to convert ByteArray to Hex
     */
    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}


class NFCViewModelFactory(
    private val attendanceRepository: AttendanceRepository,
    private val authRepository: AuthenticationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NFCViewModel::class.java)) {
            return NFCViewModel(attendanceRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}