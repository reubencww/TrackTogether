package com.example.tracktogether.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tracktogether.Interfaces.IAuthentication
import com.example.tracktogether.viewmodel.AuthViewModel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthViewModelTest : IAuthentication {
    private var successFlag: Boolean = false
    private var startedFlag: Boolean = false
    private var failureFlag: Boolean = false
    val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AuthenticationRepository()

    // Init a view model instance using the factory class
    private val authViewModel = AuthViewModel(AuthenticationRepository(),
        UserPreferencesRepository(appContext))


    override fun onStarted() {
        startedFlag = true
    }

    override fun onSuccess() {
        successFlag = true
    }

    override fun onFailure(message: String) {
        failureFlag = true
    }

    @Test
    fun login_Started_IsCorrect() {
        authViewModel.IAuthentication = this
        authViewModel.login(email = "test@test.com", password = "test1234")
        Assert.assertEquals(true, startedFlag)
    }

    @Test
    fun passwordEmpty_login_Failed_IsCorrect() {
        authViewModel.IAuthentication = this
        authViewModel.login(email = "test@test.com", password = "")
        Assert.assertEquals(true, failureFlag)
    }

    @Test
    fun emailEmpty_login_Failed_IsCorrect() {
        authViewModel.IAuthentication = this
        authViewModel.login(email = "", password = "122333")
        Assert.assertEquals(true, failureFlag)
    }

    @Test
    fun emailFormatIncorrect_login_Failed_IsCorrect() {
        authViewModel.IAuthentication = this
        authViewModel.login(email = "sddffee#dddd", password = "122333")
        Assert.assertEquals(true, failureFlag)
    }

    @Test
    fun login_Success_IsCorrect() {
        authViewModel.IAuthentication = this
        successFlag = authViewModel.login(email = "maymadiaung99@gmail.com", password = "Hars@12!")
        Assert.assertEquals(true, successFlag)
    }
}