package com.example.tracktogether.Interfaces


/**
 * Interface implemented by RegisterEmployeeActivity and LoginActivity
 * signup(employee:Employee) and loginAuthentication(email: String, password: String) in AuthViewModel
 * call onSuccess() or onFailure(message: String) depending on the emitter status
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
interface IAuthentication {
    fun onStarted()
    fun onSuccess()
    fun onFailure(message: String)

}