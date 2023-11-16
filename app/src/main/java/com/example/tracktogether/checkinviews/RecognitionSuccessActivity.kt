package com.example.tracktogether.checkinviews

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.databinding.ActivityRecognitionsuccessBinding

/**
 * Activity for successful remote checkin via facial recognition
 * Author: Ong Ze Quan
 * Updated: 5 Mar 2022
 */
class RecognitionSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecognitionsuccessBinding
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognitionsuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        imageView = binding.imageView
        imageView.setImageURI(imageUri)

    }
}