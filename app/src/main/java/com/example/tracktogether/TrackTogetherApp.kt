package com.example.tracktogether


import android.app.Application
import com.example.tracktogether.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * To get the repo from application itself (follow Prof chek examples)
 * need to remember to add this to manifest
 * Author: May Madi Aung, Reuben, Cheng Hao
 * Updated: 9 March 2022
 */
class TrackTogetherApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val authrepo by lazy { AuthenticationRepository() }

    //for listing of all employees
    val employeeListRepo by lazy { EmployeeListRepository() }

    //for listing of all employees
    val imageRepository by lazy { ImageRepository() }

    //for user preference
    val userPreferencesRepository by lazy { UserPreferencesRepository.getInstance(this) }
    val officeCRUDRepository by lazy { OfficeCRUDRepository.getInstance(this) }

    //for attendanceRepo
    val attendanceRepository by lazy { AttendanceRepository() }
}

