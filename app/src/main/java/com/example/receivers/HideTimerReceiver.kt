package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.extendions.hideTimerNotification
import com.example.helpers.INVALID_TIMER_ID
import com.example.helpers.TIMER_ID
import com.example.models.TimerEvent
import org.greenrobot.eventbus.EventBus

class HideTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getIntExtra(TIMER_ID, INVALID_TIMER_ID)
        context.hideTimerNotification(timerId)
        EventBus.getDefault().post(TimerEvent.Reset(timerId ))
        EventBus.getDefault().post(TimerEvent.Delete(timerId ))
    }
}
