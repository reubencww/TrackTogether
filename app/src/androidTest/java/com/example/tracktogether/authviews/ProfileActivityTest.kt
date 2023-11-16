package com.example.tracktogether.authviews

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.example.tracktogether.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ProfileActivityTest{

    @Test
    fun test_isActivityInView(){
        val activityScenario = ActivityScenario.launch(ProfileActivity::class.java)
        onView(withId(R.id.activity_profile)).check(matches(isDisplayed()))
    }

    @Test
    fun test_visibility_Employee_Details(){
        val activityScenario = ActivityScenario.launch(ProfileActivity::class.java)
        onView(withId(R.id.editprofilefragment)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}