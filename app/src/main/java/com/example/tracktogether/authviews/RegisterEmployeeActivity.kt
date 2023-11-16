package com.example.tracktogether.authviews


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.Interfaces.IAuthentication
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.data.Employee
import com.example.tracktogether.databinding.ActivityRegisterEmployeeBinding
import com.example.tracktogether.viewmodel.AuthViewModel
import com.example.tracktogether.viewmodel.AuthViewModelFactory


/**
 * Register a user using email and auto generated random password into FireBase auth
 * Upon successful registration. Firebase Auth reset password is triggered.
 * User will receive email with instruction to reset password on their registered email
 * Upon successful reset password user to login with new credentials
 * Author: May Madi Aung
 * Updated: 05 March 2022
 */

class RegisterEmployeeActivity : AppCompatActivity(), IAuthentication {

    private lateinit var binding: ActivityRegisterEmployeeBinding
    val TAG = "RegisterEmployeeActivity"

    private var status = false
    private lateinit var email: String
    private lateinit var password: String
    private val STRING_CHARACTERS = ('0'..'z').toList().toTypedArray()

    // Init a view model instance using the factory class
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            (application as TrackTogetherApp).authrepo,
            (application as TrackTogetherApp).userPreferencesRepository
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Register user using email and random password
         * User details is also inserted to Employee Collection
         * will all details null except uid and email...
         * After register will trigger an account reset email.
         */
        binding.registerempButton.setOnClickListener {
            email = binding.empemailEditText.text.toString()
            //generate random password
            password = (1..32).map { STRING_CHARACTERS.random() }.joinToString("")
            //create a entity to add to db
            val employee = Employee(email = email, password = password)
            Log.e(TAG, password)
            authViewModel.registerEmailChecker(employee)

            if (status) {
                Log.e(TAG, "here")
                authViewModel.signup(employee)
                authViewModel.IAuthentication = this
            }
        }

        // observe live data from View Model
        authViewModel.errorEmailMsg.observe(this) {
            binding.empemailEditText.error = it
        }

        // observe live data from View Model
        authViewModel.registerStatus.observe(this) {
            if (!it) {
                Log.e(TAG, "Please fill up all rows correctly!")
            } else {
                status = true
            }
            Log.e(TAG, status.toString())
        }
    }

    //
    /**
     * From IAuthentication
     * authViewModel.signup(employee)
     * upon start log message
     */
    override fun onStarted() {
        Log.v(TAG, "started")
    }

    /**
     * From IAuthentication
     * authViewModel.signup(employee)
     * Success Toast is displayed on success
     */
    override fun onSuccess() {
        Toast.makeText(applicationContext, "Register success!", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Success! Sending email...")

    }


    /**
     * From IAuthentication
     * authViewModel.signup(employee)
     * Error message Toast is displayed on failed
     */
    override fun onFailure(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Failed")
    }


}



