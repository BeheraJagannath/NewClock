package com.example.Service

import android.app.IntentService
import android.content.Intent
import com.example.extendions.config
import com.example.extendions.dbHelper
import com.example.extendions.hideNotification
import com.example.extendions.setupAlarmClock
import com.example.helpers.ALARM_ID
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS

class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        val id = intent!!.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        setupAlarmClock(alarm, config.snoozeTime * MINUTE_SECONDS)
    }
}
