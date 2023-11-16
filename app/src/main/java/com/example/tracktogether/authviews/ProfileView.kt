package com.example.tracktogether.authviews

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tracktogether.Interfaces.IEmployee
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.data.Employee
import com.example.tracktogether.databinding.ProfileFragmentBinding
import com.example.tracktogether.viewmodel.EmployeeListViewModelFactory
import com.example.tracktogether.viewmodel.EmployeeViewModel
import java.io.File

class ProfileView : Fragment(), IEmployee {
    private lateinit var binding: ProfileFragmentBinding
    private val TAG = "ProfileView"

    var employeeDetails = Employee()


    // Init a view model instance using the factory class
    private val employeeViewModel: EmployeeViewModel by viewModels {
        EmployeeListViewModelFactory((activity!!.application as TrackTogetherApp).employeeListRepo,
            (activity!!.application as TrackTogetherApp).imageRepository,
            (activity!!.application as TrackTogetherApp).userPreferencesRepository,
            (activity!!.application as TrackTogetherApp).authrepo,
        )
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(inflater, container, false)

        binding.savebutton.setOnClickListener {
            displayEditFragment()
        }


        employeeViewModel.getEmployee(employeeViewModel.currentUserID())
        employeeViewModel.iEmployee = this


        return binding.root
    }

    private fun displayEditFragment() {
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.editprofilefragment, ProfileEdit()).commit()

    }

    /**
     * From IEmployee
     * employeeViewModel.getEmployee(authViewModel.currentUserID())
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
        binding.fnameviewtextView.text = employeeDetails.firstName
        binding.lnameviewtextView.text = employeeDetails.lastName
        binding.dobviewtextView.text = employeeDetails.dob
        binding.departmentviewtextView.text = employeeDetails.department
        binding.designationviewtextView.text = employeeDetails.designation
        binding.phoneviewtextView.text = employeeDetails.phone
        binding.genderviewtextView.text = employeeDetails.gender

        var file = File("/data/user/0/com.example.tracktogether/files/faces/user/checkinimage.jpg")
        if (file.exists()) {
            binding.viewprofileimageView.setImageURI(Uri.parse(file.toString()))
        } else {
            binding.viewprofileimageView.setImageResource(R.drawable.profilepicholder)
        }

    }
}