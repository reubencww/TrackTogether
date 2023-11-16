package com.example.tracktogether.data

import com.google.firebase.Timestamp

/**
 * Data class for Attendance
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
data class Attendance(
    val employeeUID: String? = null,
    val inTime: Timestamp? = null,
    val outTime: Timestamp? = null,
    val remote: Boolean? = null,
    val location: String? = null,
    var imageUrl: String? = null,
    val date: Timestamp? = null,
)