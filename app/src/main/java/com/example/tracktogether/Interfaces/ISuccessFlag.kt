package com.example.tracktogether.Interfaces


/**
 * Interface implemented by SettingActivity and ProfileActivity
 * Use by some repo to return success flag upon update to FireStore
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
interface ISuccessFlag {
    fun onSuccess(flag: Boolean)
}