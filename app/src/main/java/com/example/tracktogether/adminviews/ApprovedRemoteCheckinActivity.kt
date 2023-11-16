package com.example.tracktogether.adminviews

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.Interfaces.IApproval
import com.example.tracktogether.Interfaces.INotification
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.adapter.ImageApprovalListAdapter
import com.example.tracktogether.data.Employee
import com.example.tracktogether.data.NotificationData
import com.example.tracktogether.data.PushNotification
import com.example.tracktogether.databinding.ActivityApprovedremotecheckinBinding
import com.example.tracktogether.repository.NotificationRepository
import com.example.tracktogether.service.FirebaseService
import com.example.tracktogether.service.FirebaseService.Companion.TOPIC
import com.example.tracktogether.viewmodel.EmployeeListViewModelFactory
import com.example.tracktogether.viewmodel.EmployeeViewModel
import com.google.firebase.messaging.FirebaseMessaging


/**
 * Fetch images for approval
 * Noted using picasso lib to display images from links (which is uploaded to our firebase cloud)
 * Realization of IApproval which have method changeApprovalStatus and onSuccessEmpList
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
class ApprovedRemoteCheckinActivity : AppCompatActivity(), IApproval, INotification {
    private lateinit var binding: ActivityApprovedremotecheckinBinding
    private val TAG = "ApprovedRemoteCheckinActivity"

    private var employeeArrayList = arrayListOf<Employee>()


    // Init a view model instance using the factory class
    private val employeeViewModel: EmployeeViewModel by viewModels {
        EmployeeListViewModelFactory((application as TrackTogetherApp).employeeListRepo,
            (application as TrackTogetherApp).imageRepository,
            (application as TrackTogetherApp).userPreferencesRepository,
            (application as TrackTogetherApp).authrepo,
        )
    }

    // Init firebase service and notification repo
    private val firebaseService = FirebaseService()
    private val notificationRepository = NotificationRepository()

    /**
     * Bind the recycler view
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovedremotecheckinBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //get all employee_image content which are not approved
        employeeViewModel.getAllNotApprovedEmployeeImage()
        employeeViewModel.iApproval = this

        binding.RecyclerViewApproval.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    /**
     * From IApproval
     * Listen for change in Approval Status. When admin press approve or reject update changes to Firestore
     * and display toast upon success
     * @param flag document update status -> true - success and false - failure
     * @param status Approve or Reject depending on button presssed
     */

    override fun changeApprovalStatus(flag: Boolean, status: String) {
        Log.d(TAG, "On Success")
        Toast.makeText(
            applicationContext,
            "Employee's Image successfully $status!",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * From IApproval (which inherited from IEmpList
     * Listen fetch employee collection from firebase if collection is received employeeList is send as an array list to recycler view for displaying
     * @param employeeList EmployeeListRepo will pass in a list of employee document fetched from Firestore
     */
    override fun onSuccessEmpList(employeeList: List<Employee>) {
        employeeArrayList = ArrayList(employeeList)
        Log.e(TAG, "HEREEEE 2")
        //creating our adapter
        val adapter = ImageApprovalListAdapter(
            employeeArrayList,
            employeeViewModel,
            notificationRepository,
            this
        )

        //now adding the adapter to recyclerview
        binding.RecyclerViewApproval.adapter = adapter
        val iterator = employeeArrayList.listIterator()
        Log.d(TAG, "ON SUCCESS")
        for (employee in iterator) {
            Log.d(TAG, employee.email.toString())
        }
    }

    /**
     * Listens to callback base on admin action on image approval
     * @param deviceid employee unique id
     * @param approval true = approved, false = rejected
     */
    override fun onApproval(deviceid: String, approval: Boolean) {
        employeeViewModel.storeDeviceToken(deviceid)
        Log.d("main", "DEVICE ID RECEIVED FROM DB: $deviceid")
        //FirebaseService.token = deviceid
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        var title = ""
        var message = ""
        if (approval) {
            title = "KauLa"
            message = "Your image have been approved!"
        } else {
            title = "KauLa"
            message = "Your image have been rejected!"
        }
        PushNotification(
            NotificationData(title, message),
            deviceid
        ).also {
            firebaseService.sendNotification(it)
        }
    }
}