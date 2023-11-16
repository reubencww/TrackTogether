package com.example.tracktogether.Interfaces

import com.example.tracktogether.data.Employee

/**
 * Interface implemented by EmployeeMain, LoginActivity and ProfileActivity
 * getUserRole(email: String, IEmployee: IEmployee) in AuthenticationRepo
 * and getEmployee(uid: String, IEmployee: IEmployee) in EmployeeListRepo
 * and etEmpRemoteCheckinStatus(uid: String, IEmployee: IEmployee) in ImageRepo
 * call onSuccessEmpObj(emp: Employee) upon successful fetch of an Employee document from Firestore
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
interface IEmployee {
    fun onSuccessEmpObj(emp: Employee)
}