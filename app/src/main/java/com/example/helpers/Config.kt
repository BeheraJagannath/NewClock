package com.example.helpers

import android.content.Context
import android.media.RingtoneManager
import com.example.extendions.gson
import com.example.models.Alarm
import com.example.models.Timer
import com.example.models.TimerState
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapterFactory
import com.simplemobiletools.commons.extensions.getDefaultAlarmSound
import com.simplemobiletools.commons.extensions.getDefaultAlarmTitle
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.helpers.ENABLE_PULL_TO_REFRESH
import com.simplemobiletools.commons.helpers.SORT_DESCENDING

class Config(context: Context):BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var selectedTimeZones: Set<String>
        get() = prefs.getStringSet(SELECTED_TIME_ZONES, HashSet())!!
        set(selectedTimeZones) = prefs.edit().putStringSet(SELECTED_TIME_ZONES, selectedTimeZones).apply()


    var toggleStopwatch: Boolean
        get() = prefs.getBoolean(TOGGLE_STOPWATCH, false)
        set(toggleStopwatch) = prefs.edit().putBoolean(TOGGLE_STOPWATCH, toggleStopwatch).apply()

    var alarmMaxReminderSecs: Int
        get() = prefs.getInt(ALARM_MAX_REMINDER_SECS, DEFAULT_MAX_ALARM_REMINDER_SECS)
        set(alarmMaxReminderSecs) = prefs.edit().putInt(ALARM_MAX_REMINDER_SECS, alarmMaxReminderSecs).apply()

    var alarmSort: Int
        get() = prefs.getInt(ALARMS_SORT_BY, SORT_BY_CREATION_ORDER)
        set(alarmSort) = prefs.edit().putInt(ALARMS_SORT_BY, alarmSort).apply()

    var alarmLastConfig: Alarm?
        get() = prefs.getString(ALARM_LAST_CONFIG, null)?.let { lastAlarm ->
            gson.fromJson(lastAlarm, Alarm::class.java)
        }
        set(alarm) = prefs.edit().putString(ALARM_LAST_CONFIG, gson.toJson(alarm)).apply()

    var timerSeconds: Int
        get() = prefs.getInt(TIMER_SECONDS, 300)
        set(lastTimerSeconds) = prefs.edit().putInt(TIMER_SECONDS, lastTimerSeconds).apply()

    var timerVibrate: Boolean
        get() = prefs.getBoolean(TIMER_VIBRATE, false)
        set(timerVibrate) = prefs.edit().putBoolean(TIMER_VIBRATE, timerVibrate).apply()

    var timerSoundUri: String
        get() = prefs.getString(TIMER_SOUND_URI, context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM).uri)!!
        set(timerSoundUri) = prefs.edit().putString(TIMER_SOUND_URI, timerSoundUri).apply()

    var timerSoundTitle: String
        get() = prefs.getString(TIMER_SOUND_TITLE, context.getDefaultAlarmTitle(RingtoneManager.TYPE_ALARM))!!
        set(timerSoundTitle) = prefs.edit().putString(TIMER_SOUND_TITLE, timerSoundTitle).apply()

    var timerLabel: String?
        get() = prefs.getString(TIMER_LABEL, null)
        set(label) = prefs.edit().putString(TIMER_LABEL, label).apply()

    var timerChannelId: String?
        get() = prefs.getString(TIMER_CHANNEL_ID, null)
        set(id) = prefs.edit().putString(TIMER_CHANNEL_ID, id).apply()

    var timerLastConfig: Timer?
        get() = prefs.getString(TIMER_LAST_CONFIG, null)?.let { lastAlarm ->
            gson.fromJson(lastAlarm, Timer::class.java)
        }
        set(alarm) = prefs.edit().putString(TIMER_LAST_CONFIG, gson.toJson(alarm)).apply()

    var editedTimeZoneTitles: Set<String>
        get() = prefs.getStringSet(EDITED_TIME_ZONE_TITLES, HashSet())!!
        set(editedTimeZoneTitles) = prefs.edit().putStringSet(EDITED_TIME_ZONE_TITLES, editedTimeZoneTitles).apply()

    var timerMaxReminderSecs: Int
        get() = prefs.getInt(TIMER_MAX_REMINDER_SECS, DEFAULT_MAX_TIMER_REMINDER_SECS)
        set(timerMaxReminderSecs) = prefs.edit().putInt(TIMER_MAX_REMINDER_SECS, timerMaxReminderSecs).apply()

    var increaseVolumeGradually: Boolean
        get() = prefs.getBoolean(INCREASE_VOLUME_GRADUALLY, true)
        set(increaseVolumeGradually) = prefs.edit().putBoolean(INCREASE_VOLUME_GRADUALLY, increaseVolumeGradually).apply()

     var enablePullToRefres: Boolean
        get() = prefs.getBoolean(ENABLE_PULL_TO_REFRESH, true)
        set(enablePullToRefres) = prefs.edit().putBoolean(ENABLE_PULL_TO_REFRESH, enablePullToRefres).apply()


}