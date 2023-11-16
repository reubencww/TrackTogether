package com.example.tracktogether.viewmodel

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.data.ImageUploadResponse
import com.example.tracktogether.face.BitmapUtils
import com.example.tracktogether.repository.AttendanceRepository
import com.example.tracktogether.repository.AuthenticationRepository
import com.example.tracktogether.repository.UserPreferencesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.*
import java.util.*

/**
 * View Model for Facial Recognition
 * Author: Ong Ze Quan
 * Updated: 5 Mar 2022
 */
class RecognitionViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthenticationRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    val userPreferenceFlow = userPreferencesRepository.userPreferencesFlow.asLiveData()
    val TAG = RecognitionViewModel::class.java.simpleName

    /***
     * Update state of serialized image
     * @param state True/False
     */
    private fun updateSerialImageState(state: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        userPreferencesRepository.updateSerialDataState(state)
    }

    /***
     * Create attendance in firebase
     * @param downloadUri Download Uri(firebase storage uri with token) after uploadImageToFirebase
     */
    fun addAttendanceToFirebase(downloadUri: Uri?, context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.currentUser()?.let {
                val location = fetchLocation(context)
                val attendance = Attendance(
                    employeeUID = it.uid,
                    inTime = Timestamp.now(),
                    remote = true,
                    imageUrl = downloadUri.toString(),
                    location = location
                )
                attendanceRepository.uploadAttendanceRecordToDB(attendance)
            }
        }

    /***
     * Upload image to firebase, takes in a file uri
     * @param uri Uri of image
     */
    fun uploadImageToFirebase(uri: Uri?) = viewModelScope.launch(Dispatchers.IO) {
        eventChannel.send(
            RecognitionEvent.UploadEvent(
                attendanceRepository.uploadImage(
                    authRepository.currentUser()?.uid!!,
                    uri!!
                )
            )
        )
    }

    /***
     * Load serialized image from private storage
     * Used by frame analyzer to find faces
     */
    @Suppress("UNCHECKED_CAST")
    fun loadSerializedImageData(
        filesDir: File,
        serializedImageFileName: String
    ): ArrayList<Pair<String, FloatArray>> {
        val serializedDataFile = File(filesDir, serializedImageFileName)
        val objectInputStream = ObjectInputStream(FileInputStream(serializedDataFile))
        val data = objectInputStream.readObject() as ArrayList<Pair<String, FloatArray>>
        objectInputStream.close()
        return data
    }

    /***
     * Save serialized image to private storage
     */
    fun saveSerializedImageData(
        filesDir: File,
        serializedImageFileName: String,
        data: ArrayList<Pair<String, FloatArray>>
    ) {
        val serializedDataFile = File(filesDir, serializedImageFileName)
        ObjectOutputStream(FileOutputStream(serializedDataFile)).apply {
            writeObject(data)
            flush()
            close()
        }
        updateSerialImageState(true)
    }

    /***
     * Retrieve Image as a Bitmap from a given Uri, using Exif to fix rotation
     * https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
     */
    private fun getFixedBitmap(contentResolver: ContentResolver, imageFileUri: Uri): Bitmap {
        var imageBitmap = BitmapUtils.getBitmapFromUri(contentResolver, imageFileUri)
        val exifInterface = ExifInterface(contentResolver.openInputStream(imageFileUri)!!)
        imageBitmap =
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> BitmapUtils.rotateBitmap(imageBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> BitmapUtils.rotateBitmap(imageBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> BitmapUtils.rotateBitmap(imageBitmap, 270f)
                else -> imageBitmap
            }
        return imageBitmap
    }

    /***
     * Load saved image from internal storage.
     * Image loaded will be used for facial recognition.
     */
    fun loadPhotosFromInternalStorage(filesDir: File, contentResolver: ContentResolver) =
        viewModelScope.launch(Dispatchers.IO) {
            val pathname = "faces"
            val dir = (File(filesDir.toString(), pathname))
            val images = ArrayList<Pair<String, Bitmap>>()
            var errorFound = false
            if (dir.listFiles().isNotEmpty()) {
                for (doc in dir.listFiles()) {
                    if (doc.isDirectory && !errorFound) {
                        val name = doc.name
                        for (imageDocFile in doc.listFiles()) {
                            try {
                                images.add(
                                    Pair(
                                        name,
                                        getFixedBitmap(contentResolver, Uri.fromFile(imageDocFile))
                                    )
                                )
                            } catch (e: Exception) {
                                errorFound = true
                                Log.e(
                                    TAG,
                                    "Could not parse an image in $name directory."
                                )
                                break
                            }
                        }
                        Log.v(TAG, "Found ${doc.listFiles().size} images in $name directory")
                    } else {
                        errorFound = true
                        Log.e(
                            TAG,
                            "The selected folder should contain only directories."
                        )
                    }
                }
            } else {
                errorFound = true
                Log.e(
                    TAG,
                    "The selected folder doesn't contain any directories."
                )
            }
            if (!errorFound) {
                eventChannel.send(RecognitionEvent.LocalStorageReadEvent(images))
                Log.v(TAG, "Detecting faces in ${images.size} images ...")
            }
        }

    /**
     * fetch lat and long from gps
     */
    private suspend fun fetchLocation(context: Context): String {
        var fusedLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
        val task: Task<Location> = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        val job = task.addOnSuccessListener {
        }
        job.await()
        return getLocationName(context, job.result.latitude, job.result.longitude)
    }

    /**
     * get location address base on lat and long
     */
    private fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
        val addresses: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())

        addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val address: String = addresses[0].getAddressLine(0)
        return address
    }

    /***
     * Channels for handling one time events
     */
    sealed class RecognitionEvent {
        data class UploadEvent(val response: ImageUploadResponse) : RecognitionEvent()
        data class LocalStorageReadEvent(val image: ArrayList<Pair<String, Bitmap>>) :
            RecognitionEvent()
    }

    private val eventChannel = Channel<RecognitionEvent>()

    val eventFlow = eventChannel.receiveAsFlow()

}


class RecognitionViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthenticationRepository,
    private val attendanceRepository: AttendanceRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecognitionViewModel::class.java)) {
            return RecognitionViewModel(
                userPreferencesRepository,
                authRepository,
                attendanceRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




