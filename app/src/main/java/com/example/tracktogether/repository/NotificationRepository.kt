package com.example.tracktogether.repository

import android.util.Log
import com.example.tracktogether.Interfaces.INotification
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This repo calls firestore for retrieving device token in device db
 * Take in user id, approval status and notification interface
 * Author: Cheng Hao
 * Updated: 11 Mar 2022
 */
class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * get device token tagged with employee uid
     * @param uid employee unique id
     * @param approval true = approved, false = rejected
     * @param INotification notification listener for success callbacks
     */
    fun getDeviceToken(uid: String, approval: Boolean, INotification: INotification) {
        val docRef = db.collection("device")
        docRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        INotification.onApproval(document["deviceId"].toString(), approval)
                    }
                }
            }.addOnFailureListener {
                Log.d("main", "No document found ")
            }
    }
}