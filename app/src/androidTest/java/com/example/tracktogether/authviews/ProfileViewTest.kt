package com.example.tracktogether.authviews

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.example.tracktogether.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class ProfileViewTest{

    @get: Rule
    val activityRule: ActivityScenarioRule<ProfileActivity> = ActivityScenarioRule(ProfileActivity::class.java)

    @Test
    fun test_isActivityInView(){
       onView(ViewMatchers.withId(R.id.viewprofileFragement))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}