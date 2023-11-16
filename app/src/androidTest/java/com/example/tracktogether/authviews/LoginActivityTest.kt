package com.example.tracktogether.authviews

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.example.tracktogether.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class LoginActivityTest {

    @get: Rule
    val activityScenario: ActivityScenarioRule<LoginActivity> = ActivityScenarioRule(LoginActivity::class.java)

    /**
     * Basic test to test for
     * 1. Did we launch the activity
     * 2. Is the activity in view
     */
    @Test
    fun test_isLoginActivityInView() {

        onView(withId(R.id.loginLayout))
            .check(matches(isDisplayed()))
    }

    /**
     * Basic test to test visibility of
     * 1. login page icon
     * 2. login email field
     * 3. login password field
     * 4. login button
     */
    @Test
    fun test_isWidgetsVisible() {

        onView(withId(R.id.appLogoImageView))
            .check(matches(isDisplayed()))

        onView(withId(R.id.usernameEditText))
            .check(matches(isDisplayed()))

        onView(withId(R.id.passwordEditText))
            .check(matches(isDisplayed()))

        onView(withId(R.id.loginButton))
            .check(matches(isDisplayed()))
    }

    /**
     * Basic test to test visibility of
     * 1. login page background image
     * 2. email field icon animation
     * 3. password field icon animation
     */
    @Test
    fun test_isLoginFieldsVisible() {

        onView(withId(R.id.loginWaveTopView))
            .check(matches(isDisplayed()))

        onView(withId(R.id.loginWaveImageview))
            .check(matches(isDisplayed()))

        onView(withId(R.id.emailAnimationImageView))
            .check(matches(isDisplayed()))

        onView(withId(R.id.passwordAnimationImageView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_isLoginButtonTextDisplayed() {

        onView(withId(R.id.loginButton))
            .check(matches(withText(R.string.login_button)))

    }


}