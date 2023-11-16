package com.example.tracktogether.viewmodel

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.*
import com.example.tracktogether.Interfaces.IAuthentication
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.data.Employee
import com.example.tracktogether.repository.AuthenticationRepository
import com.example.tracktogether.repository.UserPreferencesRepository
import com.example.tracktogether.service.FirebaseService.Companion.TOPIC
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * ViewModel to handle the email and password for auth login
 * Author: May Madi Aung and Reuben
 * Updated: 27 Feb 2022
 */
class AuthViewModel(
    private val repository: AuthenticationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {


    val userPrefFlow = userPreferencesRepository.userPreferencesFlow.asLiveData()

    val TAG = "AuthViewModel"

    //show biometric based on this var
    val biometricPref = userPreferencesRepository.biometricFlow.asLiveData()
    //auth listener
    var IAuthentication: IAuthentication? = null
    var IEmployee: IEmployee? = null

    //firestore listener
    lateinit var firestoreI: ISuccessFlag

    //disposable to dispose the Completable
    private val disposables = CompositeDisposable()

    val user by lazy {
        repository.currentUser()
    }

    /**
     * do simple validation of email and call loginAuthentication(email, password)
     * @param email email user entered
     * @param password password user entered
     */
    fun login(email: String, password: String): Boolean {
        //validating email and password
        loginAuthentication(email, password)
        return if (email.isEmpty() || password.isEmpty()) {
            IAuthentication?.onFailure("Invalid email or password")
            false
        } else if(!isValidEmail(email)) {
            IAuthentication?.onFailure("Invalid email or password")
            false
        }else{
            true
        }

    }

    /**
     * user logout
     */
    fun logout() {
        repository.logout()
    }


    /**
     * Sign up user
     * @param employee object contain user details
     */
    fun signup(employee: Employee) {
        if (employee.email.isNullOrEmpty() || employee.password.isNullOrEmpty()) {
            IAuthentication?.onFailure("Please input all values")
            return
        }
        IAuthentication?.onStarted()
        val disposable = repository.register(employee)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                IAuthentication?.onSuccess()
                Log.v(TAG, "Registered user success and send reset email!")
            }, {
                IAuthentication?.onFailure(it.message!!)
            })
        disposables.add(disposable)
    }

    /**
     * @return email of user login
     */
    fun signout() {
        return repository.logout()
    }



    /**
     * Login logic using Auth repo
     * @param email email user entered
     * @param password password user entered
     */
     private fun loginAuthentication(email: String, password: String) {
        //authentication started
        IAuthentication?.onStarted()


        //calling login from repository to perform the actual authentication
        val disposable = repository.login(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //sending a success callback
                IAuthentication?.onSuccess()
                Log.v(TAG, "Login user success")

            }, {
                //sending a failure callback
                IAuthentication?.onFailure(it.message!!)
                Log.v(TAG, "Login user failed"+it.message!!)
            })
        disposables.add(disposable)
    }


    /**
     * getUserRole from Employee Document based on useremail
     * @param email email user login
     */
    fun getUserRole(email: String) = viewModelScope.launch(Dispatchers.IO) {
        IEmployee?.let { repository.getUserRole(email, it) }
    }

    /**
     * store user role to UserPreferencesRepository
     * @param userRole role of user login
     */
    fun saveToDataStore(userRole: String, remoteApproved: String) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.saveProfileStatus(userRole)
        userPreferencesRepository.updateRemoteApproved(remoteApproved)
    }

    fun updateBiometricPref(state: Boolean) = viewModelScope.launch (Dispatchers.IO){
        userPreferencesRepository.saveBiometricPref(state)
    }

    fun updateLoginPref(state: Boolean) = viewModelScope.launch (Dispatchers.IO){
        userPreferencesRepository.saveLoginPref(state)
    }

    fun clearPref() = viewModelScope.launch(Dispatchers.IO){
        userPreferencesRepository.clearPref()
    }




    /**
     * updates firebase device db on success logging in
     * @param uid employee unique id
     */
    fun setDeviceToken(uid: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            repository.setDeviceToken(uid, it)
            Log.d("main", "this device token id is: $it")
        }
    }

    fun saveProfileStatus(isProfileEmpty: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.saveProfileStatus(isProfileEmpty)
    }

    /**
     * Validate email
     * @param target email user entered
     */
    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    /**
     * TODO remove?
     * Password validation criteria
     * 1 digit must occur
     * 1 lower case letter must occur
     * 1 upper case letter must occur
     * 1 special char must occur (#?!@$%^&*-)
     * no white space in string
     * At least 8 chars
     * e.g. Hars@12!
     */
    /*
    // Check for password validity
    private fun isValidPassword(password: String?): Boolean {
        password?.let {
            val passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$"
            val passwordMatcher = Regex(passwordPattern)
            return passwordMatcher.find(password) != null
        } ?: return false
    }*/


    /**
     * Live data being observed on Register Employee Activity
     * Ensure email is valid for registrations
     */
    val registerStatus = MutableLiveData<Boolean>()
    val errorEmailMsg = MutableLiveData<String>()

    /**
     * If email is invalid, Register Employee Activity will display error on editText and toast
     * @param employee employee object with employee details
     */
    fun registerEmailChecker(employee: Employee) {
        if (!isValidEmail(employee.email.toString())) {
            errorEmailMsg.postValue("Invalid email address")
            registerStatus.postValue(false)
        } else {
            registerStatus.postValue(true)
        }
    }


    //disposing the disposables
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}

/**
 * Factory for ViewModel
 */
class AuthViewModelFactory(
    private val repository: AuthenticationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(AuthenticationRepository(), userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown Class")
    }
}