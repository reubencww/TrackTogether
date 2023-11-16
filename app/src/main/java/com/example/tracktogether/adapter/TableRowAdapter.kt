package com.example.tracktogether.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.R
import com.example.tracktogether.data.Attendance
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Recycler view for daily records consist of columns: date, time in, time out, location and image
 * Author: Cheng Hao
 * Last updated: 13 Mar 2022
 */

class TableRowAdapter(
    private var userArrayList: ArrayList<Attendance>,
    private val context: Context,
) :
    RecyclerView.Adapter<TableRowAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.table_row_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bindItems(userArrayList[i], context)
    }

    override fun getItemCount(): Int {
        return userArrayList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    /**
     * Displaying the records fetched from db on table row
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindItems(attendance: Attendance, context: Context) {
            val tvDate: TextView = itemView.findViewById(R.id.date_title_textView)
            val tvTimeIn: TextView = itemView.findViewById(R.id.time_in_title_textView)
            val tvTimeOut: TextView = itemView.findViewById(R.id.time_out_title_textView)
            val tvLocation: TextView = itemView.findViewById(R.id.location_title_textView)
            val tvImage: TextView = itemView.findViewById(R.id.image_title_textView)

            val date = attendance.date?.toDate()
            val timeIn = attendance.inTime?.toDate()
            val currentDate =
                date?.toInstant()?.atZone(ZoneId.of("Asia/Singapore"))?.toLocalDateTime()
            val dateString = currentDate?.let { formatDate(it) }
            if (timeIn != null) {
                val timeInString = currentDate?.let { formatTime(it) }
                val timeOut = attendance.outTime?.toDate()
                var timeOutString = ""
                if (timeOut != null) {
                    val localTimeOut =
                        timeOut.toInstant()?.atZone(ZoneId.of("Asia/Singapore"))?.toLocalDateTime()
                    timeOutString = localTimeOut?.let { formatTime(it) }.toString()
                }

                tvDate.text = dateString
                tvTimeIn.text = timeInString
                tvTimeOut.text = timeOutString
                tvLocation.text = attendance.location.toString()
            } else {
                tvDate.text = dateString
                tvTimeIn.text = "Absent"
                tvTimeOut.text = "Absent"
            }

            if (attendance.imageUrl != null && attendance.imageUrl != "null") {
                tvImage.text = "View"
                tvImage.setOnClickListener {
                    showImage(context, attendance.imageUrl.toString())
                }
            }
        }

        /**
         * showing image on a dialog when image column is clicked
         */
        private fun showImage(context: Context, imageUrl: String) {
            val builder = Dialog(context)
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
            builder.window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
            val imageView = ImageView(context)
            Picasso
                .get()
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .rotate(270F)
                .resize(500, 500)
                .into(imageView)
            builder.addContentView(
                imageView, RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            builder.show()
        }

        /**
         * convert LocalDateTime to displayable date string
         */
        private fun formatDate(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("(E) d/M/yyyy")
            return formatter.format(date)
        }

        /**
         * convert LocalDateTime to displayable time string
         */
        private fun formatTime(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("HH:mm a")
            return formatter.format(date)
        }
    }
}