package com.example.tracktogether.fragments

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.adminviews.UploadNFCActivity
import com.example.tracktogether.databinding.OfficeUpdateFragmentBinding
import com.example.tracktogether.viewmodel.AddOfficeViewModel
import com.example.tracktogether.viewmodel.OfficeCRUDViewModel
import com.example.tracktogether.viewmodel.OfficeCRUDViewModelFactory

/**
 * Office update fragment to display user chosen office and location automatically
 * in update fields
 * Author: Reuben
 * Updated: 9 March 2022
 */
class OfficeUpdate : Fragment() {

    companion object {
        fun newInstance() = OfficeCreate()
    }

    //private lateinit var viewModel: OfficeCRUDViewModel
    private lateinit var binding: OfficeUpdateFragmentBinding
    private lateinit var latlong: Address
    private lateinit var viewModel: AddOfficeViewModel

    // viewmodel instance
    private val nfcViewModel: OfficeCRUDViewModel by viewModels {
        OfficeCRUDViewModelFactory((activity!!.application as TrackTogetherApp).officeCRUDRepository)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Get shared view model
        viewModel = ViewModelProviders.of(activity!!)[AddOfficeViewModel::class.java]
        // Observe office name and set it in xml
        viewModel.getText().observe(
            viewLifecycleOwner
        ) { charSequence -> binding.officeNameTextView.setText(charSequence) }
        // Observe postal code and set it in xml
        viewModel.getPostalCode().observe(
            viewLifecycleOwner
        ) { charSequence -> binding.postalCodeTextView.setText(charSequence) }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OfficeUpdateFragmentBinding.inflate(layoutInflater)

        // click listener to fire up NFC to update the location as well
        binding.updateLocationButton.setOnClickListener {
            if (isinputEmpty()) {
                Toast.makeText(context!!, "Invalid postal code", Toast.LENGTH_LONG).show()
            } else {
                // get the key, fire up nfc activity then upload to geofire
                val input = binding.postalCodeTextView.text.toString()
                latlong = nfcViewModel.getPostalCode(Geocoder(context!!), input)
                //save office name in crud repo
                nfcViewModel.saveOfficeName(binding.officeNameTextView.text.toString())
                // start nfc activity
                val intent: Intent = Intent(activity, UploadNFCActivity::class.java)
                var b = Bundle()
                b.putDouble("latitude", latlong.latitude)
                b.putDouble("longitude", latlong.longitude)
                intent.putExtras(b)
                startActivity(intent)
            }
        }
        return binding.root
    }

    /**
     * Helper functio nto check if input is empty
     */
    private fun isinputEmpty(): Boolean {
        return binding.postalCodeTextView.text.isNullOrEmpty()
                && binding.officeNameTextView.text.isNullOrEmpty()
    }

}