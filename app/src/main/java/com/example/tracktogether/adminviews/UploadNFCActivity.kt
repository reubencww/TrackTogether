package com.example.tracktogether.adminviews

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tracktogether.MainActivity
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.databinding.ActivityUploadOfficeBinding
import com.example.tracktogether.viewmodel.OfficeCRUDViewModel
import com.example.tracktogether.viewmodel.OfficeCRUDViewModelFactory
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

/**
 * upload NFC when admin makes any edits to office locations
 * Author: Reuben
 * Updated: 9 March 2022
 */
class UploadNFCActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityUploadOfficeBinding
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var officeName: String
    private lateinit var offices: HashMap<kotlin.String, GeoLocation>
    private lateinit var geoFire: GeoFire
    private lateinit var myLocationRef: DatabaseReference
    private var latitude = 0.00
    private var longitude = 0.00


    var TAG = UploadNFCActivity::class.java.simpleName

    private val nfcViewModel: OfficeCRUDViewModel by viewModels {
        OfficeCRUDViewModelFactory((application as TrackTogetherApp).officeCRUDRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadOfficeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nfcViewModel.officeName.observe(this) { officeName ->
            this.officeName = officeName
        }
        var b = intent.extras
        latitude = b!!.getDouble("latitude")
        longitude = b.getDouble("longitude")

        setGeoFire()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check if device has no NFC Adapter
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "NFC is not enabled or no NFC hardware", Toast.LENGTH_SHORT).show()
            finish()
        }
        lifecycleScope.launchWhenStarted {
            nfcViewModel.eventFlow.collect { event ->
                when (event) {
                    is OfficeCRUDViewModel.NFCEvent.TagEvent -> {
                        binding.nfcLoading.visibility = View.VISIBLE
                        binding.nfcProgressBar.visibility = View.VISIBLE
                        delay(1000)
                        val tagId = event.tagId
                        Log.e(TAG, tagId)
                        //store in geofire
                        addToGeoFire(tagId)
                        finish()
                        goBackToAdminView()
                    }
                }
            }
        }
    }

    private fun goBackToAdminView() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        Toast.makeText(
            applicationContext,
            "Successfully Edited Office Locations",
            Toast.LENGTH_SHORT
        ).show()
        binding.nfcLoading.visibility = View.INVISIBLE
        binding.nfcProgressBar.visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter.isEnabled) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 2000)
            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, options)
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter.isEnabled) {
            nfcAdapter.disableReaderMode(this)
        }
    }

    /***
     * This method overrides the ReaderCallback onTagDiscovered
     * https://developer.android.com/reference/android/nfc/NfcAdapter.ReaderCallback
     * onTagDiscovered runs on another thread when a card is discovered
     * https://stackoverflow.com/questions/64920307/how-to-write-ndef-records-to-nfc-tag/64921434#64921434
     */
    override fun onTagDiscovered(tag: Tag?) {
        // Read appropriate technology type class
        val tagId = tag?.id
        if (tagId != null) {
            nfcViewModel.checkTag(tagId)
        }
    }

    private fun addToGeoFire(tagID: String) {
        Log.e("here", "tag: $tagID, name: $officeName")
        offices = HashMap()
        offices[officeName] = GeoLocation(latitude, longitude)
        val db = FirebaseFirestore.getInstance()
        val myCard = hashMapOf("nfcId" to tagID)
        for (office in offices) {
            geoFire.removeLocation(office.key)
            geoFire.setLocation(office.key, office.value)
            db.collection("nfc").document(office.key).set(myCard)
        }
        Toast.makeText(applicationContext, "Successfully added Location", Toast.LENGTH_LONG)
    }

    private fun setGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("geofire")
        geoFire = GeoFire(myLocationRef)
    }
}