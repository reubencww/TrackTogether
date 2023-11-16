package com.example.tracktogether.Interfaces

/**
 * Interface implemented by ApprovedRemoteCheckinActivity
 * on approval made uses interface INotification for callbacks
 * Author: Cheng Hao
 * Updated: 11 Mar 2022
 */
interface INotification {
    fun onApproval(deviceid: String, approval: Boolean)
}