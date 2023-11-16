package com.example.tracktogether.adminviews

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.tracktogether.Interfaces.IAttendanceList
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.adapter.AttendanceTabAdapter
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.databinding.ActivityTrackattendanceBinding
import com.example.tracktogether.fragments.OfficeFragment
import com.example.tracktogether.fragments.RemoteFragment
import com.example.tracktogether.viewmodel.TrackAttendanceViewModel
import com.example.tracktogether.viewmodel.TrackAttendanceViewModelFactory
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import java.util.*

/**
 * Activity for HR/Admin to track selected employee past history records
 * Author: Cheng Hao
 * Last updated: 13 Mar 2022
 */

class TrackEmpListActivity : AppCompatActivity(), IAttendanceList {
    private lateinit var binding: ActivityTrackattendanceBinding
    private lateinit var fromDate: Timestamp
    private lateinit var toDate: Timestamp
    private lateinit var officeFragment: OfficeFragment
    private lateinit var remoteFragment: RemoteFragment

    // Init a view model instance using the factory class
    private val trackAttendanceViewModel: TrackAttendanceViewModel by viewModels {
        TrackAttendanceViewModelFactory((application as TrackTogetherApp).attendanceRepository)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackattendanceBinding.inflate(layoutInflater)
        trackAttendanceViewModel.IAttendanceList = this

        setContentView(binding.root)
        initialiseFragment(savedInstanceState)
        setUpTabs()

        val extras = intent.extras
        val fName = extras?.getString("F_NAME")
        val lName = extras?.getString("L_NAME")
        val email = extras?.getString("EMAIL")
        val employeeuid = extras?.getString("ID")
        val designation = extras?.getString("DESIGNATION")
        val imageUrl = extras?.getString("URL_IMG")

        if (imageUrl == null) {
            binding.imageView.setImageResource(R.drawable.profilepicholder)
        } else {
            Picasso.get().load(imageUrl).placeholder(R.drawable.loading).rotate(270F).into(binding.imageView)
        }

        binding.usernameTextView.text = "Name: $fName $lName"
        binding.staffIdTextView.text = "Staff Id: $email"
        binding.departmentTextView.text = designation

        binding.fromDateImageView.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this, { view, mYear, mMonth, mDay ->
                val monthCorrection = mMonth + 1
                binding.fromEditText.setText("$mDay/$monthCorrection/$mYear")
            }, year, month, day)
            //show dialog
            dpd.show()
        }

        binding.toDateImageView.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this, { view, mYear, mMonth, mDay ->
                val monthCorrection = mMonth + 1
                binding.toEditText.setText("$mDay/$monthCorrection/$mYear")
            }, year, month, day)
            //show dialog
            dpd.show()
        }

        binding.trackButton.setOnClickListener {
            trackAttendanceViewModel.validateTextfield(
                binding.fromEditText.text.toString(),
                binding.toEditText.text.toString()
            )
        }

        // observe live data from View Model
        trackAttendanceViewModel.fromEpoch.observe(this) {
            fromDate = it
        }

        // observe live data from View Model
        trackAttendanceViewModel.toEpoch.observe(this) {
            toDate = it
            if (employeeuid != null) {
                trackAttendanceViewModel.getCheckInDetails(employeeuid, fromDate, toDate)
            }
        }

        // observe live data from View Model
        trackAttendanceViewModel.fromError.observe(this) {
            binding.fromEditText.error = it
        }

        // observe live data from View Model
        trackAttendanceViewModel.toError.observe(this) {
            binding.toEditText.error = it
        }

        // observe live data from View Model
        trackAttendanceViewModel.validateResult.observe(this) {
            if (it) {
                if (binding.fromEditText.text.toString() != "")
                    trackAttendanceViewModel.convertStringToTimestamp(
                        binding.fromEditText.text.toString(),
                        binding.toEditText.text.toString()
                    )
            }
        }

        /**
         * update fragment view on live data change
         */
        trackAttendanceViewModel.officeAttendanceDetail.observe(this) {
            val officeAttendanceList = ArrayList(it)
            officeFragment.refreshList(officeAttendanceList)
        }

        /**
         * update fragment view on live data change
         */
        trackAttendanceViewModel.remoteAttendanceDetail.observe(this) {
            val remoteAttendanceList = ArrayList(it)
            remoteFragment.refreshList(remoteAttendanceList)
        }
    }

    /**
     * store created office/remote fragments
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString("office_key", officeFragment.tag)
        savedInstanceState.putString("remote_key", remoteFragment.tag)
        super.onSaveInstanceState(savedInstanceState)
    }


    /**
     * Split result list by remote and office
     */
    override fun onSuccessAttendanceList(attendanceList: List<Attendance>) {
        trackAttendanceViewModel.splitListByRemoteOffice(attendanceList)
    }

    /**
     * if activity runs for first time initialise new fragment
     * else look for saved fragment
     */
    private fun initialiseFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val fm: FragmentManager = supportFragmentManager
            val officeTag = savedInstanceState.getString("office_key")
            officeFragment = fm.findFragmentByTag(officeTag) as OfficeFragment

            val remoteTag = savedInstanceState.getString("remote_key")
            remoteFragment = fm.findFragmentByTag(remoteTag) as RemoteFragment
        } else {
            officeFragment = OfficeFragment()
            remoteFragment = RemoteFragment()
        }
    }

    /**
     * Set up tabs for both office and remote tab
     */
    private fun setUpTabs() {
        val adapter = AttendanceTabAdapter(supportFragmentManager)
        adapter.addFragment(officeFragment, "Office")
        adapter.addFragment(remoteFragment, "Remote")
        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        binding.tabs.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_corporate_fare_orange_24)
        binding.tabs.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_location_on_orange_24)
    }
}