package com.example.tracktogether.authviews

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.tracktogether.BuildConfig
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.data.Employee
import com.example.tracktogether.databinding.ProfileEditFragmentBinding
import com.example.tracktogether.face.TakeFrontCameraPreview
import com.example.tracktogether.fragments.EmployeeMain
import com.example.tracktogether.viewmodel.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * When users are created we do not have their basic details so
 * they will use this activity to update their personal information
 * Author: May Madi Aung and Ong Ze Quan (Take Photo)
 * Updated: 12 March 2022
 */

class ProfileEdit : Fragment(), IEmployee, ISuccessFlag {

    private lateinit var binding: ProfileEditFragmentBinding

    private val TAG = "ProfileEdit"
    var employeeDetails = Employee()
    private lateinit var localImage: String


    // Init a view model instance using the factory class
    private val employeeViewModel: EmployeeViewModel by viewModels {
        EmployeeListViewModelFactory((activity!!.application as TrackTogetherApp).employeeListRepo,
            (activity!!.application as TrackTogetherApp).imageRepository,
            (activity!!.application as TrackTogetherApp).userPreferencesRepository,
            (activity!!.application as TrackTogetherApp).authrepo,
        )
    }


    //arrays to display for spinner dropdown
    val gender_array = arrayOf("Male", "Female")
    var genderSelected = "Male"

    //arrays to display for spinner dropdown
    val department_array = arrayOf(
        "Human Resource Management",
        "Production",
        "Research and Development",
        "Purchasing",
        "Marketing",
        "Accounting and Finance"
    )
    var departmentSelected = "Production"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProfileEditFragmentBinding.inflate(inflater, container, false)

        setUpSpinner()
        setUpDeptSpinner()

        employeeViewModel.getEmployee(employeeViewModel.currentUserID())
        employeeViewModel.iEmployee = this

        /**
         * saveButton create an employee object with respective fields and update employee document with new inputs
         */
        binding.savebutton.setOnClickListener {
            //create a entity to add to db
            val employee = Employee(
                uid = employeeViewModel.currentUserID(),
                firstName = binding.firstnameeditText.text.toString(),
                lastName = binding.lastnameeditText.text.toString(),
                phone = binding.phoneeditText.text.toString(),
                designation = binding.designationeditText.text.toString(),
                gender = genderSelected,
                dob = binding.dobeditText.text.toString(),
                department = departmentSelected
            )

            employeeViewModel.setEmployee(employee)
            employeeViewModel.iSuccessFlag = this
        }


        //Calendar
        binding.dobeditText.setOnClickListener {
            setUpCalender()
        }

        /**
         * When user take photo get imageUri to save to FireStore (status is set to In-review upon upload) and Update profile Picture
         */
        val takePhoto =
            registerForActivityResult(TakeFrontCameraPreview()) { (isSuccess, imageUri) ->
                val employee = Employee(
                    ApprovedRemoteCheckin = "In-review",
                    email = employeeViewModel.currentUserEmail(),
                    uid = employeeViewModel.currentUserID()
                )

                if (isSuccess) {
                    savePhotoToInternalStorage(UUID.randomUUID().toString(), imageUri)
                    employeeViewModel.uploadImageToFirebase(imageUri, employee)
                    val email = employeeViewModel.currentUserEmail()
                    employeeViewModel.uploadImageToFirebase(imageUri, employee)
                    employeeViewModel.checkinI = this
                    //change image to the photo that was taken
                    binding.viewprofileimageView.setImageURI(imageUri)
                    Toast.makeText(
                        activity!!.applicationContext,
                        "Photo saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        activity!!.applicationContext,
                        "Failed to save photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        /**
         * Opens front camera for employee to take photo
         */
        binding.addimageButton.setOnClickListener {
            if (allPermissionsGranted()) {
                lifecycleScope.launchWhenStarted {
                    getTmpFileUri().let { uri -> takePhoto.launch(uri) }
                }
            } else {
                Toast.makeText(
                    activity!!.applicationContext,
                    "Permissions denied. Please allow app to use camera",
                    Toast.LENGTH_SHORT
                ).show()
                ActivityCompat.requestPermissions(
                    activity!!,
                    EmployeeMain.REQUIRED_PERMISSIONS,
                    EmployeeMain.REQUEST_CODE_PERMISSIONS
                )
            }
        }


        return binding.root
    }


    /**
     * pop-up calender when user click on date fill
     */
    private fun setUpCalender() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = this.context?.let {
            DatePickerDialog(
                it,
                DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                    val monthCorrection = mMonth + 1
                    binding.dobeditText.setText("$mDay/$monthCorrection/$mYear")
                },
                year,
                month,
                day
            )
        }

        //show dialog
        dpd?.show()

    }

