package com.example.tracktogether.fragments

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracktogether.R
import com.example.tracktogether.adapter.GeoReadQueryAdapter
import com.example.tracktogether.databinding.OfficeReadFragmentBinding
import com.example.tracktogether.viewmodel.AddOfficeViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

/**
 * Office Read fragment view for reading geofire queries and displaying using recycler view
 * Author: Reuben
 * Updated: 9 March 2022
 */
class OfficeRead : Fragment(), GeoReadQueryAdapter.OnButtonListener {

    companion object {
        fun newInstance() = OfficeRead()
    }

    // variables
    private lateinit var geoFire: GeoFire
    private lateinit var myLocationRef: DatabaseReference
    private lateinit var geoQuery: GeoQuery
    private lateinit var offices: HashMap<String, String>
    private lateinit var binding: OfficeReadFragmentBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: AddOfficeViewModel

    val keys = ArrayList<String>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // shared view model between office CRUD fragments so that this fragment can pass data to other fragments
        viewModel = ViewModelProviders.of(activity!!)[AddOfficeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OfficeReadFragmentBinding.inflate(layoutInflater)

        // Connect to firebase
        setGeoFire()

        // init hashmaps
        offices = HashMap()

        // Get all geofire location from Singapore
        geoQuery = geoFire.queryAtLocation(
            GeoLocation(1.3680309395551657, 103.80400692159104),
            25.0
        )

        // Geo Query event listener to query all available/ stored locations
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            // When a location key has entered
            override fun onKeyEntered(key: String, location: GeoLocation) {
                println(
                    String.format(
                        "Key %s entered the search area at [%f,%f]",
                        key,
                        location.latitude,
                        location.longitude
                    )
                )
                val geocoder = Geocoder(context!!, Locale.getDefault())
                val addressDetails =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                // Store in hashmap for display
                insertHashmap(key, addressDetails[0].getAddressLine(0).toString())
                // Keep track of the key
                keys.add(key)
            }

            override fun onKeyExited(key: String) {
                println(String.format("Key %s is no longer in the search area", key))
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                println(
                    String.format(
                        "Key %s moved within the search area to [%f,%f]",
                        key,
                        location.latitude,
                        location.longitude
                    )
                )
            }

            override fun onGeoQueryReady() {
                println("All initial data has been loaded and events have been fired!")
                // Once all queries returned, display all location
                displayLocations()
            }

            override fun onGeoQueryError(error: DatabaseError) {
                System.err.println("There was an error with this query: $error")
            }
        })
        return binding.root
    }

    /**
     * Helper function to set up geofrie
     */
    private fun setGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("geofire")
        geoFire = GeoFire(myLocationRef)
    }

    /**
     * Helper function to set up recycler view to display all geofire queries
     */
    private fun displayLocations() {
        // use either recycler view or list view to display query
        recyclerView = binding.geoQueryRecyclerView
        recyclerView.adapter = GeoReadQueryAdapter(keys, offices, this)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    /**
     * Helper function to insert new entered geofire keys into hashmap
     */
    private fun insertHashmap(key: String, address: String) {
        offices[key] = address
    }

    /**
     * Delete button click listener in recycler view
     */
    override fun onDeleteClick(position: Int) {
        val deleteKey = keys[position]
        geoFire.removeLocation(deleteKey)
        recyclerView.adapter?.notifyItemRemoved(position)
        recyclerView.adapter?.notifyDataSetChanged()
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.editofficeFragmentView, OfficeRead()).commit()
        Toast.makeText(context, "$deleteKey successfully deleted", Toast.LENGTH_SHORT).show()
    }

    /**
     * Update button click listener in recycler view
     */
    override fun onUpdateClick(position: Int) {
        val updateKey = keys[position]
        val updateAddress = offices[keys[position]]?.takeLast(6)
        viewModel.setOfficeName(updateKey)
        viewModel.setPostalCode(updateAddress.toString())
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.editofficeFragmentView, OfficeUpdate()).commit()
    }

}