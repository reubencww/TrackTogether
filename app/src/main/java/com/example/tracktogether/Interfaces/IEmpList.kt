package com.example.tracktogether.Interfaces

import com.example.tracktogether.data.Employee

/**
 * Interface implemented by EmployeeListActivity  and IApproval
 * getAllEmployee(IEmpList: IEmpList) in EmployeeListRepository
 * and getAllNotApprovedEmployeeImage(imgapproval: IApproval) in ImageRepository
 * call onSuccessEmpList(employeeList: List<Employee>) upon successful fetch from Employee collection
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
interface IEmpList {
    fun onSuccessEmpList(employeeList: List<Employee>)
}