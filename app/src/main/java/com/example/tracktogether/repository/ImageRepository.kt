package com.example.tracktogether.repository

import android.net.Uri
import android.util.Log
import com.example.tracktogether.Interfaces.IApproval
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.data.Employee
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

/**
 * This repo call object CloudFirestore NEED UPDATE
 * Take in user email, uri, id and firebaseListener(to check success)
 * Author: May Madi Aung
 * Updated: 27 Feb 2022
 */
class ImageRepository {
    private val TAG = "ImageRepository"
    private var storageReference = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()

    /**
     * Upload employee image to Firebase Storage on the cloud
     * Code Ref: https://stackoverflow.com/questions/61610024/how-to-upload-an-image-to-firebase-storage-using-kotlin-in-android-q
     * @param uid which is the userid which is being used as Document Key in Employee Collection
     * @param employee which include all employee details
     * @param ISuccessFlag Interface that contain onSuccess method that is to be implemented in View
     */
    suspend fun uploadImageToFirebase(
        fileUri: Uri,
        employee: Employee,
        ISuccessFlag: ISuccessFlag
    ) {
        val ref = storageReference.child("employee_image/${employee.uid}")
        val uploadTask = ref.putFile(fileUri)

        val urlTask =
            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                        Log.e(TAG, "Failed to Listen ")
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.v(TAG, "Success ")
                    val downloadUri = task.result
                    employee.imageUrl = downloadUri.toString()
                    addUploadRecordToDb(employee, ISuccessFlag)
                } else {
                    // Handle failures
                    ISuccessFlag.onSuccess(false)
                    Log.e(TAG, "Failed")
                }
            }.addOnFailureListener {
                ISuccessFlag.onSuccess(false)
                Log.e(TAG, "Failed")
            }
    }

    /**
     * Get the status of employee's photo
     * @param employee include ApprovedRemoteCheckin, email and uid
     * @param IEmployee Interface that contain onSuccess method that return an employee object
     */
    suspend fun getEmpRemoteCheckinStatus(uid: String, IEmployee: IEmployee) {
        val docRef = db.collection("employees")

        val query = uid.let {
            docRef.document(it).get()
                .addOnSuccessListener { documents ->
                    IEmployee.onSuccessEmpObj(
                        Employee(
                            ApprovedRemoteCheckin = documents["approvedRemoteCheckin"] as String?,
                            email = documents["email"] as String?,
                            uid = documents["uid"] as String?
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
     * Set the status of employee's photo to either Approved or Rejected
     * @param status which is based on button clicked by admin (Approved or Rejected)
     * @param uid user id which is the Document Key of the employees Collection
     * @param imgapproval Interface that contain onSuccess method that return an employee object
     */
    suspend fun setStatusEmployeeImage(status: String, uid: String, imgapproval: IApproval) {
        val docRef = db.collection("employees")

        val query = docRef.document(uid).update(
            mapOf(
                "approvedRemoteCheckin" to status
            )
        ).addOnSuccessListener { documents ->
            Log.e(TAG, "Updated employees image success ")
            imgapproval.changeApprovalStatus(true, status)
        }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating ", exception)
                imgapproval.changeApprovalStatus(false, status)
            }
    }

    /**
     * Get information employee that have In-review employee's photo
     * Employee's photo is used for remote checkin
     * @param imgapproval Interface that contain onSuccessEmpList method that return an employee object
     */
    suspend fun getAllNotApprovedEmployeeImage(imgapproval: IApproval) {
        val docRef = db.collection("employees")
        val query = docRef.whereEqualTo("approvedRemoteCheckin", "In-review").get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    var mutablelist = mutableListOf<Employee>()
                    for (document in documents) {
                        //mutablelist.add(document.toObject(EmployeeImage::class.java))
                        mutablelist.add(document.toObject(Employee::class.java))
                    }
                    imgapproval.onSuccessEmpList(mutablelist)
                    Log.e(TAG, "Fetched all employees image success ")
                } else {
                    Log.e(TAG, "No employees image found ")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents: ", exception)
            }
    }

    /**
     * Update approvedRemoteCheckin to in-review, add imageUrl and fileuri
     * imageUrl is the image that was uploaded by amployee
     * @param uid which is the userid which is being used as Document Key in Employee Collection
     * @param employee which include all employee details
     * @param ISuccessFlag Interface that contain onSuccess method that is to be implemented in View
     */
    private fun addUploadRecordToDb(employee: Employee, ISuccessFlag: ISuccessFlag) {
        val docRef = db.collection("employees")
        Log.v(TAG, employee.uid.toString())

        val query = employee.uid?.let {
            docRef.document(it).update(
                mapOf(
                    "approvedRemoteCheckin" to employee.ApprovedRemoteCheckin,
                    "imageUrl" to employee.imageUrl
                )
            ).addOnSuccessListener { documentReference ->
                ISuccessFlag.onSuccess(true)
                Log.v(TAG, "Passed")
            }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to Listen ")
                    ISuccessFlag.onSuccess(false)
                }
        }
    }


}