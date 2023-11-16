package com.example.tracktogether.data

/**
 * Data class needed for pushing notification
 * Author: Cheng Hao
 * Updated: 13 Mar 2022
 */

data class PushNotification(
    val data: NotificationData,
    val to: String
)