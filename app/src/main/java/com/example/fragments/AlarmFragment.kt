package com.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.Activities.SimpleActivity
import com.example.Adapters.AlarmsAdapter

import com.example.extendions.*
import com.example.helpers.*
import com.example.interfaces.ToggleAlarmInterface
import com.example.models.Alarm
import com.example.newclock.MainActivity
import com.example.newclock.R
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_CREATED
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_alarm.view.*

class AlarmFragment : Fragment(), ToggleAlarmInterface {
    private var alarms = ArrayList<Alarm>()

    lateinit var view: ViewGroup
     var adapter: AlarmsAdapter?=null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        view = inflater.inflate(R.layout.fragment_alarm, container, false) as ViewGroup

        return view
    }


    override fun onResume() {
        super.onResume()
        setupViews()

        adapter!!.clearSelections()


    }

    override fun onPause() {
        super.onPause()
    }



    private fun setupViews() {
        view.apply {
            alarm_fab.setOnClickListener {
                val newAlarm = context.createNewAlarm(DEFAULT_ALARM_MINUTES, 0)
//                newAlarm.isEnabled = true
                newAlarm.days = getTomorrowBit()
                openEditAlarm(newAlarm)
            }
        }

        setupAlarms()
    }

    private fun setupAlarms() {
        alarms = context?.dbHelper?.getAlarms() ?: return

        when (requireContext().config.alarmSort) {
            SORT_BY_ALARM_TIME -> alarms.sortBy { it.timeInMinutes }
            SORT_BY_DATE_CREATED -> alarms.sortBy { it.id }
            SORT_BY_DATE_AND_TIME -> alarms.sortWith(compareBy<Alarm> {
                requireContext().firstDayOrder(it.days)
            }.thenBy {
                it.timeInMinutes
            })
        }

        if (context?.getNextAlarm()?.isEmpty() == true) {
            alarms.forEach {
                if (it.days == TODAY_BIT && it.isEnabled && it.timeInMinutes <= getCurrentDayMinutes()) {
                    it.isEnabled = false
                    ensureBackgroundThread {
                        context?.dbHelper?.updateAlarmEnabledState(it.id, false)
                    }
                }
            }
        }

        val currAdapter = view.alarms_list.adapter
        if (currAdapter == null) {
//            AlarmsAdapter(activity as SimpleActivity, alarms, this, view.alarms_list) {
////                openEditAlarm(it as Alarm)
//            }.apply {
//                view.alarms_list.adapter = this
//            }
            adapter= AlarmsAdapter(activity as SimpleActivity, alarms, this, view.alarms_list){}
            view.alarms_list.adapter = adapter
            adapter!!.clearSelections()
        } else {
            (currAdapter as AlarmsAdapter).updateItems(alarms)
        }


    }

    private fun openEditAlarm(alarm: Alarm) {
//        currentEditAlarmDialog = EditAlarmDialog(activity as SimpleActivity, alarm) {
             Addtimepick(alarm)
//            alarm.id = it
//            currentEditAlarmDialog = null
            setupAlarms()
            checkAlarmState(alarm)
//        }
    }

    private fun Addtimepick(alarm: Alarm) {
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("SELECT TIME")
            .setHour(12)
            .setMinute(10)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTheme(R.style.TimePicker)
            .build()

        materialTimePicker.show(requireActivity().supportFragmentManager, "MainActivity")
        materialTimePicker.addOnPositiveButtonClickListener {
            alarm.timeInMinutes = materialTimePicker.hour * 60 +materialTimePicker.minute
            storeDataInDatabase(alarm)
            activity?.getFormattedTime(alarm.timeInMinutes * 60, false, true)
            setupAlarms()
            alarm.isEnabled = true



        }

    }

    private fun storeDataInDatabase(alarm: Alarm) {
        alarm.isEnabled=true
        var alarmId = alarm.id
        (activity as SimpleActivity).handleNotificationPermission {
            if (it) {
                if (alarm.id == 0) {
                    alarmId = activity?.dbHelper?.insertAlarm(alarm)!!
                    if (alarmId == -1) {
                        activity?.toast(R.string.unknown_error_occurred)
                    }
                } else {
                    if (!activity?.dbHelper?.updateAlarm(alarm)!!) {
                        activity?.toast(R.string.unknown_error_occurred)
                    }
                }

                activity?.config?.alarmLastConfig = alarm

            } else {
                activity?.toast(R.string.no_post_notifications_permissions)
            }
        }
        checkDaylessAlarm(alarm)


    }
    private fun checkDaylessAlarm(alarm: Alarm) {
        if (alarm.days <= 0) {
            val textId = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                R.string.today
            } else {
                R.string.today
            }

//            view.edit_alarm_dayless_label.text = "(${activity.getString(textId)})"
        }
//        view.edit_alarm_dayless_label.beVisibleIf(alarm.days <= 0)
    }

    override fun alarmToggled(id: Int, isEnabled: Boolean) {
        (activity as SimpleActivity).handleNotificationPermission {
            if (it) {
                if (requireContext().dbHelper.updateAlarmEnabledState(id, isEnabled)) {
                    val alarm = alarms.firstOrNull { it.id == id } ?: return@handleNotificationPermission
                    alarm.isEnabled = isEnabled
                    checkAlarmState(alarm)
                } else {
                    requireActivity().toast(R.string.unknown_error_occurred)
                }
                requireContext().updateWidgets()
            } else {
                activity?.toast(R.string.no_post_notifications_permissions)
            }
        }

    }

    private fun checkAlarmState(alarm: Alarm) {
        if (alarm.isEnabled) {
            context?.scheduleNextAlarm(alarm, true)
        } else {
            context?.cancelAlarmClock(alarm)
        }
        (activity as? MainActivity)?.updateClockTabAlarm()
    }

    fun updateAlarmSound(alarmSound: AlarmSound) {

    }



}