package com.example.tracktogether.data

import android.net.Uri

/**
 * Data class to receive upload response from Firebase Storage
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
data class ImageUploadResponse(
    var imageUri: Uri? = null,
    var downloadUri: Uri? = null,
    var exception: Exception? = null
)