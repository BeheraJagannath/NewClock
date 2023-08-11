package com.example.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.example.Activities.SimpleActivity
import com.example.Adapters.TimerAdapter
import com.example.dialogs.EditTimerDialog
import com.example.extendions.config
import com.example.extendions.createNewTimer
import com.example.extendions.timerHelper
import com.example.helpers.DisabledItemChangeAnimator
import com.example.models.Timer
import com.example.models.TimerEvent
import com.example.newclock.R
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.fragment_blank.*
import kotlinx.android.synthetic.main.fragment_blank.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
class TimerFragment : Fragment() {
    private val INVALID_POSITION = -1
    private lateinit var view: ViewGroup
    private lateinit var timerAdapter: TimerAdapter
    private var timerPositionToScrollTo = INVALID_POSITION
    private var currentEditAlarmDialog: EditTimerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

         view = ( inflater.inflate(R.layout.fragment_blank, container, false)as ViewGroup) .apply{

             timers_list.itemAnimator = DisabledItemChangeAnimator()
             timer_start.setOnClickListener {
                 activity?.run {
                     hideKeyboard()
                     recycler_open.visibility = VISIBLE
                     timer_linear.visibility = GONE

                     openEditTimer(createNewTimer())

                 }
             }

             new_timer_add.setOnClickListener{
                 recycler_open.visibility = GONE
                 timer_linear.visibility = VISIBLE

             }
             open_recycler.setOnClickListener{
                 recycler_open.visibility = VISIBLE
                 timer_linear.visibility = GONE
             }

         }

        initAdapter()
        refreshTimers()

        if (context?.config?.appRunCount == 1) {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshTimers()
            }, 1000)
        }

        return view
    }



private fun initAdapter() {
        timerAdapter = TimerAdapter(requireActivity() as SimpleActivity, view.timers_list, ::refreshTimers, ::openEditTimer)
        view.timers_list.adapter = timerAdapter


}

override fun onResume() {
    super.onResume()

        initAdapter()

        refreshTimers()

}

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }
    private fun storeStateVariables() {

    }

private fun refreshTimers(scrollToLatest: Boolean = false) {
    activity?.timerHelper?.getTimers { timers ->
        activity?.runOnUiThread {
            timerAdapter.submitList(timers) {
                getView()?.post {
                    if (timerPositionToScrollTo != INVALID_POSITION && timerAdapter.itemCount > timerPositionToScrollTo) {
                        view.timers_list.scrollToPosition(timerPositionToScrollTo)
                        timerPositionToScrollTo = INVALID_POSITION
                    } else if (scrollToLatest) {
                        view.timers_list.scrollToPosition(timers.lastIndex)
                    }
                }
            }
        }
    }
}
@Subscribe(threadMode = ThreadMode.MAIN)
fun onMessageEvent(event: TimerEvent.Refresh) {
    refreshTimers()
}

fun updateAlarmSound(alarmSound: AlarmSound) {
    currentEditAlarmDialog?.updateAlarmSound(alarmSound)
}

fun updatePosition(timerId: Int) {
    activity?.timerHelper?.getTimers { timers ->
        val position = timers.indexOfFirst { it.id == timerId }
        if (position != INVALID_POSITION) {
            activity?.runOnUiThread {
                if (timerAdapter.itemCount > position) {
                    view.timers_list.scrollToPosition(position)
                } else {
                    timerPositionToScrollTo = position
                }
            }
        }
    }
}

private fun openEditTimer(timer: Timer) {
    val hours = my_time_picker_hours.value
    val minutes = my_time_picker_minutes.value
    val seconds = my_time_picker_seconds.value
    timer.seconds = hours * 3600 + minutes * 60 + seconds


    activity?.timerHelper?.insertOrUpdateTimer(timer) {
        activity?.config?.timerLastConfig = timer
    }
    initAdapter()
    refreshTimers()
    timerAdapter.notifyDataSetChanged()
    timerAdapter.notifyItemInserted(timer.seconds)

}
}