    /**
     * spinner with male and femalesetUpDeptSpinner()
     */
    private fun setUpSpinner() {
        val genderAdapter =
            ArrayAdapter(activity!!.applicationContext, R.layout.spinner_list, gender_array)
        genderAdapter.setDropDownViewResource(R.layout.spinner_list)
        binding.genderSpinner.adapter = genderAdapter
        binding.genderSpinner.setSelection(0)


        binding.genderSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                genderSelected = gender_array[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun setUpDeptSpinner() {
        val deptAdapter =
            ArrayAdapter(activity!!.applicationContext, R.layout.spinner_list, department_array)
        deptAdapter.setDropDownViewResource(R.layout.spinner_list)
        binding.departmentSpinner.adapter = deptAdapter
        binding.departmentSpinner.setSelection(1)


        binding.departmentSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                departmentSelected = department_array[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    /**
     * From IEmployee
     * employeeViewModel.getEmployeeDetailsployee(authViewModel.currentUserID())
     * Fetch an employee object from Firestore based on document key (UID)
     * getDetails() set the fetch details for display
     */
    override fun onSuccessEmpObj(emp: Employee) {
        employeeDetails = emp
        getDetails()
    }

    /**
     * set editText fields with details from Employee collection
     * If local photo do not exist set placeholder profilepicholder
     */
    private fun getDetails() {
        Log.e(TAG, "HERE " + employeeDetails.email.toString())

        //set employee
        binding.firstnameeditText.setText(employeeDetails.firstName)
        binding.lastnameeditText.setText(employeeDetails.lastName)
        binding.dobeditText.setText(employeeDetails.dob)
        binding.designationeditText.setText(employeeDetails.designation)
        binding.phoneeditText.setText(employeeDetails.phone)
        if (employeeDetails.gender == "Female") {
            binding.genderSpinner.setSelection(1)
        } else {
            binding.genderSpinner.setSelection(0)
        }
        when (employeeDetails.department) {
            "Human Resource Management" -> binding.departmentSpinner.setSelection(0)
            "Production" -> binding.departmentSpinner.setSelection(1)
            "Research and Development" -> binding.departmentSpinner.setSelection(2)
            "Purchasing" -> binding.departmentSpinner.setSelection(3)
            "Marketing" -> binding.departmentSpinner.setSelection(4)
            "Accounting and Finance" -> binding.departmentSpinner.setSelection(4)
            else -> binding.departmentSpinner.setSelection(0)

        }

        // Save Bitmap to internal storage
        val pathname = "faces/user"
        val dir: File = File(activity!!.applicationContext.filesDir.toString(), pathname)
        if (dir.exists()) {
            val checkinimage = File(dir, "checkinimage.jpg")
            if (checkinimage.exists()) {
                binding.viewprofileimageView.setImageURI(Uri.parse(checkinimage.toString()))
                Log.e(TAG, "This is the file path to local photo: $checkinimage")
            } else {
                binding.viewprofileimageView.setImageResource(R.drawable.profilepicholder)
            }
        } else {
            binding.viewprofileimageView.setImageResource(R.drawable.profilepicholder)
        }
    }


    /**
     * From ISuccessFlag
     * employeeViewModel.setEmployee(employee)
     * After user press saveButton return toast upon sucessful update to Document FB
     */
    override fun onSuccess(flag: Boolean) {
        if (flag) {
            Log.e(TAG, "Success!")

            Toast.makeText(
                activity!!.applicationContext,
                "Saved Successfully!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * save Photo to this path: /data/user/0/com.example.tracktogether/files/faces/user/checkinimage.jpg
     * upon photo taken successfully
     */
    private fun savePhotoToInternalStorage(filename: String, uri: Uri): Boolean {
        return try {
            // Get Bitmap from URI
            var bitmap: Bitmap? = null
            val contentResolver = activity!!.applicationContext.contentResolver
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            // Save Bitmap to internal storage
            val pathname = getString(R.string.face_path)
            var test: File = File(activity!!.applicationContext.filesDir.toString())
            val dir: File = File(activity!!.applicationContext.filesDir.toString(), pathname)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val outputStream = FileOutputStream(File(dir, getString(R.string.check_in_image)), false)
            outputStream.use { stream ->
                if (bitmap != null) {
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                        throw IOException("Couldn't save bitmap.")
                    }
                }
            }
            outputStream.close()
            for (file in dir.listFiles()) {
                Log.e("SavePhoto", file.toString())
                localImage = file.toString()
            }
            for (file in test.listFiles()) {
                Log.e("test", file.toString())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * TODO add comments
     */
    private fun allPermissionsGranted() = EmployeeMain.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            activity!!.applicationContext!!, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * TODO add comments
     */
    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".png", activity!!.applicationContext!!.cacheDir)
                .apply {
                    createNewFile()
                    deleteOnExit()
                }

        return FileProvider.getUriForFile(
            activity!!.applicationContext!!,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }
}