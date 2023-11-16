package com.example.tracktogether.repository

import android.util.Log
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.data.Employee
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable


/**
 * AuthenticationRepository call FirebaseAuth which is a service provided by Firebase to authenticate users
 * We are using custom authentication using email and password
 * Author: May Madi Aung
 * Updated: 23 Feb 2022
 */
class AuthenticationRepository {
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val TAG = "AuthenticationRepository"


    /**
     *  Creating a Completable from reactivex and inside the completable we are performing the authentication.
     *  Once it is completed we are using the emitter to indicated that the task is completed or failed.
     *  @param email email entered by user
     *  @param password password entered by user
     */
    fun login(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!emitter.isDisposed) {
                if (it.isSuccessful)
                    emitter.onComplete()
                else
                    emitter.onError(it.exception!!)
            }
        }
    }

    /**
     * Completable to observe successful login
     * Upon register success to FirebaseAuth, we will try to insert employee to Employee Collection
     *  @param employee employee object with employee details for register
     */
    fun register(employee: Employee) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(
            employee.email.toString(),
            employee.password.toString()
        )
            .addOnCompleteListener {
                if (!emitter.isDisposed) {
                    if (it.isSuccessful)
                        firebaseAuth.currentUser?.let { user ->
                            employee.password = null
                            employee.uid = user.uid
                            db.collection("employees").document(user.uid).set(employee)
                            firebaseAuth.sendPasswordResetEmail(employee.email.toString())
                                .addOnCompleteListener {
                                    emitter.onComplete()
                                }
                        }
                    else
                        emitter.onError(it.exception!!)
                }
            }
    }

    /**
     * Signout user from account
     */
    fun logout() = firebaseAuth.signOut()

    /**
     * @return current user
     */
    fun currentUser() = firebaseAuth.currentUser

    /**
     * @return current user email
     */
    fun currentUserEmail(): String = firebaseAuth.currentUser?.email ?: ""

    /**
     * @return current user uid
     */
    fun currentUserID(): String = firebaseAuth.currentUser?.uid ?: ""

    /**
     * Fetch user role from employee document based on employee email
     * @param email current login user email
     * @param IEmployee interface to pass in employee object upon successful fetch from Firestore
     */
    fun getUserRole(email: String, IEmployee: IEmployee) {
        Log.d("main", email.toString())
        val docRef = db.collection("employees")
        val query = docRef.whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        IEmployee.onSuccessEmpObj(document.toObject(Employee::class.java))
                    }
                } else {
                    Log.w(TAG, "No employees found ")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun setDeviceToken(uid: String, deviceId: String) {
        val docRef = db.collection("device").document(uid)
        val data: MutableMap<String, Any> = HashMap()
        data["uid"] = uid
        data["deviceId"] = deviceId
        docRef.set(data)
    }
}

