package com.example.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.extendions.config
import com.example.extendions.showpickSecondsDialo
import com.example.helpers.DEFAULT_MAX_TIMER_REMINDER_SECS
import com.example.newclock.R
import com.example.prefrence.PreferenceManager
import com.simplemobiletools.commons.extensions.formatMinutesToTimeString
import com.simplemobiletools.commons.extensions.formatSecondsToTimeString
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.popup_layout.*
import kotlinx.android.synthetic.main.popup_layout.view.*


class SettingActivity : AppCompatActivity() {

    private var audioManager: AudioManager? = null
    private lateinit var preferenceManager: PreferenceManager
    private var setanalogclock:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        preferenceManager = PreferenceManager(this)

        if (preferenceManager.checkMode()!!) {
            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE
        } else {
            window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (preferenceManager.checkMode()!!) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        this.window.statusBarColor=resources.getColor(R.color.color_primary)
        setSupportActionBar(setting_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        updateSnoozeText()
        updateTimerMaxReminderText()

        initControls()

        setting_toolbar.getNavigationIcon()?.setColorFilter(ContextCompat.getColor(this, R.color.text_color), PorterDuff.Mode.SRC_ATOP);
        setting_toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        silence_rel.setOnClickListener{
            showpickSecondsDialo(config.timerMaxReminderSecs, true, true) {
                config.timerMaxReminderSecs = if (it != 0) it else DEFAULT_MAX_TIMER_REMINDER_SECS
                updateTimerMaxReminderText()
            }
        }
        snooze_rel.setOnClickListener{
            showpickSecondsDialo(config.snoozeTime * MINUTE_SECONDS, true) {
                config.snoozeTime = it / MINUTE_SECONDS
                updateSnoozeText()
            }
        }
        change_date_time.setOnClickListener {
            startActivity( Intent(android.provider.Settings.ACTION_DATE_SETTINGS))
        }
        if (preferenceManager.checkMode()!!) {
            preferenceManager.isNightMode(true)

        } else {
            preferenceManager.isNightMode(false)


        }
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            nightmode_switch.setChecked(true)


        nightmode_switch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            // do something, the isChecked will be
            // true if the switch is in the On position
            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                preferenceManager.isNightMode(true)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                preferenceManager.isNightMode(false)
            }
        })


    }

    private fun updateSnoozeText() {
        snooze_time.text = formatMinutesToTimeString(config.snoozeTime)
    }
    private fun updateTimerMaxReminderText() {
        snooze_silence_time.text = formatSecondsToTimeString(config.timerMaxReminderSecs)
    }
    private fun initControls() {


        try {

            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            alarm_volume_seekbar.setMax(audioManager!!.getStreamMaxVolume(AudioManager.STREAM_ALARM))
            alarm_volume_seekbar.setProgress(audioManager!!.getStreamVolume(AudioManager.STREAM_ALARM))
            alarm_volume_seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(arg0: SeekBar) {

                }
                override fun onStartTrackingTouch(arg0: SeekBar) {

                }
                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                    audioManager!!.setStreamVolume(AudioManager.STREAM_ALARM,
                        progress, 0)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }


        st_analog.setOnClickListener {
           clockpopupdialog()

        }

        screen_rel.setOnClickListener {
            ScreenpopupDialog()
        }

        if (preferenceManager.seconds()!!) {
            preferenceManager.issecondsMode(true)
            Display_seconds_switch.setChecked(true)
        }else{
            preferenceManager.issecondsMode(false)
            Display_seconds_switch.setChecked(false)
        }

        Display_seconds_switch.setOnCheckedChangeListener{ buttonView, isChecked ->

            if (isChecked){
                preferenceManager.issecondsMode(true)
                Display_seconds_switch.setChecked(true)

            }else{
                preferenceManager.issecondsMode(false)
                Display_seconds_switch.setChecked(false)

            }
        }

    }



    @SuppressLint("ResourceAsColor")
    override fun onResume() {
        super.onResume()

        if (preferenceManager.Analog()!!) {
            preferenceManager.isalogMode(true)
            analog.text= "Analong"


        }else{
            preferenceManager.isalogMode(false)
            analog.text = "Digital"

        }

        if (preferenceManager.ScreenAnalog()!!) {
            preferenceManager.ScreenalogMode(true)
            digital.text= "Analong"
//            digital.setTextColor(R.color.purple_200);
//            style.setTextColor(R.color.purple_200);
//            display_seconds.setTextColor(R.color.purple_200);

        }else{
            preferenceManager.ScreenalogMode(false)
            digital.text = "Digital"
//            digital.setTextColor(R.color.tab_indicator_color);
//            style.setTextColor(R.color.tab_indicator_color);
//            display_seconds.setTextColor(R.color.tab_indicator_color);

        }
    }

    private fun clockpopupdialog() {
        val popupWindow = PopupWindow(this)
        popupWindow.contentView = LayoutInflater.from(this).inflate(R.layout.popup_layout,null,false)

        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(this. findViewById(R.id.st_analog), +100, -50)
        val popup_analog_dialog = popupWindow.contentView.popup_analog_dialog
        val popup_digital_dialog = popupWindow.contentView.popup_digital_dialog

        if (preferenceManager.Analog()!!) {
            preferenceManager.isalogMode(true)
            analog.text= "Analong"
            popup_analog_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popup_digital_dialog.setBackgroundColor(Color.TRANSPARENT)

        }else{
            preferenceManager.isalogMode(false)
            analog.text = "Digital"
            popup_digital_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popup_analog_dialog.setBackgroundColor(Color.TRANSPARENT)
        }


        popup_analog_dialog.setOnClickListener {
            preferenceManager.isalogMode(true)
            analog.text = "Analong"
            popup_analog_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popup_digital_dialog.setBackgroundColor(Color.TRANSPARENT)
            popupWindow.dismiss()


        }
        popup_digital_dialog.setOnClickListener {
            preferenceManager.isalogMode(false)
             analog.text = "Digital"
            popup_digital_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popup_analog_dialog.setBackgroundColor(Color.TRANSPARENT)
            popupWindow.dismiss()

        }


    }

    private fun ScreenpopupDialog() {
        val popupWindow = PopupWindow(this)
        popupWindow.contentView = LayoutInflater.from(this).inflate(R.layout.popup_layout,null,false)

        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(this. findViewById(R.id.screen_rel), +100, -50)
        val popupsc_analog_dialog = popupWindow.contentView.popup_analog_dialog
        val popupsc_digital_dialog = popupWindow.contentView.popup_digital_dialog


        if (preferenceManager.ScreenAnalog()!!) {
            preferenceManager.ScreenalogMode(true)
            digital.text= "Analong"
            popupsc_analog_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popupsc_digital_dialog.setBackgroundColor(Color.TRANSPARENT)

        }else{
            preferenceManager.ScreenalogMode(false)
            digital.text = "Digital"
            popupsc_digital_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popupsc_analog_dialog.setBackgroundColor(Color.TRANSPARENT)
        }


        popupsc_analog_dialog.setOnClickListener {
            preferenceManager.ScreenalogMode(true)
            digital.text = "Analong"
            popupsc_analog_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popupsc_digital_dialog.setBackgroundColor(Color.TRANSPARENT)
            popupWindow.dismiss()
        }
        popupsc_digital_dialog.setOnClickListener {
            preferenceManager.ScreenalogMode(false)
            digital.text = "Digital"
            popupsc_digital_dialog.setBackgroundColor(getResources().getColor(R.color.tab_indicator_color))
            popupsc_analog_dialog.setBackgroundColor(Color.TRANSPARENT)
            popupWindow.dismiss()
        }

    }


//    val html = ("<font color=" +resources.getColor(R.color.cur_available_color).toString() + ">CURR_AVBL-0004 </font><br>" +
//            "<font color=" + resources.getColor(R.color.not_available_color).toString() + "> No more booking</font><br>"+
//            "<font color=" + resources.getColor(R.color.cur_available_color).toString() + "> (1 day ago)</font>")
//
//    val htm = ("<font color=" +resources.getColor(R.color.cur_available_color).toString() + ">CURR_AVBL-0004 </font><br>" +
//            "<font color=" + resources.getColor(R.color.available_text_color).toString() + ">  Available</font><br>"+
//            "<font color=" + resources.getColor(R.color.cur_available_color).toString() + "> (1 day ago)</font>")


//    view.no_available_text.text = Html.fromHtml(html)
//    view.second_available.text = Html.fromHtml(htm)
//    view.third_available.text = Html.fromHtml(htm)






}