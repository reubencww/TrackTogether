package com.example.tracktogether.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.Interfaces.INotification
import com.example.tracktogether.R
import com.example.tracktogether.adminviews.ApprovedRemoteCheckinActivity
import com.example.tracktogether.data.Employee
import com.example.tracktogether.repository.NotificationRepository
import com.example.tracktogether.service.FirebaseService
import com.example.tracktogether.service.FirebaseService.Companion.TOPIC
import com.example.tracktogether.viewmodel.EmployeeViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso


/**
 * Recycler view to manage newly uploaded images by employee (contain some approval/rejection buttons)
 * Author: May Madi Aung
 * Last updated: 10 Mar 2022
 */


class ImageApprovalListAdapter(
    val employee: ArrayList<Employee>,
    val employeeViewModel: EmployeeViewModel,
    val notificationRepository: NotificationRepository,
    val notificationListener: INotification
) : RecyclerView.Adapter<ImageApprovalListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageApprovalListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.approvalremotecheckin_list, parent, false)
        return ViewHolder(v)
    }

    /**
     * Using Picasso to fetch images from FireBase Cloud
     * Upon click button (approve/reject) -> start new intent to same activity -> kill bg activity by casting the context of previous activity
     */

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentitem = employee[position]
        holder.email.text = currentitem.email.toString()
        val picasso = Picasso.get()
        picasso.load(currentitem.imageUrl).placeholder(R.drawable.loading).rotate(270F)
            .into(holder.empimage)

        //Approve
        holder.approve.setOnClickListener {
            notificationRepository.getDeviceToken(
                currentitem.uid.toString(),
                true,
                notificationListener
            )
            employeeViewModel.setStatusEmployeeImage("Approved", currentitem.uid.toString())
            val intent = Intent(holder.itemView.context, ApprovedRemoteCheckinActivity::class.java)
            holder.itemView.context.startActivity(intent)
            (holder.itemView.context as ApprovedRemoteCheckinActivity).finish()
        }
        //Reject
        holder.reject.setOnClickListener {
            notificationRepository.getDeviceToken(
                currentitem.uid.toString(),
                false,
                notificationListener
            )
            employeeViewModel.setStatusEmployeeImage("Rejected", currentitem.uid.toString())
            val intent = Intent(holder.itemView.context, ApprovedRemoteCheckinActivity::class.java)
            holder.itemView.context.startActivity(intent)
            (holder.itemView.context as ApprovedRemoteCheckinActivity).finish()
        }
    }

    override fun getItemCount(): Int {
        return employee.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val email: TextView = itemView.findViewById(R.id.textViewEmail_ApprovalRM)
        val empimage: ImageView = itemView.findViewById(R.id.imageViewEmployee)
        val approve: Button = itemView.findViewById(R.id.buttonApprove)
        val reject: Button = itemView.findViewById(R.id.buttonReject)
    }
}