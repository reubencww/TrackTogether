package com.example.tracktogether.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.R


/**
 * Geo query adapter for office read fragment to display geo queries
 * Author: Reuben
 * Updated: 9 March 2022
 */
class GeoReadQueryAdapter(
    private val keys: ArrayList<String>, private val hashmap: HashMap<String, String>,
    onButtonListener: OnButtonListener
) :

    RecyclerView.Adapter<GeoReadQueryAdapter.ViewHolder>() {

    private var onButtonListener = onButtonListener

    class ViewHolder(view: View, onButtonListener: OnButtonListener) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        val onButtonListener = onButtonListener
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        val updateButton: Button = view.findViewById(R.id.updateButton)

        init {
            deleteButton.setOnClickListener(this)
            updateButton.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            if (p0 == deleteButton) {
                onButtonListener.onDeleteClick(absoluteAdapterPosition)
            } else if (p0 == updateButton) {
                onButtonListener.onUpdateClick(absoluteAdapterPosition)
            }

        }
    }

    interface OnButtonListener {
        fun onDeleteClick(position: Int)
        fun onUpdateClick(position: Int)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.geoquery_row_item, viewGroup, false)

        return ViewHolder(view, onButtonListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.titleTextView.text = keys[position]
        viewHolder.addressTextView.text = hashmap[keys[position]]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = keys.size
}