package com.example.tracktogether.checkinviews


import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tracktogether.MainActivity
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.databinding.ActivityNfcBinding
import com.example.tracktogether.viewmodel.NFCViewModel
import com.example.tracktogether.viewmodel.NFCViewModelFactory
import kotlinx.coroutines.delay

/**
 * Activity for NFC
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
class NFCActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var binding: ActivityNfcBinding
    private lateinit var tagTextView: TextView
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var office: String
    private lateinit var locationTag: String
    var TAG = NFCActivity::class.java.simpleName

    private val nfcViewModel: NFCViewModel by viewModels {
        NFCViewModelFactory(
            (application as TrackTogetherApp).attendanceRepository,
            (application as TrackTogetherApp).authrepo
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        office = intent.getStringExtra("OFFICE").toString()
        tagTextView = binding.tagtextView
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check if device has no NFC Adapter
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "NFC is not enabled or no NFC hardware", Toast.LENGTH_SHORT).show()
            finish()
        }
        setUpNFCEvent()

        // Get NFC Tag for location
        nfcViewModel.getLocationNFC(office)
    }

    private fun setUpNFCEvent() {
        lifecycleScope.launchWhenStarted {
            nfcViewModel.eventFlow.collect { event ->
                when (event) {
                    is NFCViewModel.NFCEvent.ErrorEvent -> {
                        Log.v(TAG, event.message)
                    }
                    is NFCViewModel.NFCEvent.LocationEvent -> {
                        locationTag = event.locationTag
                        Log.v(TAG, "$office: $locationTag")
                    }
                    is NFCViewModel.NFCEvent.TagEvent -> {
                        binding.nfcLoading.visibility = View.VISIBLE
                        binding.nfcProgressBar.visibility = View.VISIBLE
                        delay(1000)
                        val tagId = event.tagId
                        Log.v(TAG, "Scanned tag: $tagId")
                        if (tagId == locationTag) {
                            Log.v(TAG, "Nfc: $tagId , Location: $office TaggedNfc: $locationTag")
                            checkIn(office)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Please scan the NFC card at the entrance",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.nfcLoading.visibility = View.INVISIBLE
                            binding.nfcProgressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }

    /***
     * Enable reader mode for NFC Controller to act as NFC reader
     */
    override fun onResume() {
        super.onResume()
        if (nfcAdapter.isEnabled) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 2000)
            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, options)
        }
    }

    /***
     * Disable reader mode on pause
     */
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

    /***
     * Check user in if attendance is not taken for the day yet
     * @param location Location of office
     *
     */
    private fun checkIn(location: String) {
        nfcViewModel.uploadAttendanceToFirebase(location)
        /*val intent = Intent(this@NFCActivity, MainActivity::class.java)
        intent.putExtra("LOCATION", location)
        startActivity(intent)*/
        //finish()
        alertSuccess(location)
        binding.nfcLoading.visibility = View.INVISIBLE
        binding.nfcProgressBar.visibility = View.INVISIBLE
        //Toast.makeText(applicationContext, "Successfully checked into $location", Toast.LENGTH_SHORT).show()
    }

    private fun alertSuccess(location: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Success!")
        builder.setMessage("Successfully checked into $location, have a good day at work!")
        builder.setCancelable(false)
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
            val intent = Intent(this@NFCActivity, MainActivity::class.java)
            intent.putExtra("LOCATION", location)
            startActivity(intent)
            finish()
        })
        val alert = builder.create()
        alert.setTitle("Success!")
        alert.show()
    }
}