package com.example.Adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.example.Activities.SimpleActivity
import com.example.dialogs.ConformDialog
import com.example.extendions.*
import com.example.models.Timer
import com.example.models.TimerEvent
import com.example.models.TimerState
import com.example.newclock.R
import com.example.prefrence.MyCountDownTimer
import com.simplemobiletools.commons.extensions.beInvisibleIf
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.item_timer.view.*
import org.greenrobot.eventbus.EventBus


class TimerAdapter(
    private val simpleActivity: SimpleActivity,
    recyclerView: MyRecyclerView,
    onRefresh: () -> Unit,
    onItemClick: (Timer) -> Unit,
) : MyRecyclerViewAdapter<Timer>(simpleActivity, recyclerView, diffUtil, onItemClick, onRefresh) {

    var countDownTimer: MyCountDownTimer? = null

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Timer>() {
            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
                return oldItem == newItem
            }
        }
    }

    init {
        setupDragListener(true)
    }
    override fun getActionMenuId() =R.menu.alarms_menu


    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_delete -> askConfirmDelete()
            R.id.cab_select_all -> selectAll()
        }
    }

    override fun getSelectableItemCount() = itemCount

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = getItem(position).id

    override fun getItemKeyPosition(key: Int): Int {
        var position = -1
        for (i in 0 until itemCount) {
            if (key == getItem(i).id) {
                position = i
                break
            }
        }
        return position
    }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_timer, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(getItem(position), true, true) { itemView, _ ->
            setupView(itemView, getItem(position),position)
        }
        bindViewHolder(holder)
    }

    private fun askConfirmDelete() {
        val itemsCnt = selectedKeys.size

        // not sure how we can get UnknownFormatConversionException here, so show the error and hope that someone reports it
        val items = try {
            resources.getQuantityString(R.plurals.delete_messages, itemsCnt, itemsCnt)
        } catch (e: Exception) {
            activity.showErrorToast(e)
            return
        }

        val baseString = R.string.deletion_confirmation
        val question = String.format(resources.getString(baseString), items)

        ConformDialog(activity, question) {
            ensureBackgroundThread {
                deleteItems()
            }
        }
    }

    private fun deleteItems() {
        val positions = getSelectedItemPositions()
        val timersToRemove = positions.map { position ->
            getItem(position)
        }
        activity.runOnUiThread {
            removeSelectedItems(positions)
            timersToRemove.forEach(::deleteTimer)
        }

    }
    private fun singledeleteItems(position: Int, timer: Timer) {
        activity.runOnUiThread {
            removesingle(position)
            deleteTimer(timer)
            notifyDataSetChanged()

        }

    }

    private fun setupView(view: View, timer: Timer, position: Int) {
        view.apply {
            val isSelected = selectedKeys.contains(timer.id)
            timer_frame.isSelected = isSelected
            timer_time.text = when (timer.state) {
                is TimerState.Finished -> 0.getDuration()
                is TimerState.Idle -> timer.seconds.getDuration()
                is TimerState.Paused -> timer.state.tick.getFormattedDuratio()
                is TimerState.Running -> timer.state.tick.getFormattedDuratio()
            }
            totaltiem.text ="Total "+timer.seconds.asDuration(false,false,false)

            timer_reset.setOnClickListener {
                setProgressBarValues(view, timer)
                resetTimer(timer,view)

            }
            single_timer_delete.setOnClickListener {

                singledeleteItems(position,timer)

            }


            timer_play_pause.setOnClickListener {

//                val animator = ValueAnimator.ofInt( 0,  view.timer_progressbar.max)
//                animateProgressbar(timer, view, timer.state, animator)
                (activity as SimpleActivity).handleNotificationPermission {
                    if (it) {
                        when (val state = timer.state) {
                            is TimerState.Idle -> {
                                EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
//                                timer_progressbar.max = timer.seconds.secondsToMillis.toInt()
//                                animator.resume()
                                startCountDownTimer(view,timer)
                                setProgressBarValues(view, timer)
                                countDownTimer?.resume()

                            }
                            is TimerState.Paused -> {
                                EventBus.getDefault().post(TimerEvent.Start(timer.id!!, state.tick))
//                                timer_progressbar.progress =state.tick.toInt()
//                                animateProgressbar(timer, view, state, animator)
//                                animator.resume()
                                countDownTimer?.resume()
                            }
                            is TimerState.Running -> {
                                EventBus.getDefault().post(TimerEvent.Pause(timer.id!!, state.tick))
                                countDownTimer?.pause()
                            }
                            is TimerState.Finished -> {
                                EventBus.getDefault().post(TimerEvent.Start(timer.id!!, timer.seconds.secondsToMillis))
                                setProgressBarValues(view,timer)
                            }

                        }

                        /* call to start the count down timer
                            startCountDownTimer(timer,view)
                            val animator = ValueAnimator.ofInt( 0,  timer_progressbar.getMax())
                            animator.duration =timer.seconds.secondsToMillis
                            animator.addUpdateListener {
                                    animation -> timer_progressbar.setProgress(animation.animatedValue as Int)
                            }
                            animator.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    // start your activity here
                                }
                            })
                            animator.start()*/

                    } else {
                        activity.toast(R.string.no_post_notifications_permissions)
                    }
                }
            }
            val state = timer.state
            val resetPossible = state is TimerState.Running || state is TimerState.Paused || state is TimerState.Finished
            timer_reset.beInvisibleIf(!resetPossible)
            val drawableId = if (state is TimerState.Running) R.drawable.ic_pause_vector else R.drawable.ic_timer_play_vector
            timer_play_pause.setImageDrawable(simpleActivity.resources.getColoredDrawableWithColor(drawableId, textColor))

        }
    }

    private fun startCountDownTimer(view: View,timer: Timer) {

        countDownTimer = object : MyCountDownTimer((timer.seconds* 1000).toLong(), 2) {
            override fun onTick(millisUntilFinished: Long) {
                if (!pause){
                   view. timer_progressbar.progress = (millisUntilFinished / 1000).toInt()
                    Log.d(millisUntilFinished.toString(),"dsfdad")
                }
            }
            override fun onFinish() {
                setProgressBarValues(view,timer)

            }
        }
        countDownTimer?.start()


    }

    private fun setProgressBarValues(view: View, timer: Timer) {
        view.timer_progressbar.max = timer.seconds*1000/1000
        view.timer_progressbar.progress =timer.seconds*1000/1000


    }


    private fun animateProgressbar(
        timer: Timer,
        view: View,
        state: TimerState,
        animator: ValueAnimator,
    ) {

        animator.duration =timer.seconds.secondsToMillis
        animator.addUpdateListener {
                animation -> view.timer_progressbar.setProgress(animation.animatedValue as Int)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                view.timer_progressbar.max = timer.seconds.secondsToMillis.toInt()
                view.timer_progressbar.progress = timer.seconds.secondsToMillis.toInt()
            }

            override fun onAnimationPause(animation: Animator?) {
                super.onAnimationPause(animation)
//                if (state is TimerState.Paused) {
//                    view. timer_progressbar.progress = timer.state.toInt()
                    animation?.pause()
//                }
            }

            override fun onAnimationResume(animation: Animator?) {
                super.onAnimationResume(animation)
//                if (state is TimerState.Running) {
//                    view. timer_progressbar.progress = timer.state.toInt()
                    animation?.resume()
//                }
            }

            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                view. timer_progressbar.max = timer.seconds.secondsToMillis.toInt()
                view. timer_progressbar.progress = timer.seconds.secondsToMillis.toInt()
            }
        })
        animator.start()
    }
    private fun resetTimer(timer: Timer, view: View) {
        countDownTimer?.cancel()
        EventBus.getDefault().post(TimerEvent.Reset(timer.id!!))
        simpleActivity.hideTimerNotification(timer.id!!)
    }

    fun deleteTimer(timer: Timer) {
        EventBus.getDefault().post(TimerEvent.Delete(timer.id!!))
        simpleActivity.hideTimerNotification(timer.id!!)
    }

}

