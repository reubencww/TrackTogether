package com.example.tracktogether.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.Interfaces.IApproval
import com.example.tracktogether.Interfaces.IEmpList
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.data.Employee
import com.example.tracktogether.repository.AuthenticationRepository
import com.example.tracktogether.repository.EmployeeListRepository
import com.example.tracktogether.repository.ImageRepository
import com.example.tracktogether.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * View Model for Employee related operations
 * Author: May Madi Aung
 * Updated: 6 Mar 2022
 */
class EmployeeViewModel(
    private val employeeListRepository: EmployeeListRepository,
    private val imageRepository: ImageRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    var iEmpList: IEmpList? = null
    var iEmployee: IEmployee? = null
    var iSuccessFlag: ISuccessFlag? = null
    var iApproval: IApproval? = null
    var checkinI: ISuccessFlag? = null

    /**
     * retrieve all registered employees from DB
     */
    fun getAllEmployee() = viewModelScope.launch(Dispatchers.IO) {
        iEmpList?.let { employeeListRepository.getAllEmployee(it) }
    }

    /**
     * retrieve all registered employees from DB
     */
    fun getAllEmployeeByDepartment(empDepartment: String) = viewModelScope.launch(Dispatchers.IO) {
        iEmpList?.let { employeeListRepository.getAllEmployeeByDepartment(empDepartment, it) }
    }

    /**
     * retrieve employee with image in-review
     */
    fun getAllNotApprovedEmployeeImage() = viewModelScope.launch(Dispatchers.IO) {
        iApproval?.let { imageRepository.getAllNotApprovedEmployeeImage(it) }
    }

    /**
     * change status of image to approve or reject
     * @param status approve or reject
     */
    fun setStatusEmployeeImage(status: String, imageUrl: String) =
        viewModelScope.launch(Dispatchers.IO) {
            iApproval?.let {
                imageRepository.setStatusEmployeeImage(
                    status,
                    imageUrl,
                    it
                )
            }
        }

    /**
     * @return userid of user login
     */
    fun currentUserID(): String {
        return authenticationRepository.currentUserID()
    }
    // Auth Repo Access
    fun currentUserEmail(): String {
        return authenticationRepository.currentUserEmail()
    }
    /**
     * get Employee based on uid
     * @param uid user id (document key)
     */
    fun getEmployee(uid: String) = viewModelScope.launch(Dispatchers.IO) {
        iEmployee?.let { employeeListRepository.getEmployee(uid, it) }
    }

    /**
     * update Employee details
     * @param employee employee object
     */
    fun setEmployee(employee: Employee) = viewModelScope.launch(Dispatchers.IO) {
        iSuccessFlag?.let { employeeListRepository.setEmployee(employee, it) }
    }

    /**
     * store device token in userpref repo
     * @param token unique device id
     */
    fun storeDeviceToken(token: String) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.storeDeviceToken(token)
    }

    fun uploadImageToFirebase(fileUri: Uri, employee: Employee) =
        viewModelScope.launch(Dispatchers.IO) {
            checkinI?.let { imageRepository.uploadImageToFirebase(fileUri, employee, it) }
        }

}

/**
 * Factory for ViewModel
 */
class EmployeeListViewModelFactory(private val employeeRepo: EmployeeListRepository, private val imageRepository: ImageRepository,
                                   private val userPreferencesRepository: UserPreferencesRepository, private val authenticationRepository: AuthenticationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeViewModel::class.java)) {
            return EmployeeViewModel(EmployeeListRepository(), ImageRepository(), userPreferencesRepository, AuthenticationRepository()) as T
        }
        throw IllegalArgumentException("Unknown class")
    }
}