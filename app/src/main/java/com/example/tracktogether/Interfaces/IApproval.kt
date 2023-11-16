package com.example.tracktogether.Interfaces

/**
 * Interface implemented by ApprovedRemoteCheckinActivity
 * inherited onSuccessEmpList(employeeList: List<Employee>) from IEmpList
 * setStatusEmployeeImage(status:String, uid:String, imgapproval: IApproval) in ImageRepo will pass in the
 * FireStore update success/failure Flag and the status it was updated to
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
interface IApproval : IEmpList {
    fun changeApprovalStatus(flag: Boolean, status: String)
}