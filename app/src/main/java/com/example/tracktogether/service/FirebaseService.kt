package com.example.tracktogether.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.tracktogether.MainActivity
import com.example.tracktogether.R
import com.example.tracktogether.data.PushNotification
import com.example.tracktogether.instance.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * FirebaseService inherits FirebaseMessagingService
 * Mainly for making use of notification services provided by firebase
 * Author: Cheng Hao
 * Updated: 11 Mar 2022
 */
class FirebaseService : FirebaseMessagingService() {

    companion object {
        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = "AAAA3VzCkx0:APA91bEExT2em6mbcxHmEYg3L594x6MaJSnmbQvD0QqdEOPptqeUgxIIJ9oHSl9YMuaJ8bO40Pnubi4KIn57bbMORQBVuDfqWcvW6EMqybZYIULAy_3v2wlCcGNKJSgJrPGoxiPUt8BR"
        const val CONTENT_TYPE = "application/json"
        const val TOPIC = "/topics/KauLa"
        const val CHANNEL_ID = "KauLa Channel"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        createNotificationChannel(notificationManager)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.app_logo_v2)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    /**
     * creates notification channel for main application
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "Upload Image Status"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * sending notification to device by making api call
     * @param notification of type PushNotification which contains information about the device to send to
     */
    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("main", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("main", response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e("main", e.toString())
        }
    }
}