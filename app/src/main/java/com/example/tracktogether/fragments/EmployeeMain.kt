package com.example.tracktogether.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.MainActivity
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.authviews.AttendanceActivity
import com.example.tracktogether.authviews.ProfileActivity
import com.example.tracktogether.authviews.SettingActivity
import com.example.tracktogether.checkinviews.MapsActivity
import com.example.tracktogether.checkinviews.RecognitionActivity
import com.example.tracktogether.data.Employee
import com.example.tracktogether.databinding.EmployeeMainFragmentBinding
import com.example.tracktogether.viewmodel.EmployeeMainViewModel
import com.example.tracktogether.viewmodel.EmployeeMainViewModelFactory
import java.io.File


/**
 * Employee fragment view
 * Author: Reuben
 * Updated: 9 March 2022
 */
class EmployeeMain : Fragment(), IEmployee {
    private lateinit var binding: EmployeeMainFragmentBinding


    // viewmodel instance
    private val employeeMainViewModel: EmployeeMainViewModel by activityViewModels {
        EmployeeMainViewModelFactory(
            (activity!!.application as TrackTogetherApp).userPreferencesRepository,
            (activity!!.application as TrackTogetherApp).authrepo,
            (activity!!.application as TrackTogetherApp).imageRepository,
            (activity!!.application as TrackTogetherApp).attendanceRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EmployeeMainFragmentBinding.inflate(inflater, container, false)

        employeeMainViewModel.getEmpRemoteCheckinStatus(employeeMainViewModel.currentUserID())
        employeeMainViewModel.IEmployee = this


        // animations
        setComponentsTransparent()
        setComponentsYAxis()
        loadPageAnimation()

        // check all permissions needed
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                activity!!,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        setUpEvent()


        // Click listeners
        binding.analyticsCardView.setOnClickListener {
            val intent = Intent(activity, AttendanceActivity::class.java)
            startActivity(intent)
        }

        binding.settingsCardView.setOnClickListener {
            val intent = Intent(activity, SettingActivity::class.java)
            startActivity(intent)
        }
        binding.profileCardView.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
        observeRemoteApproval()
        return binding.root
    }

    private fun setUpEvent() {
        lifecycleScope.launchWhenStarted {
            employeeMainViewModel.eventFlow.collect { event ->
                when (event) {
                    is EmployeeMainViewModel.EmployeeMainViewEvent.GetAttendanceEvent -> {
                        val documents = event.response.document
                        if (documents?.size == 0) {
                            // Not checked in yet
                            setUpCheckIn()
                        } else {
                            val document = documents!![0]
                            val outTime = document["outTime"]
                            if (outTime != null) {
                                // Checked out for the day
                                setUpDoneMessage()
                            } else {
                                // Did not check out for the day yet
                                setUpCheckOutButton(document.id)
                            }
                        }
                    }
                }
            }
        }
        employeeMainViewModel.checkAttendanceToday()
    }

    override fun onResume() {
        super.onResume()
        // Check attendance again
        employeeMainViewModel.checkAttendanceToday()
        //check status again?
        employeeMainViewModel.getEmpRemoteCheckinStatus(employeeMainViewModel.currentUserID())
        employeeMainViewModel.IEmployee = this
    }

    private fun setUpDoneMessage() {
        // Set other cards to invisible
        binding.checkOutCardView.visibility = View.INVISIBLE
        binding.checkOutTextView.visibility = View.INVISIBLE
        binding.remoteCheckInCardView.visibility = View.INVISIBLE
        binding.remoteCheckInTextview.visibility = View.INVISIBLE
        binding.nfcCheckInCardView.visibility = View.INVISIBLE
        binding.nfcCheckInTextView.visibility = View.INVISIBLE

        binding.doneCardTextView.visibility = View.VISIBLE
        binding.doneCardView.visibility = View.VISIBLE
        binding.textViewPendingMessage.text = ""
    }

    private fun setUpCheckIn() {
        binding.checkOutCardView.visibility = View.INVISIBLE
        binding.doneCardView.visibility = View.INVISIBLE
        binding.checkOutTextView.visibility = View.INVISIBLE
        binding.doneCardTextView.visibility = View.INVISIBLE
        setUpNFCCheckIn()
    }

    private fun observeRemoteApproval() {
        employeeMainViewModel.userPrefFlow.observe(this) { pref ->
            when (pref.remoteApproved) {
                getString(R.string.image_approved) -> if (checkLocalImageExists()) setUpRemoteCheckInButtons() else setUpAddImage(
                    getString(R.string.image_not_found_message)
                )
                getString(R.string.image_in_review) -> setUpPendingApproval()
                getString(R.string.image_waiting) -> setUpAddImage(getString(R.string.image_waiting_message))
                getString(R.string.image_rejected) -> setUpAddImage(getString(R.string.image_rejected_message))
            }
        }
    }

    private fun checkLocalImageExists(): Boolean {
        val pathname = getString(R.string.face_path) + "/" + getString(R.string.check_in_image)
        var file = File(activity!!.applicationContext.filesDir.toString(), pathname)
        return file.exists()
    }

    private fun setUpRemoteCheckInButtons() {
        binding.textViewPendingMessage.text = ""
        binding.remoteCheckInTextview.visibility = View.VISIBLE
        binding.remoteCheckInCardView.visibility = View.VISIBLE
        binding.remoteCheckInCardView.setOnClickListener {
            val intent = Intent(activity, RecognitionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpNFCCheckIn() {
        binding.nfcCheckInCardView.visibility = View.VISIBLE
        binding.nfcCheckInTextView.visibility = View.VISIBLE
        binding.nfcCheckInCardView.setOnClickListener {
            val intent = Intent(activity, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpAddImage(message: String) {
        // Set other cards to invisible
        binding.checkOutCardView.visibility = View.INVISIBLE
        binding.remoteCheckInCardView.visibility = View.INVISIBLE
        binding.remoteCheckInTextview.visibility = View.INVISIBLE
        binding.checkOutTextView.visibility = View.INVISIBLE

        binding.doneCardTextView.visibility = View.INVISIBLE
        binding.doneCardView.visibility = View.INVISIBLE

        binding.textViewprofileMessage.text = "Please add image here!"
        binding.textViewPendingMessage.text = message

    }


    private fun setUpPendingApproval() {
        // Set other cards to invisible
        binding.checkOutCardView.visibility = View.INVISIBLE
        binding.remoteCheckInCardView.visibility = View.INVISIBLE
        binding.checkOutTextView.visibility = View.INVISIBLE
        binding.remoteCheckInTextview.visibility = View.INVISIBLE

        binding.doneCardTextView.visibility = View.INVISIBLE
        binding.doneCardView.visibility = View.INVISIBLE

        binding.textViewPendingMessage.text = "Pending admin approval to check-in remotely!"

    }

    private fun setUpCheckOutButton(documentId: String) {
        //set other cards to invisible
        binding.remoteCheckInCardView.visibility = View.INVISIBLE
        binding.nfcCheckInCardView.visibility = View.INVISIBLE
        binding.remoteCheckInTextview.visibility = View.INVISIBLE
        binding.nfcCheckInTextView.visibility = View.INVISIBLE

        binding.textViewPendingMessage.text = ""

        binding.checkOutTextView.visibility = View.VISIBLE
        binding.checkOutCardView.visibility = View.VISIBLE

        binding.checkOutCardView.setOnClickListener {
            employeeMainViewModel.checkOut(documentId)
            alertSuccess()

            /* val intent = Intent(activity, CheckoutActivity::class.java)
             startActivity(intent)*/
        }
    }


    private fun alertSuccess() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Success!")
        builder.setMessage("Successfully checked out,  have a good rest \uD83D\uDECF!")
        builder.setCancelable(false)
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
            val intent = Intent(context!!, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        })
        val alert = builder.create()
        alert.setTitle("Success!")
        alert.show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            activity!!.applicationContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "Employee Main"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


    /**
     * Load components with separate duration
     */
    private fun loadPageAnimation() {
        binding.firstFeatureTextView.animate().alpha(1f).translationYBy(-50F).duration = 500
        binding.analyticsCardView.animate().alpha(1f).translationYBy(-50F).duration = 500
        binding.settingsFeatureTextView.animate().alpha(1f).translationYBy(-50F).duration =
            1000
        binding.settingsCardView.animate().alpha(1f).translationYBy(-50F).duration = 1000
        binding.profileTextView.animate().alpha(1f).translationYBy(-50F).duration = 1500
        binding.profileCardView.animate().alpha(1f).translationYBy(-50F).duration = 1500
        binding.nfcCheckInCardView.animate().alpha(1f).translationYBy(-50F).duration = 2000
        binding.nfcCheckInTextView.animate().alpha(1f).translationYBy(-50F).duration = 2000

        // done/ checkout cards
        binding.doneCardTextView.animate().alpha(1f).translationYBy(-50F).duration = 2000
        binding.doneCardView.animate().alpha(1f).translationYBy(-50F).duration = 2000
        binding.checkOutTextView.animate().alpha(1f).translationYBy(-50F).duration = 2000
        binding.checkOutCardView.animate().alpha(1f).translationYBy(-50F).duration = 2000

        binding.remoteCheckInCardView.animate().alpha(1f).translationYBy(-50F).duration =
            2000
        binding.remoteCheckInTextview.animate().alpha(1f).translationYBy(-50F).duration =
            2000

    }

    /**
     * Set the edit text field etc as transparent first, for animation
     */
    private fun setComponentsTransparent() {
        binding.firstFeatureTextView.alpha = 0f
        binding.analyticsCardView.alpha = 0f
        binding.settingsFeatureTextView.alpha = 0f
        binding.settingsCardView.alpha = 0f
        binding.profileTextView.alpha = 0f
        binding.profileCardView.alpha = 0f
        binding.remoteCheckInCardView.alpha = 0f
        binding.remoteCheckInTextview.alpha = 0f
        binding.nfcCheckInCardView.alpha = 0f
        binding.nfcCheckInTextView.alpha = 0f
        binding.textViewPendingMessage.alpha = 0f
        binding.doneCardTextView.alpha = 0f
        binding.doneCardView.alpha = 0f
        binding.checkOutTextView.alpha = 0f
        binding.checkOutCardView.alpha = 0f
    }

    /**
     * Set lower y axis to simulate moving
     */
    private fun setComponentsYAxis() {
        binding.firstFeatureTextView.translationY = 50f
        binding.analyticsCardView.translationY = 50f
        binding.settingsFeatureTextView.translationY = 50f
        binding.settingsCardView.translationY = 50f
        binding.profileTextView.translationY = 50f
        binding.profileCardView.translationY = 50f
        binding.remoteCheckInCardView.translationY = 50f
        binding.nfcCheckInCardView.translationY = 50f
        binding.doneCardTextView.translationY = 50f
        binding.doneCardView.translationY = 50f
        binding.checkOutTextView.translationY = 50f
        binding.checkOutCardView.translationY = 50f
    }

    override fun onSuccessEmpObj(emp: Employee) {
        employeeMainViewModel.updateRemoteState(emp.ApprovedRemoteCheckin.toString())
    }
}