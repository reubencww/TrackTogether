package com.example.tracktogether.data

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Data class to receive documents from Firebase Firestore Query
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
data class AttendanceResponse(
    var document: MutableList<DocumentSnapshot>? = null,
    var exception: Exception? = null
)