package com.example.tracktogether.adminviews

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.Interfaces.IEmpList
import com.example.tracktogether.R
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.adapter.EmployeeListAdapter
import com.example.tracktogether.data.Employee
import com.example.tracktogether.databinding.ActivityEmployeelistBinding
import com.example.tracktogether.viewmodel.EmployeeListViewModelFactory
import com.example.tracktogether.viewmodel.EmployeeViewModel

/**
 *  Fetch all rows from FireStore employee table using employeeListViewModel
 * Author:  May, Cheng Hao
 * Updated: 10 Mar 2022
 */
// set up binding
class EmpListActivity : AppCompatActivity(), IEmpList {
    private lateinit var binding: ActivityEmployeelistBinding
    private var empList = arrayListOf<Employee>()
    private lateinit var departmentSpinner: Spinner
    private lateinit var empListAdapter: EmployeeListAdapter

    val TAG = "EmployeeListActivity"


    // Init a view model instance using the factory class
    private val employeeViewModel: EmployeeViewModel by viewModels {
        EmployeeListViewModelFactory((application as TrackTogetherApp).employeeListRepo,
            (application as TrackTogetherApp).imageRepository,
            (application as TrackTogetherApp).userPreferencesRepository,
            (application as TrackTogetherApp).authrepo,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeelistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        employeeViewModel.iEmpList = this
        initialiseSpinner()
        binding.recyclerViewEmployeeList.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /**
             *Override Spinner AdapterView to get Department selected
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            /**
             * Display Employee Dynamically based on spinner item selected
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> employeeViewModel.getAllEmployee()
                    1 -> employeeViewModel.getAllEmployeeByDepartment("Human Resource Management")
                    2 -> employeeViewModel.getAllEmployeeByDepartment("Production")
                    3 -> employeeViewModel.getAllEmployeeByDepartment("Research and Development")
                    4 -> employeeViewModel.getAllEmployeeByDepartment("Purchasing")
                    5 -> employeeViewModel.getAllEmployeeByDepartment("Marketing")
                    6 -> employeeViewModel.getAllEmployeeByDepartment("Accounting and Finance")
                }
            }
        }
    }


    /**
     * Setting up spinner
     */
    private fun initialiseSpinner() {
        departmentSpinner = binding.historySpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.department_array,
            R.layout.color_spinner
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            // Apply the adapter to the spinner
            departmentSpinner.adapter = adapter
        }
    }

    /**
     * Update recyclerview with all employees retrieved
     * @param employeeList A list of employee from FireStore
     */
    override fun onSuccessEmpList(employeeList: List<Employee>) {
        empList = ArrayList(employeeList)

        this.empList = ArrayList(employeeList)
        empListAdapter = EmployeeListAdapter(this, empList)
        binding.recyclerViewEmployeeList.adapter = empListAdapter
    }


}