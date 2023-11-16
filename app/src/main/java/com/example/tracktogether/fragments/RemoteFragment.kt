package com.example.tracktogether.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracktogether.adapter.TableRowAdapter
import com.example.tracktogether.data.Attendance
import com.example.tracktogether.databinding.FragmentRemoteBinding

/**
 * Office Fragment for history attendance records related to remote check in
 * Author: Cheng Hao
 * Updated: 13 March 2022
 */
class RemoteFragment : Fragment() {

    private lateinit var binding: FragmentRemoteBinding
    private var itemList = arrayListOf<Attendance>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRemoteBinding.inflate(inflater, container, false)

        binding.remoteRecyclerView.setHasFixedSize(true)
        binding.remoteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.remoteRecyclerView.adapter = TableRowAdapter(itemList, requireContext())

        return binding.root
    }

    /**
     * Whenever new datepicker range is selected, update the fragment
     */
    fun refreshList(remoteAttendanceList: ArrayList<Attendance>) {
        itemList.clear() // empty list
        itemList = remoteAttendanceList
        binding.remoteRecyclerView.adapter = TableRowAdapter(remoteAttendanceList, requireContext())
    }
}