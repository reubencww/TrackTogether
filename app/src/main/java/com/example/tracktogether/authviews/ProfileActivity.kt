package com.example.tracktogether.authviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.R
import com.example.tracktogether.databinding.ActivityProfileBinding


/**
 * TODO
 */

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        displayViewFragment()


    }

    private fun displayViewFragment() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.editprofilefragment, ProfileView()).commit()

    }
}
