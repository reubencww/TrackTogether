package com.example.tracktogether.viewmodel

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.tracktogether.repository.OfficeCRUDRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Office CRUD fragments shared view model. Main admin activity holds a reference to this ViewModel
 * and shares it between all fragments related to office location CRUD for geofire
 * Author: Reuben
 * Updated: 9 March 2022
 */
class OfficeCRUDViewModel(
    private val officeCURDRepository: OfficeCRUDRepository
) : ViewModel() {

    // Manage latlong data when user chooses office from recycler view
    private lateinit var latlong: Address

    // Manage office name data when user chooses office from recycler view
    val officeName = officeCURDRepository.officeNameFlow.asLiveData()

    /**
     * Helper function to pass office name data from recycler view to
     * Preference datastore
     */
    fun saveOfficeName(officeName: String) = viewModelScope.launch(Dispatchers.IO) {
        officeCURDRepository.saveOfficeName(officeName)
    }

    /***
     * Channels for handling one time events
     */
    sealed class NFCEvent {
        data class TagEvent(val tagId: String) : NFCEvent()
    }

    private val eventChannel = Channel<NFCEvent>()

    val eventFlow = eventChannel.receiveAsFlow()

    /**
     * Check tag that was scanned by device
     */
    fun checkTag(tagId: ByteArray?) = viewModelScope.launch(Dispatchers.IO) {
        eventChannel.send(NFCEvent.TagEvent(tagId?.toHex()!!))
    }

    /***
     * Helper function to convert ByteArray to Hex
     */
    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    private fun storeInGeoFire() {

    }

    /**
     * Help function to convert postal code to latlong
     */
    @SuppressLint("DefaultLocale")
    fun getPostalCode(geocoder: Geocoder, postalCode: String): Address {
        var address = geocoder.getFromLocationName(postalCode, 1)
        if (address.isNotEmpty()) {
            latlong = address[0]
        }
        return latlong
    }


}

/**
 * Factory Pattern to instantiate ViewModel
 */
class OfficeCRUDViewModelFactory(
    private val officeCURDRepository: OfficeCRUDRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfficeCRUDViewModel::class.java)) {
            return OfficeCRUDViewModel(officeCURDRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
