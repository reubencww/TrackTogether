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
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.adminviews.UploadNFCActivity
import com.example.tracktogether.databinding.OfficeCreateFragmentBinding
import com.example.tracktogether.viewmodel.OfficeCRUDViewModel
import com.example.tracktogether.viewmodel.OfficeCRUDViewModelFactory

/**
 * Office Create fragment view for creating office location using geofire
 * Author: Reuben
 * Updated: 9 March 2022
 */
class OfficeCreate : Fragment() {

    companion object {
        fun newInstance() = OfficeCreate()
    }

    private lateinit var binding: OfficeCreateFragmentBinding
    private lateinit var latlong: Address

    // View Model instance
    private val nfcViewModel: OfficeCRUDViewModel by viewModels {
        OfficeCRUDViewModelFactory((activity!!.application as TrackTogetherApp).officeCRUDRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OfficeCreateFragmentBinding.inflate(layoutInflater)

        // listener to add location
        binding.addLocationbutton.setOnClickListener {
            if (isinputEmpty()) {
                // check if any input issues
                binding.errorFeedbackTextView.text = "Invalid postal code."
                Toast.makeText(context!!, "Invalid postal code", Toast.LENGTH_LONG).show()
            } else {
                // get the key, fire up nfc activity then upload to geofire
                val input = binding.postalCodeTextView.text.toString()
                latlong = nfcViewModel.getPostalCode(Geocoder(context!!), input)
                //save office name in crud repo
                nfcViewModel.saveOfficeName(binding.officeNametextView.text.toString())
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
     * Helper function to check if input is empty
     */
    private fun isinputEmpty(): Boolean {
        return binding.postalCodeTextView.text.isNullOrEmpty()
                && binding.officeNametextView.text.isNullOrEmpty()
    }

}