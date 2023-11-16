package com.example.tracktogether.adminviews

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tracktogether.R
import com.example.tracktogether.databinding.ActivityAddOfficeBinding
import com.example.tracktogether.fragments.OfficeCreate
import com.example.tracktogether.fragments.OfficeRead

/**
 * Main office edit activity to house CRUD fragments
 * Author: Reuben
 * Updated: 9 March 2022
 */
class AddOfficeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOfficeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOfficeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //display read first
        displayFragmentBasedOnUserSelection(OfficeRead())

        binding.createOfficeButton.setOnClickListener {
            displayFragmentBasedOnUserSelection(OfficeCreate())
        }

        binding.readOfficeButton.setOnClickListener {
            displayFragmentBasedOnUserSelection(OfficeRead())
        }

    }

    private fun displayFragmentBasedOnUserSelection(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.editofficeFragmentView, fragment).commit()
    }


}