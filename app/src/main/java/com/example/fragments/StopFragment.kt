package com.example.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.example.Stopwatch.StopwatchService
import com.example.newclock.R
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_stop.*
import kotlinx.android.synthetic.main.fragment_stop.view.*
import org.greenrobot.eventbus.EventBus

class StopFragment : Fragment(), StopwatchService.Listener, ServiceConnection {

    private var textColorPrimarySubscription: Disposable? = null

    private var service: StopwatchService? = null
    lateinit var view: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stop, container, false) as ViewGroup

        view.reset.setOnClickListener {
            if (service != null) {
                service?.reset()
                view.lap_scroll.visibility = View.GONE
            }
        }
        view.reset.setClickable(false)

        view.toggle.setOnClickListener {
            if (service != null) {
                service?.toggle()
            }
        }

        view.lap.setOnClickListener {
            if (service != null) {
                service?.lap()
                view.lap_scroll.visibility = View.VISIBLE
            }
        }


        try {
            textColorPrimarySubscription = get()
                .textColorPrimary()
                .subscribe { integer: Int? ->
                    for (i in 0 until laps.getChildCount()) {
                        val layout = laps.getChildAt(i) as LinearLayout
                        for (i2 in 0 until layout.childCount) {
//                            (layout.getChildAt(i2) as TextView).setTextColor(integer!!)
                        }
                    }
                }
        } catch (e: Exception) {
        }


        val intent = Intent(requireContext(), StopwatchService::class.java)
        requireActivity().startService(intent)
        requireActivity().bindService(intent, this, Context.BIND_AUTO_CREATE)

        return view

    }

//    override fun onDestroy() {
//        textColorPrimarySubscription!!.dispose()
//            time.unsubscribe()
//            if (service != null) {
//                service!!.setListener(null)
//                val isRunning = service!!.isRunning
//                requireContext().unbindService(this)
//                if (!isRunning) requireContext().stopService(Intent(requireContext(), StopwatchService::class.java))
//            }
//
//        super.onDestroy()
//    }


    override fun onStateChanged(isRunning: Boolean) {
        try {
            if (isRunning) {
                view.reset.isClickable = false
                view.reset.animate().alpha(0f).start()
                view.lap.visibility = View.VISIBLE
                val drawable = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.ic_pause_vector)
                if (drawable != null) {
                    view.toggle.setImageDrawable(drawable)
                    drawable.start()
                } else toggle.setImageResource(R.drawable.ic_play_vector)


            } else {
                if (service!!.elapsedTime > 0) {
                    view.reset.isClickable = true
                    view.reset.visibility = View.VISIBLE
                    view.lap.visibility = View.GONE
                    view.reset.animate().alpha(1f).start()

                } else
                    view.lap.visibility = View.GONE

                val drawable = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.ic_play_vector)
                if (drawable != null) {
                    toggle.setImageDrawable(drawable)
                    drawable.start()
                } else{
                    toggle.setImageResource(R.drawable.ic_pause_vector)

                }


            }

        } catch (e: Exception) {

        }

    }

    override fun onReset() {
        view.laps.removeAllViews()
        view.time.setMaxProgress(0)
        view.time.setReferenceProgress(0)
        view.reset.isClickable = false
        view.reset.alpha = 0f
        view.lap.visibility = View.GONE

    }

    override fun onTick(currentTime: Long, text: String?) {
        if (service != null) {
            view.time.setText(text!!)
//            view.time.setProgress(currentTime)
//            time.setProgress(currentTime - if (service!!.lastLapTime === 0) currentTime else service!!.lastLapTime)

            view.time.setProgress(currentTime - if (service!!.lastLapTime ==null ) currentTime  else service!!.lastLapTime)

        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onLap(lapNum: Int, lapTime: Long, lastLapTime: Long, lapDiff: Long) {
        if (lastLapTime == 0L) time.setMaxProgress(lapDiff) else time.setReferenceProgress(lapDiff)
        val layout = LinearLayout(context)
        val number = TextView(context)
        number.text = getString(R.string.title_lap_number, lapNum)
        number.setTextColor(resources.getColor(R.color.text_color))
        layout.addView(number)
        val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.weight = 1f
        val lap = TextView(context)
        lap.layoutParams = layoutParams
        lap.gravity = GravityCompat.END
        lap.text = getString(R.string.title_lap_time,
            com.example.Stopwatch.FormatUtils.formatMillis(lapDiff))
        lap.setTextColor(resources.getColor(R.color.text_color))
        layout.addView(lap)
        val total = TextView(context)
        total.layoutParams = layoutParams
        total.gravity = GravityCompat.END
        total.text = getString(R.string.title_total_time,
            com.example.Stopwatch.FormatUtils.formatMillis(lapTime))
        total.setTextColor(resources.getColor(R.color.text_color))
        layout.addView(total)
        view.laps.addView(layout, 0)
    }

    override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
        if (iBinder != null && iBinder is StopwatchService.LocalBinder) {
            service = (iBinder).getService()
            onStateChanged(service!!.isRunning())
            onTick(0, "00:00:00")
            service!!.setListener(this)
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        service = null
    }


}