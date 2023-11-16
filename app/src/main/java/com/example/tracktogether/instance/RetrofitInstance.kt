package com.example.tracktogether.instance

import com.example.tracktogether.api.NotificationAPI
import com.example.tracktogether.service.FirebaseService.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit REST client instance to consume the Rest Web services
 * Author: Cheng Hao
 * Updated: 11 Mar 2022
 */
class RetrofitInstance {

    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}