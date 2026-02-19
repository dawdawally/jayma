package com.jayma.pos.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jayma.pos.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductListFragmentTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun productListFragment_isDisplayed() {
        // Verify that the product list fragment is displayed
        onView(withId(R.id.productsRecyclerView))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun searchView_isDisplayed() {
        // Verify search view is displayed
        onView(withId(R.id.searchView))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun searchView_canEnterText() {
        // Enter text in search view
        onView(withId(R.id.searchView))
            .perform(click())
            .perform(typeText("test"))
    }
    
    @Test
    fun scanButton_isDisplayed() {
        // Verify scan button (FAB) is displayed
        onView(withId(R.id.scanButton))
            .check(matches(isDisplayed()))
    }
}
