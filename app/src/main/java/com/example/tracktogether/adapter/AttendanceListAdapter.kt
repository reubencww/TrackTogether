package com.example.tracktogether.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.R
import com.example.tracktogether.data.Attendance
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Recycler view for tracking personal history records
 * Author: Cheng Hao
 * Last updated: 10 Mar 2022
 */

class AttendanceListAdapter(val attendanceList: ArrayList<Attendance>) :
    RecyclerView.Adapter<AttendanceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.attendance_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(attendanceList[position])
    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindItems(attendance: Attendance) {
            val textViewDate = itemView.findViewById(R.id.textViewDate) as TextView
            val textViewTimeIn = itemView.findViewById(R.id.textViewTimeIn) as TextView
            val textViewTimeOut = itemView.findViewById(R.id.textViewTimeOut) as TextView
            val textViewAttendance = itemView.findViewById(R.id.textViewAttendance) as TextView

            val timeIn =
                attendance.inTime?.toDate()?.toInstant()?.atZone(ZoneId.of("Asia/Singapore"))
                    ?.toLocalDateTime()
            val timeOut =
                attendance.outTime?.toDate()?.toInstant()?.atZone(ZoneId.of("Asia/Singapore"))
                    ?.toLocalDateTime()
            val dateString = timeIn?.let { formatDate(it) }
            val timeInString = timeIn?.let { formatTime(it) }
            val timeOutString = timeOut?.let { formatTime(it) }

            if (timeIn?.hour!! > 8) {
                textViewAttendance.setTextColor(Color.parseColor("#FFD65301")) //orange code
                textViewAttendance.text = "Late"
            } else {
                textViewAttendance.setTextColor(Color.GREEN)
                textViewAttendance.text = "Present"
            }
            textViewDate.text = dateString
            textViewTimeIn.text = "Checked in: $timeInString"
            textViewTimeOut.text = "Checked Out: $timeOutString"
        }

        /**
         * convert LocalDateTime to displayable date string
         */
        private fun formatDate(date: LocalDateTime): String? {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
            return formatter.format(date)
        }

        /**
         * convert LocalDateTime to displayable time string
         */
        private fun formatTime(date: LocalDateTime): String? {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return formatter.format(date)
        }
    }
}