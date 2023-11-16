package com.example.tracktogether.adminviews

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tracktogether.R
import com.example.tracktogether.adapter.TableRowAdapter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TrackEmpListActivityTest{
    @get: Rule
    var attendanceRule = ActivityScenarioRule(TrackEmpListActivity::class.java)


    /**
     * check if recyclerview is displayed on TrackEmpListActivity launch
     */
    @Test
    fun test_trackAttendanceVisible_launch(){
        onView(withId(R.id.trackAttendanceActivity))
            .check(matches(isDisplayed()))
    }

    /**
     * test office fragment visibility in TrackEmpListActivity
     */
    @Test
    fun test_FragmentDisplayed_launch(){
        onView(withId(R.id.office_fragment))
            .check(matches(isDisplayed()))
    }

    /**
     * test remote fragment visibility in TrackEmpListActivity
     */
    @Test
    fun test_remoteFramentDisplayed(){
        onView(withId(R.id.viewPager))
            .check(matches(hasDescendant(withId(R.id.remote_fragment))))
    }

    /**
     * test activity title visibility in TrackEmpListActivity
     */
    @Test
    fun test_isPageTitleTextDisplayed(){
        onView(withId(R.id.textViewEmployeeListTitle))
            .check(matches(withText(R.string.attendance_history)))
    }

    /**
     * test to label visibility in TrackEmpListActivity
     */
    @Test
    fun test_isToTitleTextDisplayed(){
        onView(withId(R.id.to_textView))
            .check(matches(withText(R.string.to)))
    }

    /**
     * test from label visibility in TrackEmpListActivity
     */
    @Test
    fun test_isFromTitleTextDisplayed(){
        onView(withId(R.id.from_textView))
            .check(matches(withText(R.string.from)))
    }

    /**
     * test button text visibility in TrackEmpListActivity
     */
    @Test
    fun test_trackButtonVisibility(){
        onView(withId(R.id.track_button))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    /**
     * check if date input is valid
     */
    @Test
    fun test_fromDateInput_isNotValid(){
        onView(withId(R.id.from_editText)).perform(replaceText("01/2020/20"))
        onView(withId(R.id.track_button)).perform(click())
        onView(withId(R.id.from_editText)).check(matches(hasErrorText("Field is not valid")))
    }

    /**
     * check if date input is valid
     */
    @Test
    fun test_toDateInput_isNotValid(){
        onView(withId(R.id.from_editText)).perform(replaceText("01/03/2020"))
        onView(withId(R.id.to_editText)).perform(replaceText("01/2020/20"))
        onView(withId(R.id.track_button)).perform(click())
        onView(withId(R.id.to_editText)).check(matches(hasErrorText("Field is not valid")))
    }
    /**
     * check if date input is valid
     */
    @Test
    fun test_checkRecyclerView_isShown(){
        onView(withId(R.id.from_editText)).perform(replaceText("01/03/2020"))
        onView(withId(R.id.to_editText)).perform(replaceText("02/03/2020"))
        onView(withId(R.id.track_button)).perform(click())
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()))
    }
}