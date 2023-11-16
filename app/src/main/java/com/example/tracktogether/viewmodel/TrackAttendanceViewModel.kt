package com.example.tracktogether.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.Interfaces.IAttendanceList
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.repository.AttendanceRepository
import com.example.tracktogether.repository.AuthenticationRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Track attendance view model for processing business logic of track attendance activity
 * Author: Cheng Hao
 * Updated: 13 March 2022
 */
class TrackAttendanceViewModel(
    private val attendanceRepo: AttendanceRepository,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    var IAttendanceList: IAttendanceList? = null
    val fromError = MutableLiveData<String>()
    val toError = MutableLiveData<String>()
    val validateResult = MutableLiveData<Boolean>()
    val fromEpoch = MutableLiveData<Timestamp>()
    val toEpoch = MutableLiveData<Timestamp>()
    val remoteAttendanceDetail = MutableLiveData<List<Attendance>>()
    val officeAttendanceDetail = MutableLiveData<List<Attendance>>()

    /**
     * Validates field of date inputs
     * @param from starting date range
     * @param to ending date range
     */
    fun validateTextfield(from: String, to: String) {
        if (!validateDate(from)) {
            fromError.postValue("Field is not valid")
        } else if (!validateDate(to))
            toError.postValue("Field is not valid")
        else
            validateResult.postValue(true)
    }

    /**
     * converts date in string to timestamp
     * @param from starting date range
     * @param to ending date range
     */
    fun convertStringToTimestamp(from: String, to: String) {
        val fromDate = "$from 00:00:00"
        val toDate = "$to 00:00:00"
        var dt = LocalDateTime.parse(fromDate, DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"))
        fromEpoch.postValue(Timestamp(dt.atZone(ZoneId.of("Asia/Singapore")).toEpochSecond(), 0))
        dt = LocalDateTime.parse(toDate, DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"))
        toEpoch.postValue(Timestamp(dt.atZone(ZoneId.of("Asia/Singapore")).toEpochSecond(), 0))
    }

    /**
     * actual function that validates date input string
     * @param date date in string
     */
    @SuppressLint("SimpleDateFormat")
    fun validateDate(date: String): Boolean {
        val format = SimpleDateFormat("dd/MM/yyyy")
        format.isLenient = false
        try {
            format.parse(date)
        } catch (e: ParseException) {
            return false
        }
        return true
    }

    /**
     * function that calls repository for retrieving attendance records base on selected date range
     * @param employeeuid employees' unique id
     * @param from starting date in timestamp
     * @param to ending date in timestamp
     */
    fun getCheckInDetails(employeeuid: String, from: Timestamp, to: Timestamp) =
        viewModelScope.launch(Dispatchers.IO) {
            IAttendanceList?.let {
                attendanceRepo.getSelectedDateRangeAttendanceDetails(
                    employeeuid,
                    from,
                    to,
                    it
                )
            }
        }

    /**
     * function that splits records retrieved from db to remote and office lists
     * @param attendanceList list of attendance records
     */
    fun splitListByRemoteOffice(attendanceList: List<Attendance>) {
        val remoteAttendanceList = mutableListOf<Attendance>()
        val officeAttendanceList = mutableListOf<Attendance>()

        for (attendance in attendanceList) {
            if (attendance.remote == true) {
                remoteAttendanceList.add(attendance)
            } else {
                officeAttendanceList.add(attendance)
            }
        }
        if (remoteAttendanceList.isNotEmpty()) {
            remoteAttendanceDetail.postValue(remoteAttendanceList)
        }
        if (officeAttendanceList.isNotEmpty()) {
            officeAttendanceDetail.postValue(officeAttendanceList)
        }
    }

    /**
     * get past records based on numberOfDays input
     */
    fun numberOfDaysSelected(numberOfDays: Int) {
        val startDate = LocalDate.now().minusDays(numberOfDays.toLong()).atStartOfDay()
            .atZone(ZoneId.of("Asia/Singapore")).toEpochSecond()
        val endTimeStamp = Timestamp.now()
        val startTimeStamp = Timestamp(startDate, 0)
        val userId = authenticationRepository.currentUser()?.uid.toString()
        Log.d("main", "startDate is: $startDate, endDate: $endTimeStamp")
        IAttendanceList?.let {
            attendanceRepo.getDateRangeAttendanceDetails(userId, startTimeStamp, endTimeStamp, it)
        }
    }
}

/**
 * Factory for ViewModel
 */
class TrackAttendanceViewModelFactory(private val attendanceRepo: AttendanceRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackAttendanceViewModel::class.java)) {
            return TrackAttendanceViewModel(AttendanceRepository(), AuthenticationRepository()) as T
        }
        throw IllegalArgumentException("Unknown class")
    }
}