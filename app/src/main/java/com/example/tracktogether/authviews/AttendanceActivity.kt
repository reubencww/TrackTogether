package com.example.tracktogether.authviews

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.Interfaces.IAttendanceList
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.adapter.AttendanceListAdapter
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.databinding.ActivityAttendanceBinding
import com.example.tracktogether.viewmodel.TrackAttendanceViewModel
import com.example.tracktogether.viewmodel.TrackAttendanceViewModelFactory

/**
 * Activity for all employee to track personal past attendance records
 * Author: Cheng Hao
 * Last updated: 13 Mar 2022
 */

class AttendanceActivity : AppCompatActivity(), IAttendanceList {
    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var dateSpinner: Spinner
    private lateinit var attendanceListAdapter: AttendanceListAdapter
    private var attendanceList = arrayListOf<Attendance>()

    // Init a view model instance using the factory class
    private val trackAttendanceViewModel: TrackAttendanceViewModel by viewModels {
        TrackAttendanceViewModelFactory((application as TrackTogetherApp).attendanceRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        trackAttendanceViewModel.IAttendanceList = this

        setContentView(binding.root)
        initialiseSpinner()
        binding.dailyAttendanceRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0)
                    trackAttendanceViewModel.numberOfDaysSelected(0)
                else if (position == 1)
                    trackAttendanceViewModel.numberOfDaysSelected(6)
                else if (position == 2)
                    trackAttendanceViewModel.numberOfDaysSelected(30)
                else if (position == 3)
                    trackAttendanceViewModel.numberOfDaysSelected(92)
            }
        }
    }

    /**
     * Setting up spinner
     */
    private fun initialiseSpinner() {
        dateSpinner = binding.historySpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.history_array,
            R.layout.color_spinner
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            // Apply the adapter to the spinner
            dateSpinner.adapter = adapter
        }
    }

    /**
     * On successful retrieval of past records from db, udpate recycler view
     */
    override fun onSuccessAttendanceList(attendanceList: List<Attendance>) {
        this.attendanceList = ArrayList(attendanceList)
        attendanceListAdapter = AttendanceListAdapter(this.attendanceList)
        binding.dailyAttendanceRecyclerView.adapter = attendanceListAdapter
    }
}