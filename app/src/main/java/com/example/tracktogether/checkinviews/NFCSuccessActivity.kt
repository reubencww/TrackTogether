package com.example.tracktogether.checkinviews

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.databinding.ActivityNfcsuccessBinding

/**
 * Activity for successful NFC attendance
 * Author: Ong Ze Quan
 * Updated: 5 Mar 2022
 */
class NFCSuccessActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private lateinit var binding: ActivityNfcsuccessBinding
    private lateinit var messageTextView: TextView
    private lateinit var nfcAdapter: NfcAdapter
    var TAG = NFCSuccessActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcsuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "NFC is not enabled or no NFC hardware", Toast.LENGTH_SHORT).show()
        }
        messageTextView = binding.messageTextView
        // Probably wait for firebase response to check if NFC card is valid for LOCATION
        // Then update TextView
        val location = intent.getStringExtra("LOCATION")
        messageTextView.text = "Successfully checked into $location"

    }


    /***
     * Enable reader mode for NFC Controller to act as NFC reader
     */
    override fun onResume() {
        super.onResume()
        if (nfcAdapter.isEnabled) {
            val options = Bundle()
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
     * Ignoring NFC Tag even if scanned, to improve user experience
     */
    override fun onTagDiscovered(tag: Tag?) {
        Log.v(TAG, "Doing nothing with $tag")
    }
}