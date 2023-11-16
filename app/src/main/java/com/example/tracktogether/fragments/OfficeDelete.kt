package com.example.tracktogether.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracktogether.databinding.OfficeDeleteFragmentBinding
import com.firebase.geofire.GeoFire
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Office Delete fragment to delete locations
 * Author: Reuben
 * Updated: 9 March 2022
 */
class OfficeDelete : Fragment() {

    companion object {
        fun newInstance() = OfficeDelete()
    }

    private lateinit var binding: OfficeDeleteFragmentBinding
    private lateinit var geoFire: GeoFire
    private lateinit var myLocationRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OfficeDeleteFragmentBinding.inflate(layoutInflater)

        // set up geofire to connect to firebase instance
        setGeoFire()

        // click listener
        binding.deleteLocationButton.setOnClickListener {
            removeLocation()
        }
        return binding.root
    }

    /**
     * Helper functio nto remove location based on user;s click
     */
    private fun removeLocation() {
        val location = binding.officeNametextView.text.toString()
        Log.e("delete", location)
        geoFire.removeLocation(location)
    }

    /**
     * Helper function to connect to firebase instance
     */
    private fun setGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("geofire")
        geoFire = GeoFire(myLocationRef)
    }


}