package com.example.tracktogether.authviews

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.tracktogether.Interfaces.IAuthentication
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.MainActivity
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.data.Employee
import com.example.tracktogether.databinding.ActivityLoginBinding
import com.example.tracktogether.repository.UserPreferences
import com.example.tracktogether.viewmodel.AuthViewModel
import com.example.tracktogether.viewmodel.AuthViewModelFactory


/**
 * LoginActivity page using Firebase Auth
 * Author: May, Reuben (Manual login)
 *         Ze Quan and Jevan (Biometrics)
 * Updated: 24 Feb 2022
 */
class LoginActivity : AppCompatActivity(), IAuthentication, IEmployee {
    val TAG = LoginActivity::class.simpleName
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRole: String
    private lateinit var userPref: UserPreferences
    private var cancellationSignal: CancellationSignal? = null


    //Create an authentication callback
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            //If fingerprint is not recognized
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                authViewModel.signout()
                authViewModel.clearPref()
                notifyUser("Authentication Error : $errString")
            }

            //If fingerprint is recognized
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                //Succeed -> Main Activity
                sendToMain()
            }
        }


    // Animation state/ edit text focus
    var emailEditTextFocus = false
    var passwordEditTextFocus = false

    // Init a view model instance using the factory class
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            (application as TrackTogetherApp).authrepo,
            (application as TrackTogetherApp).userPreferencesRepository
        )
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // run start up animations
        setComponentsTransparent()
        setComponentsYAxis()
        loadPageAnimation()

        authViewModel.IAuthentication = this

        lifecycleScope.launchWhenStarted {

        }
        authViewModel.userPrefFlow.observe(this) { pref ->
            userPref = pref
            if (authViewModel.user != null) {
                if (userPref.enableBiometric && checkBiometricSupport()) {
                    setUpBiometricPrompt()
                    //Show fingerprint icon
                    binding.biometricLogin.visibility = View.VISIBLE
                    //Get and set current user's email
                    var user = authViewModel.user
                    var email = user?.email
                    //Need to set EditText as email if not cannot log in with password
                    binding.usernameEditText.setText(email.toString())
                    //Disable email edit text
                    binding.usernameEditText.isEnabled = false
                    //Set email as invisible
                    binding.usernameEditText.visibility = View.INVISIBLE
                    binding.emailAnimationImageView.visibility = View.INVISIBLE
                    //Display profile photo with current user email and enable button
                    binding.imageViewProfile.visibility = View.VISIBLE
                    binding.textViewProfile.text = user?.email
                    binding.textViewProfile.visibility = View.VISIBLE
                    binding.buttonNotYou.visibility = View.VISIBLE
                    binding.buttonNotYou.isEnabled = true
                }
            }
        }



        setUpClickListener()
        //Change account onclick listener
        binding.buttonNotYou.setOnClickListener {
            changeAccount()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setUpBiometricPrompt() {
        //Create biometric prompt
        val biometricPrompt = BiometricPrompt.Builder(this)
            .setTitle("Track Together")
            .setSubtitle("Biometric Authentication")
            .setDescription(
                "Session will close" +
                        " upon 5 failed attempts"
                        + " or when you cancel the authentication"
            )
            .setNegativeButton(
                "Cancel",
                this.mainExecutor,
                DialogInterface.OnClickListener { dialog, which ->
                    notifyUser("Authentication Cancelled")
                    //If cancel the finger print authentication, stay on the same page
                }).build()
        //Execute and display authentication prompt
        biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpClickListener() {

        // logic for logging in
        binding.loginButton.setOnClickListener {
            //Hars@12! and may@gmail.com

            binding.loginButton.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.usernameEditText.text.toString().trim()
            val passwd = binding.passwordEditText.text.toString()
            authViewModel.login(email, passwd)
            authViewModel.getUserRole(email)
            authViewModel.IAuthentication = this
            authViewModel.IEmployee = this

        }

        /*binding.biometricImageView.setOnClickListener() {
            fingerprintAuthentication()
        }*/

        // on touch for username, for animation
        binding.usernameEditText.setOnTouchListener { v, event ->
            if (emailEditTextFocus) {
                // email is already not focused, do nothing
            } else {
                // else animate
                emailEditTextFocus = true
                animateEmail(binding.emailAnimationImageView)
            }
            //if click email from password
            if (passwordEditTextFocus) {
                passwordEditTextFocus = false
                animatePassword(binding.passwordAnimationImageView)
            }
            false
        }

        //on touch for password, for animation
        binding.passwordEditText.setOnTouchListener { v, event ->
            if (passwordEditTextFocus) {
                // password is already not focused, do nothing
            } else {
                // else animate
                passwordEditTextFocus = true
                animatePassword(binding.passwordAnimationImageView)
            }
            //if click email from password
            if (emailEditTextFocus) {
                emailEditTextFocus = false
                animateEmail(binding.emailAnimationImageView)
            }
            false
        }

        binding.loginParentConstraint.setOnTouchListener { v, event ->
            // username focus check
            if (!emailEditTextFocus) {
                // email is already not focused, do nothing
            } else {
                // else animate
                emailEditTextFocus = false
                animateEmail(binding.emailAnimationImageView)
            }
            // password focus check
            if (!passwordEditTextFocus) {
                // email is already not focused, do nothing
            } else {
                // else animate
                passwordEditTextFocus = false
                animatePassword(binding.passwordAnimationImageView)
            }
            false
        }

        binding.biometricLogin.setOnClickListener{
            setUpBiometricPrompt()
        }
    }


    /**
     * Load components with separate duration
     */
    private fun loadPageAnimation() {
        binding.usernameEditText.animate().alpha(1f).translationYBy(-50F).duration = 500
        binding.emailAnimationImageView.animate().alpha(1f).translationYBy(-50F).duration = 500
        binding.passwordEditText.animate().alpha(1f).translationYBy(-50F).duration = 1000
        binding.passwordAnimationImageView.animate().alpha(1f).translationYBy(-50F).duration = 1000
        binding.loginButton.animate().alpha(1f).translationYBy(-50F).duration = 1500

    }

    /**
     * Set the edit text field etc as transparent first, for animation
     */
    private fun setComponentsTransparent() {
        binding.usernameEditText.alpha = 0f
        binding.passwordEditText.alpha = 0f
        binding.loginButton.alpha = 0f
        binding.emailAnimationImageView.alpha = 0f
        binding.passwordAnimationImageView.alpha = 0f
    }

    /**
     * Set lower y axis to simulate moving
     */
    private fun setComponentsYAxis() {
        binding.usernameEditText.translationY = 50f
        binding.passwordEditText.translationY = 50f
        binding.loginButton.translationY = 50f
        binding.emailAnimationImageView.translationY = 50f
        binding.passwordAnimationImageView.translationY = 50f
    }

    /**
     *  Animate the email icon based on edit text's focus
     *  @param imageView the image view icon view class
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun animateEmail(imageView: ImageView) {
        if (emailEditTextFocus) {
            // user click on email edit text
            imageView.setImageDrawable(resources.getDrawable(R.drawable.avd_login_email_onclick))
            (imageView.drawable as? Animatable)?.start()
        } else if (!emailEditTextFocus) {
            //user click on parent constraint, i.e. any other place on screen other then edit text
            imageView.setImageDrawable(resources.getDrawable(R.drawable.avd_login_account_to_mail))
            (imageView.drawable as? Animatable)?.start()
        }

    }

    /**
     * Animate the password icon based on edit text's focus
     * @param imageView the image view icon view class
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun animatePassword(imageView: ImageView) {
        if (passwordEditTextFocus) {
            // user click on email edit text
            imageView.setImageDrawable(resources.getDrawable(R.drawable.avd_login_lock_close_to_open))
            (imageView.drawable as? Animatable)?.start()
        } else if (!passwordEditTextFocus) {
            //user click on parent constraint, i.e. any other place on screen other then edit text
            imageView.setImageDrawable(resources.getDrawable(R.drawable.avd_login_lock_open_to_close))
            (imageView.drawable as? Animatable)?.start()
        }

    }

    /**
     * Log message once user is in login page
     */
    override fun onStarted() {
        Log.v(TAG, "started")
    }

    override fun onStart() {
        super.onStart()
    }

    /**
     * Display toast and intent to MainActivity if login successful
     */
    override fun onSuccess() {
        Log.v(TAG, authViewModel.user.toString())
        Toast.makeText(applicationContext, "Login success!", Toast.LENGTH_SHORT).show()
        if (userPref.firstLogin) {
            authViewModel.userPrefFlow.removeObservers(this);
            authViewModel.updateLoginPref(false)
            showBiometricAlert()
        } else {
            sendToMain()
        }
    }

    /***
     * Ask user if biometric is to be enabled
     */
    private fun showBiometricAlert() {
        if (checkBiometricSupport()) {
            //Create a Prompt to check if user wants to enable fingerprint login method
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Fingerprint Authentication")
            builder.setMessage("Do you want to enable fingerprint authentication?")

            /*
            If SelectedFingerprintPreference = "true",
            it means that they have completed the alert dialog
             */

            //Scenario A: User presses "yes"
            builder.setPositiveButton("yes") { _, _ ->
                authViewModel.userPrefFlow.removeObservers(this);
                authViewModel.updateBiometricPref(true)
                sendToMain()
            }
            //Scenario B: User presses "no"
            builder.setNegativeButton("no") { _, _ ->
                authViewModel.userPrefFlow.removeObservers(this);
                authViewModel.updateBiometricPref(false)
                sendToMain()
            }
            //Create the Alert Dialog
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        } else {
            authViewModel.userPrefFlow.removeObservers(this);
            authViewModel.updateBiometricPref(false)
            sendToMain()
        }
    }

    /***
     * Send to main activity
     */
    private fun sendToMain() {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        binding.loginButton.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
        finish()
    }

    /**
     * Display error message
     * @param message Error message from firebase login
     */
    override fun onFailure(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        binding.loginButton.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

    /**
     * From IEmployee
     * authViewModel.getUserRole(email)
     * Fetch Employee Role from Firestore
     * Store role and isProfileEmpty in DataStore
     * @param emp Employee details
     */
    override fun onSuccessEmpObj(emp: Employee) {
        userRole = emp.role.toString()
        val approvedRemoteCheckin = emp.ApprovedRemoteCheckin.toString()
        Log.e(TAG, "user role:$userRole")
        authViewModel.userPrefFlow.removeObservers(this);
        authViewModel.saveToDataStore(userRole, approvedRemoteCheckin)
        authViewModel.setDeviceToken(emp.uid.toString())
        if ((emp.firstName == null && emp.lastName == null) || emp.imageUrl == null) {
            authViewModel.saveProfileStatus(true)
        } else {
            authViewModel.saveProfileStatus(false)
        }
    }

    //Obtain biometric cancellation signal
    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was Cancelled by the user, will kill this logged in session")
        }
        return cancellationSignal as CancellationSignal
    }

    //Checks if the app has permission to use fingerprint sensor
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isDeviceSecure) {
            notifyUser("Fingerprint authentication has not been enabled in settings")
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notifyUser("Fingerprint Authentication Permission is not enabled")
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    //Function to show a toast after user used the fingerprint scanner
    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    companion object {
        private const val TAG = "LoginActivity"
    }

    //Function prompt "Not You?"
    private fun changeAccount() {
        //Create a Prompt to check if user wants change account
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Deregister your profile?")
        builder.setMessage("You will no longer be able to log in using biometrics until" +
                " you register your profile again")

        //Scenario A: User presses "yes"
        builder.setPositiveButton("yes") { _, _ ->
            authViewModel.clearPref()
            val intent = Intent(this@LoginActivity, LoginActivity::class.java)
            startActivity(intent)

        }
        //Scenario B: User presses "cancel"
        builder.setNeutralButton("Cancel") { _, _ ->
            //Do Nothing
        }
        //Create the Alert Dialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}
