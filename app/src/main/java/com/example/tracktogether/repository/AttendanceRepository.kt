package com.example.tracktogether.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracktogether.Interfaces.IAttendanceList
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.data.AttendanceResponse
import com.example.tracktogether.data.ImageUploadResponse
import com.example.tracktogether.data.NFCResponse
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.*
import java.util.*

/**
 * Repository for attendance
 * Author: Ong Ze Quan
 * Updated: 5 Mar 2022
 */
class AttendanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference
    private val TAG = AttendanceRepository::class.simpleName
    private val _downloadUri = MutableLiveData<Uri>()
    val downloadUri: LiveData<Uri> = _downloadUri

    /**
     * Get location's tagged NFC ID
     * @param location String for office name
     * @return NFCResponse contains nfcId or exception
     */
    suspend fun getLocationNFC(location: String): NFCResponse {
        val response = NFCResponse()
        try {
            val data = db.collection("nfc").document(location).get().await().data
            response.nfcId = data?.get("nfcId") as String?
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }

    /**
     * Upload attendance Image to Firebase Storage
     * @param employeeUID UID of employee to use as file path
     * @param uri Uri of image stored in local storage
     */
    suspend fun uploadImage(employeeUID: String, uri: Uri): ImageUploadResponse {
        val response = ImageUploadResponse()
        response.imageUri = uri
        val ref = storageReference.child("attendance/${employeeUID}/${UUID.randomUUID()}")
        try {
            response.downloadUri = ref.putFile(uri)
                .await()
                .storage
                .downloadUrl
                .await()
        } catch (exception: Exception) {
            response.exception = exception
            Log.v(TAG, "An error occurred while uploading. $exception")
        }
        return response
    }

    fun getImageDetials() {

    }

    /**
     * Upload attendance Image to Firebase Storage
     * @param attendance Attendance object
     */
    suspend fun uploadAttendanceRecordToDB(attendance: Attendance) = withContext(Dispatchers.IO) {
        db.collection("attendance").add(attendance)
    }


    /**
     * Check if user has checked in today
     * @param employeeUID UID
     * @return Boolean
     */
    suspend fun checkAttendanceToday(employeeUID: String): Boolean {
        val start =
            Timestamp.now().toLocalDateTime().toLocalDate().atTime(LocalTime.MIN).toTimestamp()
        val end =
            Timestamp.now().toLocalDateTime().toLocalDate().atTime(LocalTime.MAX).toTimestamp()
        Log.e(TAG, "TIME $start, TIME $end")
        val documents = db.collection("attendance")
            .whereEqualTo("employeeUID", employeeUID)
            .whereGreaterThanOrEqualTo("inTime", start)
            .whereLessThan("inTime", end)
            .get().await().documents
        return documents.isEmpty()
    }

    /**
     * Get user attendance document for today
     * @param employeeUID UID
     * @return AttendanceResponse A mutable list of DocumentSnapshots
     */
    suspend fun getAttendanceDocToday(employeeUID: String): AttendanceResponse {
        val response = AttendanceResponse()
        val start =
            Timestamp.now().toLocalDateTime().toLocalDate().atTime(LocalTime.MIN).toTimestamp()
        val end =
            Timestamp.now().toLocalDateTime().toLocalDate().atTime(LocalTime.MAX).toTimestamp()
        Log.e(TAG, "START $start, END $end")
        try {
            val documents = db.collection("attendance")
                .whereEqualTo("employeeUID", employeeUID)
                .whereGreaterThanOrEqualTo("inTime", start)
                .whereLessThan("inTime", end)
                .get().await().documents
            response.document = documents
        } catch (exception: Exception) {
            Log.e(TAG, "Something went wrong when checking checkout status")
            response.exception = exception
        }
        return response
    }

    /**
     * Get a user attendance document between range
     * @param employeeUID: Employee UID
     * @param start Start Date
     * @param end End Date
     * @return AttendanceResponse A mutable list of DocumentSnapshots
     */
    suspend fun getAttendanceDocBetweenRange(
        employeeUID: String,
        start: LocalDate,
        end: LocalDate
    ): AttendanceResponse {
        val response = AttendanceResponse()
        Log.v(TAG, "THE TIME IS NOW START $start, END $end")
        try {
            val documents = db.collection("attendance")
                .whereEqualTo("employeeUID", employeeUID)
                .whereGreaterThanOrEqualTo("inTime", start)
                .whereLessThanOrEqualTo("outTime", end)
                .get().await().documents
            response.document = documents
            Log.v(TAG, "THE NUMBER OF DOCUMENTS IS ${documents.size}")
        } catch (exception: Exception) {
            Log.e(TAG, "Something went wrong when checking checkout status")
            response.exception = exception
        }
        return response
    }

    /**
     * retrieve records of employee daily attendance
     * @param employeeuid target employee's UID
     * @param from start date
     * @param to end date
     * @param IEmpList listener of type EmployeeListListener for callback result on success retrieval
     */
    fun getDateRangeAttendanceDetails(
        employeeuid: String,
        from: Timestamp,
        to: Timestamp,
        IAttendanceList: IAttendanceList
    ) {
        val docRef = db.collection("attendance")
        val attendanceList = mutableListOf<Attendance>()
        val beforeToNextDay = Timestamp(to.seconds + 86399, 0)
        docRef.whereEqualTo("employeeUID", employeeuid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val dateTime = document["inTime"]
                        if (dateTime is Timestamp) {
                            if (from <= dateTime && dateTime <= beforeToNextDay) {
                                attendanceList.add(dayAttendanceRecord(document))
                            }
                        }
                    }
                    IAttendanceList.onSuccessAttendanceList(attendanceList)
                }
            }
    }


    /**
     * Checkout user by updating outTime
     * @param documentId from getAttendanceDocToday
     */
    suspend fun updateOutTime(documentId: String) = withContext(Dispatchers.IO) {
        db.collection("attendance").document(documentId).update("outTime", Timestamp.now())
    }


    /***************************************
     * Admin functionalities below
     ***************************************/

    /**
     * Get all user attendance document for today
     * @return AttendanceResponse A mutable list of DocumentSnapshots
     */
    suspend fun getAttendanceDocToday(): AttendanceResponse {
        val response = AttendanceResponse()
        val start =
            Timestamp.now().toLocalDateTime().toLocalDate().atTime(LocalTime.MIN).toTimestamp()
        val end =
            Timestamp.now().toLocalDateTime().toLocalDate().atTime(LocalTime.MAX).toTimestamp()
        Log.v(TAG, "START $start, END $end")
        try {
            val documents = db.collection("attendance")
                .whereGreaterThanOrEqualTo("inTime", start)
                .whereLessThan("inTime", end)
                .get().await().documents
            response.document = documents
        } catch (exception: Exception) {
            Log.e(TAG, "Something went wrong when checking checkout status")
            response.exception = exception
        }
        return response
    }

    /**
     * Get all user attendance document between range
     * @param start Start Date
     * @param end End Date
     * @return AttendanceResponse A mutable list of DocumentSnapshots
     */
    suspend fun getAttendanceDocBetweenRange(start: LocalDate, end: LocalDate): AttendanceResponse {
        val response = AttendanceResponse()
        Log.v(TAG, "THE TIME IS NOW START $start, END $end")
        try {
            val documents = db.collection("attendance")
                .whereGreaterThanOrEqualTo("inTime", start)
                .whereLessThanOrEqualTo("inTime", end)
                .get().await().documents
            response.document = documents
            Log.v(TAG, "THE NUMBER OF DOCUMENTS IS ${documents.size}")
        } catch (exception: Exception) {
            Log.e(TAG, "Something went wrong when checking checkout status")
            response.exception = exception
        }
        return response
    }

    /**
     * retrieve records of employee daily attendance
     * @param employeeuid target employee's UID
     * @param from start date
     * @param to end date
     * @param IEmpList listener of type EmployeeListListener for callback result on success retrieval
     */
    fun getSelectedDateRangeAttendanceDetails(
        employeeuid: String,
        from: Timestamp,
        to: Timestamp,
        AttendanceList: IAttendanceList
    ) {
        val docRef = db.collection("attendance")
        val attendanceList = fillListWithDefaultValues(from, to)
        val beforeToNextDay = Timestamp(to.seconds + 86399, 0)
        docRef.whereEqualTo("employeeUID", employeeuid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val dateTime = document["inTime"]
                        if (dateTime is Timestamp) {
                            if (from <= dateTime && dateTime <= beforeToNextDay) {
                                attendanceList[((dateTime.seconds - from.seconds) / 86400).toInt()] =
                                    dayAttendanceRecord(document)
                            }
                        }
                    }
                    AttendanceList.onSuccessAttendanceList(attendanceList)
                }
            }
    }

    /**
     * Set attendance list with each day default values
     * @param from Start Date
     * @param to End Date
     * @return A mutable list of Attendance
     */
    private fun fillListWithDefaultValues(from: Timestamp, to: Timestamp): MutableList<Attendance> {
        val attendanceList = mutableListOf<Attendance>()
        val numberOfDays =
            ((to.seconds - from.seconds) / 86400) + 1 // 86400 = number of seconds in a day

        for (i in 1..numberOfDays.toInt()) {
            val checkInDefault = Attendance(
                date = Timestamp(from.seconds + (86400 * (i - 1)), 0),
                inTime = null,
                outTime = null,
                location = ""
            )
            attendanceList.add(checkInDefault)
        }
        return attendanceList
    }

    /**
     * Get Attendance object from QueryDocumentSnapshot
     * @param document Daily record in QueryDocumentSnapshot
     * @return Attendance object
     */
    private fun dayAttendanceRecord(document: QueryDocumentSnapshot): Attendance {
        return Attendance(
            date = document["inTime"] as Timestamp?,
            inTime = document["inTime"] as Timestamp?,
            outTime = document["outTime"] as Timestamp?,
            employeeUID = document["employeeUID"].toString(),
            imageUrl = document["imageUrl"].toString(),
            location = document["location"].toString(),
            remote = document["remote"] as Boolean?
        )
    }


    /***
     * Thank you Desmond Lua
     * https://code.luasoftware.com/tutorials/google-cloud-firestore/understanding-date-in-firestore/
     */

    /***
     * Convert LocalDateTime to timestamp
     */
    private fun LocalDateTime.toTimestamp() =
        Timestamp(atZone(ZoneId.systemDefault()).toEpochSecond(), nano)

    /***
     * Convert timestamp to Local Date
     */
    private fun Timestamp.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()) =
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(seconds * 1000 + nanoseconds / 1000000), zone
        )

}