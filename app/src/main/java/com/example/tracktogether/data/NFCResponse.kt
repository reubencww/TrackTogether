package com.example.tracktogether.data

/**
 * Data class to receive NFC query from Firebase Firestore
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
data class NFCResponse(
    var nfcId: String? = null,
    var exception: Exception? = null
)