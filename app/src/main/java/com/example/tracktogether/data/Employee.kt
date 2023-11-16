package com.example.tracktogether.data


/**
 * Entity class for the Firestore table employee
 * Author: May Madi
 * Updated: 1 Mar 2022
 */
class Employee(
    var uid: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    var password: String? = null,
    val designation: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val department: String? = null,
    val role: String? = "Employee",
    var imageUrl: String? = null,
    val ApprovedRemoteCheckin: String? = "Waiting"
)