package com.example.tracktogether.checkinviews

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.databinding.ActivityRecognitionBinding
import com.example.tracktogether.face.FileReader
import com.example.tracktogether.face.FrameAnalyzer
import com.example.tracktogether.face.model.FaceNetModel
import com.example.tracktogether.face.model.Models
import com.example.tracktogether.viewmodel.RecognitionViewModel
import com.example.tracktogether.viewmodel.RecognitionViewModelFactory
import com.google.common.util.concurrent.ListenableFuture
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activity for Facial Recognition features
 * Author: Ong Ze Quan
 * Updated: 5 Mar 2022
 */
class RecognitionActivity : AppCompatActivity() {
    // Serialized data will be stored ( in app's private storage ) with this filename.
    private val serializedImageFileName = "image_data"
    private lateinit var viewBinding: ActivityRecognitionBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var frameAnalyzer: FrameAnalyzer
    private lateinit var faceNetModel: FaceNetModel
    private lateinit var fileReader: FileReader
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val recognitionViewModel: RecognitionViewModel by viewModels {
        RecognitionViewModelFactory(
            (application as TrackTogetherApp).userPreferencesRepository,
            (application as TrackTogetherApp).authrepo,
            (application as TrackTogetherApp).attendanceRepository
        )
    }


    // <----------------------- User controls --------------------------->

    // Use the device's GPU to perform faster computations.
    // Refer https://www.tensorflow.org/lite/performance/gpu
    private val useGpu = true

    // Use XNNPack to accelerate inference.
    // Refer https://blog.tensorflow.org/2020/07/accelerating-tensorflow-lite-xnnpack-integration.html
    private val useXNNPack = true

    // You may the change the models here.
    // Use the model configs in Models.kt
    // Default is Models.FACENET ; Quantized models are faster
    private val modelInfo = Models.FACENET_512

    // <---------------------------------------------------------------->


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRecognitionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        weakActivity = WeakReference(this)

        observeData()
        previewView = viewBinding.previewView
        setUpRecognitionEvent()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        setUpFacialRecognition()
    }

    /***
     * Set up facial recognition features, bounding boxes on face
     */
    private fun setUpFacialRecognition() {
        val boundingBoxOverlay = viewBinding.bboxOverlay

        // Overlay on face
        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)
        // Initialize model and frame analyzer
        faceNetModel = FaceNetModel(this, modelInfo, useGpu, useXNNPack)
        frameAnalyzer = FrameAnalyzer(this, boundingBoxOverlay, faceNetModel)
        fileReader = FileReader(faceNetModel)

        cameraExecutor = Executors.newSingleThreadExecutor()
        recognitionViewModel.loadPhotosFromInternalStorage(filesDir, contentResolver)
    }

    /***
     * Setup event collection
     */
    private fun setUpRecognitionEvent() {
        lifecycleScope.launchWhenStarted {
            recognitionViewModel.eventFlow.collect { event ->
                when (event) {
                    is RecognitionViewModel.RecognitionEvent.UploadEvent -> {
                        val imageUri = event.response.imageUri
                        val downloadUri = event.response.downloadUri
                        Log.d(TAG, "Image uploaded to $downloadUri, image Uri is $imageUri")
                        recognitionViewModel.addAttendanceToFirebase(
                            downloadUri,
                            applicationContext
                        )
                        val intent =
                            Intent(this@RecognitionActivity, RecognitionSuccessActivity::class.java)
                        intent.putExtra("imageUri", imageUri.toString())
                        startActivity(intent)
                        finish()
                    }
                    is RecognitionViewModel.RecognitionEvent.LocalStorageReadEvent -> {
                        fileReader.run(event.image, fileReaderCallback)
                    }
                }
            }
        }
    }

    /***
     * Watch serialized data state and load it into frame analyzer if it is true
     */
    private fun observeData() {
        recognitionViewModel.userPreferenceFlow.observe(this) { isStored ->
            if (isStored.serializedImageSaved) {
                frameAnalyzer.faceList =
                    recognitionViewModel.loadSerializedImageData(filesDir, serializedImageFileName)
            }
        }
    }

    /***
     * Take photo and save image to local storage
     */
    fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TrackTogether/CheckIn")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val localImgUri = output.savedUri
                    val msg = "Photo capture succeeded: $localImgUri"
                    Log.d(TAG, msg)
                    // upload in background
                    uploadImage(output.savedUri)
                    showInProgress()
                }
            }
        )
    }

    /**
     * Upload the saved image to firebase
     * @param uri : File location
     */
    private fun uploadImage(uri: Uri?) {
        recognitionViewModel.uploadImageToFirebase(uri)
    }


    /***
     * Start camera of device
     */
    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(this)
        )
        imageCapture = ImageCapture.Builder().build()
    }

    /***
     * Select front camera and set up frame analysis
     */
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        // Preview
        val preview: Preview = Preview.Builder().build()
            .also {
                it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
            }
        // Select Front Camera Only
        val cameraSelector: CameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        // Set up Frame Analysis
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyzer)
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageFrameAnalysis
        )
    }

    /***
     * Check if all permission required is given by user.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /***
     * Ask user for permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /***
     * File Reader callback, when files are loaded, call saveSerializedImageData to save it to private storage
     */
    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(
            data: ArrayList<Pair<String, FloatArray>>,
            numImagesWithNoFaces: Int
        ) {
            frameAnalyzer.faceList = data
            recognitionViewModel.saveSerializedImageData(filesDir, serializedImageFileName, data)
            Log.e(TAG, "Images parsed. Found $numImagesWithNoFaces images with no faces.")
        }
    }

    /***
     *
     */
    private fun showInProgress() {
        viewBinding.progressOverlay.visibility = View.VISIBLE
        viewBinding.uploadTextView.visibility = View.VISIBLE
        viewBinding.progressIndicator.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        lateinit var weakActivity: WeakReference<RecognitionActivity>
        fun getCameraInstanceActivity(): RecognitionActivity? {
            return weakActivity.get()
        }

        private val TAG = RecognitionActivity::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}