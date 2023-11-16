package com.example.tracktogether.repository

import android.util.Log
import com.example.tracktogether.Interfaces.IEmpList
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.data.Employee
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * This repo call object CloudFirestore NEED UPDATE
 * Take in user email, uri, id and firebaseListener(to check success)
 * Author: May Madi Aung
 * Updated: 27 Feb 2022
 */

class EmployeeListRepository {

    private val TAG = "EmployeeListRepository"
    private var storageReference = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()

    /**
     * Get all document rows from employee collection
     * @param IEmpList interface to pass in employee object list upon successful fetch from Firestore
     */

    suspend fun getAllEmployee(IEmpList: IEmpList) {
        val docRef = db.collection("employees")
        val query = docRef.whereEqualTo("role", "Employee").get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    var mutablelist = mutableListOf<Employee>()
                    for (document in documents) {
                        mutablelist.add(document.toObject(Employee::class.java))
                        //Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    IEmpList.onSuccessEmpList(mutablelist)
                } else {
                    Log.w(TAG, "No employees found ")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    /**
     * Get all document rows from employee collection
     * @param IEmpList interface to pass in employee object list upon successful fetch from Firestore
     */

    suspend fun getAllEmployeeByDepartment(empDepartment: String, IEmpList: IEmpList) {
        val docRef = db.collection("employees")
        val query = docRef.whereEqualTo("department", empDepartment).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    var mutablelist = mutableListOf<Employee>()
                    for (document in documents) {
                        mutablelist.add(document.toObject(Employee::class.java))
                        //Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    IEmpList.onSuccessEmpList(mutablelist)
                } else {
                    Log.w(TAG, "No employees found ")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


    /**
     * get all detail of current document in employee collection based UID (which is the document Key)
     * @param uid user id (current user)
     * @param IEmployee Interface that contain onSuccess method that return an employee object
     */
    suspend fun getEmployee(uid: String, IEmployee: IEmployee) {
        val docRef = db.collection("employees")


        val query = uid.let {
            docRef.document(it).get()
                .addOnSuccessListener { documents ->
                    IEmployee.onSuccessEmpObj(
                        Employee(
                            uid = documents["uid"] as String?,
                            firstName = documents["firstName"] as String?,
                            lastName = documents["lastName"] as String?,
                            phone = documents["phone"] as String?,
                            email = documents["email"] as String?,
                            designation = documents["designation"] as String?,
                            department = documents["department"] as String?,
                            dob = documents["dob"] as String?,
                            ApprovedRemoteCheckin = documents["approvedRemoteCheckin"] as String?,
                            imageUrl = documents["imageUrl"] as String?,
                            role = documents["role"] as String?,
                            gender = documents["gender"] as String?
                        )
                    )
                    Log.e(TAG, "Selected employee success ")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error selecting ", exception)
                }
        }

    }

    /**
     * Get an employee object and update all attribute of this employee object to Employee Document
     * Return successFLag = true upon update success
     * @param employee which is based on button clicked by admin (Approved or Rejected)
     * @param ISuccessFlag Interface that contain onSuccess method that return flag (true or false)
     */
    suspend fun setEmployee(employee: Employee, ISuccessFlag: ISuccessFlag) {
        val docRef = db.collection("employees")

        val query = employee.uid?.let {
            docRef.document(it).update(
                mapOf(
                    "firstName" to employee.firstName,
                    "lastName" to employee.lastName,
                    "phone" to employee.phone,
                    "designation" to employee.designation,
                    "department" to employee.department,
                    "dob" to employee.dob,
                    "gender" to employee.gender,

                    )
            ).addOnSuccessListener { documents ->
                Log.e(TAG, "Updated employees success ")
                ISuccessFlag.onSuccess(true)
            }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating ", exception)
                    ISuccessFlag.onSuccess(false)
                }
        }
    }


}

