package com.example.fragments

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.Activities.SimpleActivity
import com.example.Adapters.StopwatchAdapter
import com.example.extendions.config
import com.example.extendions.formatStopwatchTime
import com.example.helpers.Stopwatch
import com.example.newclock.R
import com.example.prefrence.MyCountDownTimer
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.fragment_stopwatch.*
import kotlinx.android.synthetic.main.fragment_stopwatch.view.*

class StopwatchFragment : Fragment() {
    private var storedTextColor = 0

    lateinit var stopwatchAdapter: StopwatchAdapter
    lateinit var view: ViewGroup
    private var animator: ValueAnimator? = null
    private var state: Stopwatch.State? = null



        var countDownTimer: MyCountDownTimer? = null

    private val timeCountInMilliSeconds = (1 * 60000).toLong()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment


        view =
            (inflater.inflate(R.layout.fragment_stopwatch, container, false) as ViewGroup).apply {

                stopwatch_play_pause.setOnClickListener {
                    togglePlayPause()

                }

                stopwatch_reset.setOnClickListener {
                    resetStopwatch()


                }

                stopwatch_lap.setOnClickListener {
                    countDownTimer?.resume()

                    stopwatch_list.visibility = VISIBLE
                    Stopwatch.lap()
                    updateLaps()

                }

                stopwatchAdapter =
                    StopwatchAdapter(activity as SimpleActivity, ArrayList(), stopwatch_list) {

                    }

                stopwatch_list.adapter = stopwatchAdapter
            }


        return view
    }


    override fun onResume() {
        super.onResume()

        Stopwatch.addUpdateListener(updateListener)
        updateLaps()


        if (requireContext().config.toggleStopwatch) {
            requireContext().config.toggleStopwatch = false
            startStopWatch()
        }
    }

    override fun onPause() {
        super.onPause()

        Stopwatch.removeUpdateListener(updateListener)
    }


    private fun updateIcons(state: Stopwatch.State) {
        try {
            val drawableId = if (state == Stopwatch.State.RUNNING) {
                R.drawable.ic_pause_vector
            } else {
                R.drawable.ic_play_vector
            }

            val iconColor =
                if (requireContext().getProperPrimaryColor() == Color.WHITE) Color.BLACK else Color.WHITE
            view.stopwatch_play_pause.setImageDrawable(resources.getColoredDrawableWithColor(drawableId!!.toInt(),
                iconColor))

        }catch (e:Exception){

        }

    }
    private fun togglePlayPause() {

        (activity as SimpleActivity).handleNotificationPermission {
            if (it) {
                Stopwatch.toggle(true)

                if (state != Stopwatch.State.RUNNING ) {
                    state = Stopwatch.State.RUNNING

                    setProgressBarValues()
                    startCountDownTimer(state!!)
//                    countDownTimer?.resume()

                }
//                else if (state == Stopwatch.State.RUNNING && state ==Stopwatch.State.PAUSED && state != Stopwatch.State.STOPPED) {
//
//                    countDownTimer?.resume()
//
//
//                } else if (state ==Stopwatch.State.RESUME && state != Stopwatch.State.STOPPED) {
//                    countDownTimer?.pause()
//                }
                else {
                    state = Stopwatch.State.STOPPED

                    stopCountDownTimer()


                }


            } else {
                activity?.toast(R.string.no_post_notifications_permissions)
            }
        }

    }


    private fun startCountDownTimer(state: Stopwatch.State) {


        countDownTimer = object : MyCountDownTimer(timeCountInMilliSeconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!pause){
                    progressBarCircle.progress = (millisUntilFinished / 1000).toInt()
                }

            }

            override fun onFinish() {

                setProgressBarValues()
                // changing the timer status to stopped
                state == Stopwatch.State.STOPPED


            }
        }.start() as MyCountDownTimer?
        countDownTimer!!.start()
    }

    private fun stopCountDownTimer() {
        countDownTimer!!.pause()
    }

    private fun setProgressBarValues() {

        progressBarCircle.max = timeCountInMilliSeconds.toInt() / 1000
        progressBarCircle.progress = timeCountInMilliSeconds.toInt() / 1000

    }


    private fun updateDisplayedText(totalTime: Long, lapTime: Long, useLongerMSFormat: Boolean) {
        view.stopwatch_time.text = totalTime.formatStopwatchTime(useLongerMSFormat)
        if (Stopwatch.laps.isNotEmpty() && lapTime != -1L) {
            stopwatchAdapter.updateLastField(lapTime, totalTime)

        }
    }


    private fun resetStopwatch() {
        Stopwatch.reset()
        setProgressBarValues()


        updateLaps()
        view.apply {
            stopwatch_reset.beGone()
            stopwatch_lap.beGone()
            stopwatch_list.beGone()
            stopwatch_time.text = 1L.formatStopwatchTime(true)

        }
    }


    fun startStopWatch() {
        if (Stopwatch.state == Stopwatch.State.STOPPED) {
            togglePlayPause()
        }
    }

    private fun updateLaps() {

        stopwatchAdapter.updateItems(Stopwatch.laps)


    }

    private val updateListener = object : Stopwatch.UpdateListener {
        override fun onUpdate(totalTime: Long, lapTime: Long, useLongerMSFormat: Boolean) {
            updateDisplayedText(totalTime, lapTime, useLongerMSFormat)
        }

        override fun onStateChanged(state: Stopwatch.State) {
            updateIcons(state)

            view.stopwatch_lap.beVisibleIf(state == Stopwatch.State.RUNNING)
            view.stopwatch_reset.beVisibleIf(state != Stopwatch.State.STOPPED)
            if (state != Stopwatch.State.RUNNING) {
//                mCurrentPlayTime = animator?.getCurrentPlayTime();
                animator?.pause()
            }
        }
    }

}