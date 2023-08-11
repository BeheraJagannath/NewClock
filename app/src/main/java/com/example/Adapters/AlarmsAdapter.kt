package com.example.Adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.Activities.SimpleActivity
import com.example.dialogs.AlarmSoundDialog
import com.example.dialogs.ConformDialog
import com.example.extendions.*
import com.example.helpers.PICK_AUDIO_FILE_INTENT_ID
import com.example.helpers.TODAY_BIT
import com.example.helpers.TOMORROW_BIT
import com.example.helpers.getCurrentDayMinutes
import com.example.interfaces.ToggleAlarmInterface
import com.example.models.Alarm
import com.example.newclock.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.AlarmSound
import kotlinx.android.synthetic.main.item_alarm.view.*
import kotlinx.android.synthetic.main.item_alarm.view.edit_alarm_days_holder
import kotlinx.android.synthetic.main.item_alarm.view.edit_alarm_sound
import kotlinx.android.synthetic.main.item_alarm.view.edit_alarm_vibrate_holder


class AlarmsAdapter(activity: SimpleActivity, var alarms: ArrayList<Alarm>, val toggleAlarmInterface: ToggleAlarmInterface,
                    recyclerView: RecyclerView, itemClick: (Any) -> Unit, ) : MyRecyclerAdapter(activity, recyclerView, itemClick) {

    var value: Boolean = false

    override fun getActionMenuId() = R.menu.alarms_menu

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_delete -> ConfirmDelete()
            R.id.cab_select_all -> selectAll()
        }
    }

    override fun getSelectableItemCount() = alarms.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = alarms.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = alarms.indexOfFirst { it.id == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_alarm, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.bindView(alarm, true, true) { itemView, layoutPosition ->
            setupView(itemView, alarm,position)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = alarms.size

    fun updateItems(newItems: ArrayList<Alarm>) {
        alarms = newItems
        notifyDataSetChanged()
        finishActMode()
    }



    fun ConfirmDelete() {


        val itemsCnt = selectedKeys.size

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
   private fun SingleDeleteItem(position: Int) {

       activity.runOnUiThread {
           removesingle(position)
           activity.dbHelper.SingleDelete(alarms[position].id.toString())
           alarms.removeAt(position)
           notifyDataSetChanged()

       }



   }

    private fun deleteItems() {
        val alarmsToRemove = ArrayList<Alarm>()
        val positions = getSelectedItemPositions()
        getSelectedItems().forEach {
            alarmsToRemove.add(it)
        }
        activity.runOnUiThread {
            alarms.removeAll(alarmsToRemove)
            removeSelectedItems(positions)
            activity.dbHelper.deleteAlarms(alarmsToRemove)

        }

    }
    fun clearSelections() {
        notifyDataSetChanged()
    }

    private fun getSelectedItems() = alarms.filter { selectedKeys.contains(it.id) } as ArrayList<Alarm>

    @SuppressLint("ResourceAsColor")
    private fun setupView(view: View, alarm: Alarm, position: Int) {
        val isSelected = selectedKeys.contains(alarm.id)
        view.apply {


            val dayLetters = activity.resources.getStringArray(R.array.week_days_short).toList() as ArrayList<String>
            val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
            if (activity.config.isSundayFirst) {
                dayIndexes.moveLastItemToFront()
            }

            dayIndexes.forEach {
                val pow = Math.pow(2.0, it.toDouble()).toInt()
                val day = activity.layoutInflater.inflate(R.layout.alarm_day, edit_alarm_days_holder, false) as TextView
                day.text = dayLetters[it]

                val isDayChecked = alarm.days > 0 && alarm.days and pow != 0
                day.background = getProperDayDrawable(isDayChecked)
                if (isDayChecked){
                    day.setTextColor(R.color.clock_bg)
                }

                day.setOnClickListener {
                    if (alarm.days < 0) {
                        alarm.days = 0
                    }

                    val selectDay = alarm.days and pow == 0
                    if (selectDay) {
                        alarm.days = alarm.days.addBit(pow)
                        alarm_days.text = activity.getAlarmSelectedDaysString(alarm.days)
                        storeDataInDatabase(alarm)

                    } else {
                        alarm.days = alarm.days.removeBit(pow)
                        alarm_days.text = activity.getAlarmSelectedDaysString(alarm.days)
                        storeDataInDatabase(alarm)

                    }
                    day.background = getProperDayDrawable(selectDay)
                    if (selectDay){
                    day.setTextColor(R.color.clock_bg)
                    }
                        checkDaylessAlarm(alarm)

                }

                edit_alarm_days_holder.addView(day)

                if (alarm.days <= 0) {
                    alarm.days = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                        TODAY_BIT
                    } else {
                        TOMORROW_BIT
                    }
                }


                storeDataInDatabase(alarm)


            }

//  Default alarm
            default_text.text = alarm.soundTitle
            edit_alarm_sound.setOnClickListener{
                AlarmSoundDialog(activity, alarm.soundUri, AudioManager.STREAM_ALARM, PICK_AUDIO_FILE_INTENT_ID, RingtoneManager.TYPE_RINGTONE , true,
                    onAlarmPicked = {
                        if (it != null) {
                            updateSelectedAlarmSound(it,view, alarm)
                            storeDataInDatabase(alarm)

                        }
                    }, onAlarmSoundDeleted = {
                         if (alarm.soundUri == it.uri) {
                            val defaultAlarm = context.getDefaultAlarmSound(RingtoneManager.TYPE_ALARM)
                            updateSelectedAlarmSound(defaultAlarm,view ,alarm)
                             storeDataInDatabase(alarm)
                        }
                        activity.checkAlarmsWithDeletedSoundUri(it.uri)
                    })



            }
// delete single item

            delete.setOnClickListener{
                Handler().postDelayed({
                    SingleDeleteItem(position)
                }, 200)


            }

            visible.setOnClickListener{
                if (!invisible_relative.isVisible){
                    invisible_relative.visibility =View.VISIBLE
                    visible.setImageResource(R.drawable.ic_colapse_vector)
                    alarm_time.isEnabled=true

                    value = true
                }else{
                    invisible_relative.visibility =View.GONE
                    visible.setImageResource(R.drawable.ic_alarm_item_visible)
                    value = false
                    alarm_time.isEnabled=false
                }


            }

//            if (vibrant_circle.drawable ==context.resources.getDrawable(R.drawable.ic_vibrant_check_circle) && alarm.vibrate){
//                vibrant_circle.setImageResource(R.drawable.ic_vibrant_check_circle)
//                alarm.vibrate = true
//
//
//            }else{
//                vibrant_circle.setImageResource(R.drawable.ic_vibrant_circle)
//                alarm.vibrate = false
//
//            }
//


//  Vibrant
            if (alarm.vibrate ){
                vibrant_circle.setChecked(true)
                storeDataInDatabase(alarm)

            }else{
                vibrant_circle.setChecked(false)
                storeDataInDatabase(alarm)

            }
            vibrant_circle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    alarm.vibrate = vibrant_circle.isChecked
                    vibrant_circle.setChecked(true)
                    storeDataInDatabase(alarm)


                }else{
                    vibrant_circle.setChecked(false)
                    storeDataInDatabase(alarm)

                }
            })

            edit_alarm_vibrate_holder.setOnClickListener{
//                vibrant_circle.toggle()
//                alarm.vibrate = vibrant_circle.isChecked
//                storeDataInDatabase(alarm)

                if (!vibrant_circle.isChecked()) {
                    alarm.vibrate = vibrant_circle.isChecked
                    vibrant_circle.setChecked(true)
                    storeDataInDatabase(alarm)
                } else {
                    vibrant_circle.setChecked(false)
                    storeDataInDatabase(alarm)
                }



//                if (vibrant_circle.drawable.constantState==resources.getDrawable(R.drawable.ic_vibrant_circle).constantState){
//                    vibrant_circle.setImageResource(R.drawable.ic_vibrant_check_circle)
//                    alarm.vibrate
//
//                    storeDataInDatabase(alarm)
//                    value = true
//                }else{
//                    vibrant_circle.setImageResource(R.drawable.ic_vibrant_circle)
//                    alarm.vibrate = false
//                    storeDataInDatabase(alarm)
//
//                    value = false
//                }

            }
            add_level.setOnClickListener{

                val dialog = Dialog(activity, R.style.Dialog_Theme)
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.add_level)
                dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)

                val level_ok = dialog.findViewById<TextView>(R.id.level_ok)
                val edit_alarm = dialog.findViewById<TextInputEditText>(R.id.edit_alarm)
                level_ok.setOnClickListener {
                    alarm.label = edit_alarm.value
                    notifyDataSetChanged()
                    dialog.dismiss()


                }
                val cancel = dialog.findViewById<TextView>(R.id.level_cancel)
                cancel.setOnClickListener {
                    dialog.dismiss()

                }


                if (dialog.window != null) {
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                }

                dialog.show()

                storeDataInDatabase(alarm)

            }


            alarm_time.setOnClickListener{

                val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                    .setTitleText("SELECT TIME")
                    .setHour(12)
                    .setMinute(10)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setTheme(R.style.TimePicker)
                    .build()

                materialTimePicker.show(activity.supportFragmentManager, "MainActivity")


                materialTimePicker.addOnPositiveButtonClickListener {

                        alarm.timeInMinutes = materialTimePicker.hour * 60 +materialTimePicker.minute
                        storeDataInDatabase(alarm)
                        updateAlarmTime(view,alarm)
                        enablealaram(alarm,view,context)



                }


            }
            updateAlarmTime(view,alarm)
            alarm_frame.isSelected = isSelected
            alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)
            alarm_days.text = activity.getAlarmSelectedDaysString(alarm.days)
            edit_text.text = alarm.label

            if (edit_text.text.toString().isEmpty()){

                edit_text.text = "Alarm Label"

            }else{
                edit_text.text = alarm.label


            }

