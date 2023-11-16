package com.example.tracktogether

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.authviews.LoginActivity
import com.example.tracktogether.databinding.ActivityMainBinding
import com.example.tracktogether.fragments.AdminMain
import com.example.tracktogether.fragments.EmployeeMain
import com.example.tracktogether.viewmodel.MainViewModel
import com.example.tracktogether.viewmodel.MainViewModelFactory
import com.google.firebase.auth.FirebaseAuth


/**
 * Main Activity to house employee and admin fragments
 * Author: Reuben
 * Updated: 9 March 2022
 */
class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private lateinit var binding: ActivityMainBinding

    private var userRole = ""

    var auth = FirebaseAuth.getInstance()

    // viewmodel instance
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (application as TrackTogetherApp).userPreferencesRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Display fragment based on user role from user pref repo
        mainViewModel.userRole.observe(this) { role ->
            setUserRole(role)
            displayFragmentBasedOnRole(userRole)
        }

        //Logout functionality which will trigger the activity lifecycle
        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            mainViewModel.clearPref()
            finish()
        }
    }

    private fun setUserRole(userRole: String) {
        this.userRole = userRole
    }


    private fun displayFragmentBasedOnRole(userRole: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        when (userRole) {
            "Admin" -> {
                fragmentTransaction.replace(R.id.mainViewUserFragment, AdminMain()).commit()
            }
            else -> {
                fragmentTransaction.replace(R.id.mainViewUserFragment, EmployeeMain()).commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        if (auth.currentUser == null) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}