package com.example.Activities

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.example.extendions.*
import com.example.helpers.ALARM_ID
import com.example.helpers.getPassedSeconds
import com.example.models.Alarm
import com.example.newclock.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import com.simplemobiletools.commons.helpers.SILENT
import com.simplemobiletools.commons.helpers.isOreoMr1Plus
import com.simplemobiletools.commons.helpers.isOreoPlus
import kotlinx.android.synthetic.main.activity_reminder.*

class ReminderActivity : SimpleActivity() {

    private val INCREASE_VOLUME_DELAY = 3000L
    private val increaseVolumeHandler = Handler()
    private val maxReminderDurationHandler = Handler()
    private val swipeGuideFadeHandler = Handler()
    private var isAlarmReminder = false
    private var didVibrate = false
    private var wasAlarmSnoozed = false
    private var alarm: Alarm? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var lastVolumeValue = 0.1f
    private var dragDownX = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        showOverLockscreen()

        val id = intent.getIntExtra(ALARM_ID, -1)
        isAlarmReminder = id != -1
        if (id != -1) {
            alarm = dbHelper.getAlarmWithId(id) ?: return
        }

        val label = if (isAlarmReminder) {
            if (alarm!!.label.isEmpty()) {
                getString(R.string.alarm)
            } else {
                alarm!!.label
            }
        } else {
            getString(R.string.timer)
        }

        reminder_title.text = label
        reminder_text.text = if (isAlarmReminder) getFormattedTime(getPassedSeconds(), false, false) else getString(R.string.time_expired)

        val maxDuration = if (isAlarmReminder) config.alarmMaxReminderSecs else config.timerMaxReminderSecs
        maxReminderDurationHandler.postDelayed({
            finishActivity()
        }, maxDuration * 1000L)

        setupButtons()
        setupEffects()

    }

    override fun onResume() {
        super.onResume()
//        setupToolbar(reminder_toolbar)
    }

    private fun setupButtons() {
        if (isAlarmReminder) {
            setupAlarmButtons()
        } else {
            setupTimerButtons()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAlarmButtons() {
        reminder_stop.beGone()
        reminder_draggable_background.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulsing_animation))
        reminder_draggable_background.applyColorFilter(getProperPrimaryColor())

//        val textColor = getProperTextColor()
//        reminder_dismiss.applyColorFilter(textColor)
//        reminder_draggable.applyColorFilter(textColor)
//        reminder_snooze.applyColorFilter(textColor)

        var minDragX = 0f
        var maxDragX = 0f
        var initialDraggableX = 0f

        reminder_dismiss.onGlobalLayout {
            minDragX = reminder_snooze.left.toFloat()
            maxDragX = reminder_dismiss.left.toFloat()
            initialDraggableX = reminder_draggable.left.toFloat()
        }

        reminder_draggable.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragDownX = event.x
                    reminder_draggable_background.animate().alpha(0f)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragDownX = 0f
                    if (!didVibrate) {
                        reminder_draggable.animate().x(initialDraggableX).withEndAction {
                            reminder_draggable_background.animate().alpha(0.2f)
                        }

                        reminder_guide.animate().alpha(1f).start()
                        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
                        swipeGuideFadeHandler.postDelayed({
                            reminder_guide.animate().alpha(0f).start()
                        }, 2000L)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    reminder_draggable.x = Math.min(maxDragX, Math.max(minDragX, event.rawX - dragDownX))
                    if (reminder_draggable.x >= maxDragX - 50f) {
                        if (!didVibrate) {
                            reminder_draggable.performHapticFeedback()
                            didVibrate = true
                            finishActivity()
                        }

                        if (isOreoPlus()) {
                            notificationManager.cancelAll()
                        }
                    } else if (reminder_draggable.x <= minDragX + 50f) {
                        if (!didVibrate) {
                            reminder_draggable.performHapticFeedback()
                            didVibrate = true
                            snoozeAlarm()
                        }

                        if (isOreoPlus()) {
                            notificationManager.cancelAll()
                        }
                    }
                }
            }
            true
        }
    }

    private fun setupTimerButtons() {
        reminder_stop.background = resources.getColoredDrawableWithColor(R.drawable.circle_background_filled, getProperPrimaryColor())
        arrayOf(reminder_snooze, reminder_draggable_background, reminder_draggable, reminder_dismiss).forEach {
            it.beGone()
        }

        reminder_stop.setOnClickListener {
            finishActivity()
        }
    }

    private fun setupEffects() {
        if (!isAlarmReminder || !config.increaseVolumeGradually) {
            lastVolumeValue = 1f
        }

        val doVibrate = if (alarm != null) alarm!!.vibrate else config.timerVibrate
        if (doVibrate && isOreoPlus()) {
            val pattern = LongArray(2) { 500 }
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }

        val soundUri = if (alarm != null) {
            alarm!!.soundUri
        } else {
            config.timerSoundUri
        }

        if (soundUri != SILENT) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_ALARM)
                    setDataSource(this@ReminderActivity, Uri.parse(soundUri))
                    setVolume(lastVolumeValue, lastVolumeValue)
                    isLooping = true
                    prepare()
                    start()
                }

                if (config.increaseVolumeGradually) {
                    scheduleVolumeIncrease()
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun scheduleVolumeIncrease() {
        increaseVolumeHandler.postDelayed({
            lastVolumeValue = Math.min(lastVolumeValue + 0.1f, 1f)
            mediaPlayer?.setVolume(lastVolumeValue, lastVolumeValue)
            scheduleVolumeIncrease()
        }, INCREASE_VOLUME_DELAY)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        finishActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        increaseVolumeHandler.removeCallbacksAndMessages(null)
        maxReminderDurationHandler.removeCallbacksAndMessages(null)
        swipeGuideFadeHandler.removeCallbacksAndMessages(null)
        destroyEffects()
    }

    private fun destroyEffects() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun snoozeAlarm() {
        destroyEffects()
        if (config.useSameSnooze) {
            setupAlarmClock(alarm!!, config.snoozeTime * MINUTE_SECONDS)
            wasAlarmSnoozed = true
            finishActivity()
        } else {
            showpickSecondsDialo(config.snoozeTime * MINUTE_SECONDS, true, cancelCallback = { finishActivity() }) {
                config.snoozeTime = it / MINUTE_SECONDS
                setupAlarmClock(alarm!!, it)
                wasAlarmSnoozed = true
                finishActivity()
            }
        }
    }

    private fun finishActivity() {
        if (!wasAlarmSnoozed && alarm != null && alarm!!.days > 0) {
            scheduleNextAlarm(alarm!!, false)
        }

        destroyEffects()
        finish()
        overridePendingTransition(0, 0)
    }

    private fun showOverLockscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        }
    }
}