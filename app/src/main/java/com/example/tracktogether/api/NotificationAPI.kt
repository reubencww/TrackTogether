package com.example.tracktogether.api

import com.example.tracktogether.data.PushNotification
import com.example.tracktogether.service.FirebaseService.Companion.CONTENT_TYPE
import com.example.tracktogether.service.FirebaseService.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Notification api for post notification based on firebase cloud messaging server key and content type
 * Author: Cheng Hao
 * Updated: 11 Mar 2022
 */
interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}