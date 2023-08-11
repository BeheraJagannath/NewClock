package com.example.javamodel

import android.view.View

interface TimeZoneListener {
    fun OnfileClicked(view: View?, note: CityTimeZone?, position: Int)
    fun OnLongClick(view: View?, note: CityTimeZone?, position: Int)
}
