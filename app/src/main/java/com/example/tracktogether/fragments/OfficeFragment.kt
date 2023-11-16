package com.example.tracktogether.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracktogether.adapter.TableRowAdapter
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.databinding.FragmentOfficeBinding

/**
 * Office Fragment for history attendance records related to physical office check in
 * Author: Cheng Hao
 * Updated: 13 March 2022
 */
class OfficeFragment : Fragment() {

    private lateinit var binding: FragmentOfficeBinding
    private var itemList = arrayListOf<Attendance>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOfficeBinding.inflate(inflater, container, false)

        binding.officeRecyclerView.setHasFixedSize(true)
        binding.officeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.officeRecyclerView.adapter = TableRowAdapter(itemList, requireContext())

        return binding.root
    }

    /**
     * Whenever new datepicker range is selected, update the fragment
     */
    fun refreshList(officeAttendanceList: ArrayList<Attendance>) {
        itemList.clear() // empty list
        itemList = officeAttendanceList
        binding.officeRecyclerView.adapter = TableRowAdapter(officeAttendanceList, requireContext())
    }
}