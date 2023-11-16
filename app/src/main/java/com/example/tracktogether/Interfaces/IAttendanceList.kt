package com.example.tracktogether.Interfaces

import com.example.tracktogether.data.Attendance

/**
 * Interface implemented by AttendanceActivity and TrackEmpListActivity
 * getDateRangeAttendanceDetails(employeeuid: String, from: Timestamp, to: Timestamp, IAttendanceList: IAttendanceList)
 * and getSelectedDateRangeAttendanceDetails(employeeuid: String, from: Timestamp, to: Timestamp, AttendanceList: IAttendanceList) in AttendanceRepository
 * will fetch the attendance documents and pass in onSuccessAttendanceList()
 * Author: May Madi Aung
 * Updated: 10 Mar 2022
 */
interface IAttendanceList {
    fun onSuccessAttendanceList(attendanceList: List<Attendance>)
}