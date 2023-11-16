package com.example.tracktogether.checkinviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.databinding.ActivitySuccesscheckoutBinding

/**
 * Activity for successful checkout
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuccesscheckoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccesscheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}