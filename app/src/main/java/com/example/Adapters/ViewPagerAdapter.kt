package com.example.Adapters

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.fragments.*
import com.example.helpers.*
import com.example.models.Alarm
import com.simplemobiletools.commons.models.AlarmSound


class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = HashMap<Int, Fragment>()
    override fun getCount()= TABS_COUNT

    override fun getItem(position: Int): Fragment {
        val fragment = getFragment(position)
        fragments[position] = fragment
        return fragment
    }

    private fun getFragment(position: Int) = when (position) {
        0 -> AlarmFragment()
        1 ->SearchFragment()
        2 -> TimerFragment()
        3 -> StopFragment()
        else -> throw RuntimeException("Trying to fetch unknown fragment id $position")
    }

    fun updateClockTabAlarm() {
        (fragments[TAB_CLOCK] as? SearchFragment)?.updateAlarm()
    }

    fun updateAlarmTabAlarmSound(alarmSound: AlarmSound) {
        (fragments[TAB_ALARM] as? AlarmFragment)?.updateAlarmSound(alarmSound)
    }

    fun updateTimerTabAlarmSound(alarmSound: AlarmSound) {
        (fragments[TAB_TIMER] as? TimerFragment)?.updateAlarmSound(alarmSound)
    }

    fun updateTimerPosition(timerId: Int) {
        (fragments[TAB_TIMER] as? TimerFragment)?.updatePosition(timerId)
    }

    fun startStopWatch() {
//        (fragments[TAB_STOPWATCH] as? StopwatchFragment)?.startStopWatch()
    }

}
