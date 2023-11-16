package com.example.tracktogether.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracktogether.adminviews.AddOfficeActivity
import com.example.tracktogether.adminviews.ApprovedRemoteCheckinActivity
import com.example.tracktogether.adminviews.EmpListActivity
import com.example.tracktogether.authviews.RegisterEmployeeActivity
import com.example.tracktogether.databinding.AdminMainFragmentBinding

/**
 * Admin main fragment
 * Author: Reuben
 * Updated: 9 March 2022
 */
class AdminMain : Fragment() {

    private lateinit var binding: AdminMainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AdminMainFragmentBinding.inflate(inflater, container, false)
        // animations
        setComponentsTransparent()
        setComponentsYAxis()
        loadPageAnimation()


        binding.approveCheckInCardView.setOnClickListener {
            val intent: Intent = Intent(activity, ApprovedRemoteCheckinActivity::class.java)
            startActivity(intent)
        }


        // listeners
        binding.trackAttendanceCardView.setOnClickListener {
            val intent: Intent = Intent(activity, EmpListActivity::class.java)
            startActivity(intent)
        }

        binding.resgisterCardView.setOnClickListener {
            val intent: Intent = Intent(activity, RegisterEmployeeActivity::class.java)
            startActivity(intent)
        }

        binding.editOfficeLocationCardView.setOnClickListener {
            val intent: Intent = Intent(activity, AddOfficeActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    /**
     * Load components with separate duration
     */
    private fun loadPageAnimation() {
        binding.approveCheckInTitle.animate().alpha(1f).translationYBy(-50F).duration = 500
        binding.approveCheckInCardView.animate().alpha(1f).translationYBy(-50F).duration = 500
        binding.editOfficeTextView.animate().alpha(1f).translationYBy(-50F).duration = 1000
        binding.editOfficeLocationCardView.animate().alpha(1f).translationYBy(-50F).duration = 1000
        binding.resgisterCardView.animate().alpha(1f).translationYBy(-50F).duration = 1500
        binding.resgisterTextView.animate().alpha(1f).translationYBy(-50F).duration = 1500
        binding.trackAttendanceCardView.animate().alpha(1f).translationYBy(-50F).duration = 2000
        binding.trackAttendanceTextView.animate().alpha(1f).translationYBy(-50F).duration = 2000

    }

    /**
     * Set the edit text field etc as transparent first, for animation
     */
    private fun setComponentsTransparent() {
        binding.approveCheckInTitle.alpha = 0f
        binding.approveCheckInCardView.alpha = 0f
        binding.editOfficeLocationCardView.alpha = 0f
        binding.resgisterCardView.alpha = 0f
        binding.trackAttendanceCardView.alpha = 0f
        binding.trackAttendanceTextView.alpha = 0f
        binding.resgisterTextView.alpha = 0f
        binding.editOfficeTextView.alpha = 0f
    }

    /**
     * Set lower y axis to simulate moving
     */
    private fun setComponentsYAxis() {
        binding.approveCheckInTitle.translationY = 50f
        binding.approveCheckInCardView.translationY = 50f
        binding.editOfficeLocationCardView.translationY = 50f
        binding.resgisterCardView.translationY = 50f
        binding.trackAttendanceCardView.translationY = 50f
        binding.trackAttendanceTextView.translationY = 50f
        binding.resgisterTextView.translationY = 50f
        binding.editOfficeTextView.translationY = 50f
    }

}