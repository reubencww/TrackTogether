package com.example.tracktogether.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.R
import com.example.tracktogether.adminviews.TrackEmpListActivity
import com.example.tracktogether.data.Employee
import com.squareup.picasso.Picasso

/**
 * Recycler view for admin to view all employee in the employee collection in Firestore
 * Author: May Madi Aung
 * Last updated: 10 Mar 2022
 */


class EmployeeListAdapter(val context: Context, val employeeList: ArrayList<Employee>) :
    RecyclerView.Adapter<EmployeeListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.employee_list, parent, false)
        return ViewHolder(context, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(employeeList[position])
    }

    override fun getItemCount(): Int {
        return employeeList.size
    }

    /**
     * Using Picasso to fetch images from FireBase Cloud
     * if the employee have yet to add image display default profilepicholder
     */
    class ViewHolder(private val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItems(emp: Employee) {
            val textViewName = itemView.findViewById(R.id.textViewUsername) as TextView
            val textViewAddress = itemView.findViewById(R.id.textViewAddress) as TextView
            val textViewDesignation = itemView.findViewById(R.id.department) as TextView
            val employeeCard = itemView.findViewById(R.id.layoutId) as RelativeLayout
            val image = itemView.findViewById(R.id.imageView_icon) as ImageView
            textViewName.text = emp.firstName
            textViewAddress.text = emp.email
            textViewDesignation.text = emp.designation
            val picasso = Picasso.get()

            if (emp.imageUrl == null) {
                image.setImageResource(R.drawable.profilepicholder)
            } else {
                picasso.load(emp.imageUrl).placeholder(R.drawable.loading).rotate(270F).into(image)
            }

            /**
             * Passing attributes related to selected employee to TrackEmpListActivity
             */
            employeeCard.setOnClickListener {
                val intent = Intent(context, TrackEmpListActivity::class.java)
                val extras = Bundle()
                extras.putString("F_NAME", emp.firstName)
                extras.putString("L_NAME", emp.lastName)
                extras.putString("EMAIL", emp.email)
                extras.putString("ID", emp.uid)
                extras.putString("DESIGNATION", emp.designation)
                extras.putString("URL_IMG", emp.imageUrl)
                intent.putExtras(extras)
                context.startActivity(intent)
            }
        }
    }
}