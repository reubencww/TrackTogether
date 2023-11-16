package com.example.tracktogether.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * View Model for add office activity from admin feature
 * Author: Reuben
 * Updated: 9 March 2022
 */
class AddOfficeViewModel : ViewModel() {

    // Live data for office update framgent to observe and get user chosen office name and postal code
    private var officeName: MutableLiveData<CharSequence> = MutableLiveData()
    private var postalcode: MutableLiveData<CharSequence> = MutableLiveData()

    /**
     * Helper function to set Office name
     */
    fun setOfficeName(input: CharSequence) {
        officeName.value = input
    }

    /**
     * Helper function to get office name
     */
    fun getText(): LiveData<CharSequence> {
        return officeName
    }

    /**
     * Helper function to set postal code
     */
    fun setPostalCode(input: CharSequence) {
        postalcode.value = input
    }

    /**
     * Helper function to get postal code
     */
    fun getPostalCode(): LiveData<CharSequence> {
        return postalcode
    }

}