//            alarm_label.text = alarm.label
//            alarm_label.setTextColor(textColor)
//            alarm_label.beVisibleIf(alarm.label.isNotEmpty())

            alarm_switch.isChecked = alarm.isEnabled
//            alarm_switch.setColors(textColor, properPrimaryColor, backgroundColor)
            alarm_switch.setOnClickListener {
                enablealaram(alarm,view,context)

            }
            switchcheck(view,alarm)


            val layoutParams = alarm_switch.layoutParams as RelativeLayout.LayoutParams
//            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, if (alarm_label.isVisible()) alarm_label.id else alarm_days.id)
        }
    }

    private fun enablealaram(alarm: Alarm, view: View, context: Context) {
        if (alarm.days > 0) {
            if (activity.config.wasAlarmWarningShown) {
                toggleAlarmInterface.alarmToggled(alarm.id, view.alarm_switch.isChecked)
            } else {
//                ConfirmationDialog(activity, messageId = R.string.alarm_warning, positive = R.string.ok, negative = 0) {
                    activity.config.wasAlarmWarningShown = true
                    toggleAlarmInterface.alarmToggled(alarm.id, view.alarm_switch.isChecked)
//                }
            }
        } else if (alarm.days == TODAY_BIT) {
            if (alarm.timeInMinutes <= getCurrentDayMinutes()) {
                alarm.days = TOMORROW_BIT
                view.alarm_days.text = resources.getString(R.string.tomorrow)
            }
            activity.dbHelper.updateAlarm(alarm)
            context.scheduleNextAlarm(alarm, true)
            toggleAlarmInterface.alarmToggled(alarm.id, view.alarm_switch.isChecked)
        } else if (alarm.days == TOMORROW_BIT) {
            toggleAlarmInterface.alarmToggled(alarm.id,view. alarm_switch.isChecked)
        } else if (view.alarm_switch.isChecked) {
            activity.toast(R.string.no_days_selected)
            view.alarm_switch.isChecked = false
        } else {
            toggleAlarmInterface.alarmToggled(alarm.id,view. alarm_switch.isChecked)
        }
        switchcheck(view,alarm)

    }


     private fun updateAlarmTime(view: View, alarm: Alarm) {
        view.alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)
        checkDaylessAlarm(alarm)
    }

    private fun storeDataInDatabase(alarm: Alarm) {
        var alarmId = alarm.id
        activity.handleNotificationPermission {
            if (it) {
                if (alarm.id == 0) {
                    alarmId = activity.dbHelper.insertAlarm(alarm)
                    if (alarmId == -1) {
                        activity.toast(R.string.unknown_error_occurred)
                    }
                } else {
                    if (!activity.dbHelper.updateAlarm(alarm)) {
                        activity.toast(R.string.unknown_error_occurred)
                    }
                }

                activity.config.alarmLastConfig = alarm

            } else {
                activity.toast(R.string.no_post_notifications_permissions)
            }
        }
    }

    private fun switchcheck(view: View, alarm: Alarm) {
        if (view.alarm_switch.isChecked && alarm.isEnabled){
            view.alarm_frame.alpha = 1F

        }else{
            view.alarm_frame.alpha = 0.5F

        }
    }

     fun updateSelectedAlarmSound(alarmSound: AlarmSound, view: View, alarm: Alarm) {
        alarm.soundTitle = alarmSound.title
        alarm.soundUri = alarmSound.uri
        view.default_text.text = alarmSound.title
    }

    private fun checkDaylessAlarm(alarm: Alarm) {
        if (alarm.days <= 0) {
            val textId = if (alarm.timeInMinutes > getCurrentDayMinutes()) {
                R.string.today
            } else {
                R.string.tomorrow
            }

//            view.edit_alarm_dayless_label.text = "(${activity.getString(textId)})"
        }
//        view.edit_alarm_dayless_label.beVisibleIf(alarm.days <= 0).
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = activity.resources.getDrawable(drawableId)
        return drawable
    }
}