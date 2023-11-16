package com.example.tracktogether.authviews

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.tracktogether.R
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test


class AttendanceActivityTest{
    @get: Rule
    var attendanceRule = ActivityScenarioRule(AttendanceActivity::class.java)

    /**
     * check if recyclerview is displayed on AttendanceActivity launch
     */
    @Test
    fun test_attendanceVisible_launch(){
        onView(withId(R.id.attendanceActivity))
            .check(matches(isDisplayed()))
    }

    /**
     * test attendance title visibility in AttendanceActivity
     */
    @Test
    fun test_attendanceTitleDisplayed(){
        onView(withId(R.id.textViewAttendanceTitle))
            .check(matches(withText(R.string.attendance_history)))
    }

    /**
     * test spinner label visibility in AttendanceActivity
     */
    @Test
    fun test_spinnerTitleDisplayed(){
        onView(withId(R.id.departmentfiltertextView))
            .check(matches(withText(R.string.history_filter)))
    }

    /**
     * test spinner visibility in AttendanceActivity
     */
    @Test
    fun test_spinnerDisplayed(){
        onView(withId(R.id.historySpinner))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    /**
     * test spinner option text matches selected string
     */
    @Test
    fun test_spinnerSelectedItem(){
        onView(withId(R.id.historySpinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Today"))).perform(click())
        onView(withId(R.id.historySpinner)).check(matches(withSpinnerText(containsString("Today"))))

        onView(withId(R.id.historySpinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("1 Week"))).perform(click())
        onView(withId(R.id.historySpinner)).check(matches(withSpinnerText(containsString("1 Week"))))

        onView(withId(R.id.historySpinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("1 Month"))).perform(click())
        onView(withId(R.id.historySpinner)).check(matches(withSpinnerText(containsString("1 Month"))))

        onView(withId(R.id.historySpinner)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("3 Months"))).perform(click())
        onView(withId(R.id.historySpinner)).check(matches(withSpinnerText(containsString("3 Months"))))
    }
}