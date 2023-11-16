package com.example.tracktogether.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Adapter use for HR/Manager tracking employee attendance
 * Author: Cheng Hao
 * Last updated: 13 Mar 2022
 */

class AttendanceTabAdapter(supportFragmentManager: FragmentManager) :
    FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitleList = ArrayList<String>()

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return fragmentTitleList[position]
    }

    /**
     * adding office/remote fragment to fragment list
     */
    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }
}