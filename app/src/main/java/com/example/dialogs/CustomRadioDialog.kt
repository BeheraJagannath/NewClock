package com.example.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DAY_SECONDS
import com.simplemobiletools.commons.helpers.HOUR_SECONDS
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.android.synthetic.main.custom_radio_dialog.view.*

@SuppressLint("ResourceAsColor")
class CustomRadioDialog(val activity: Activity, val selectedSeconds: Int = 0, val showSeconds: Boolean = false, val callback: (minutes: Int) -> Unit) {
    private var dialog: AlertDialog? = null
    private var view = activity.layoutInflater.inflate(com.example.newclock.R.layout.custom_radio_dialog, null)

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmReminder() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                    val custom_button = alertDialog .getButton(DialogInterface.BUTTON_POSITIVE)
                    with(custom_button){
                        setTextColor(com.example.newclock.R.color.text_color)

                    }
                    val custom_button1 = alertDialog .getButton(DialogInterface.BUTTON_NEGATIVE)
                    with(custom_button1){
                        setTextColor(com.example.newclock.R.color.text_color)

                    }
                    alertDialog.showKeyboard(view.findViewById(R.id.dialog_custom_interval_value))
                }
            }

        view.apply {
            dialog_radio_seconds.beVisibleIf(showSeconds)
            when {
                selectedSeconds == 0 -> dialog_radio_view.check(R.id.dialog_radio_minutes)
                selectedSeconds % DAY_SECONDS == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_days)
                    dialog_custom_interval_value.setText((selectedSeconds / DAY_SECONDS).toString())
                }
                selectedSeconds % HOUR_SECONDS == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_hours)
                    dialog_custom_interval_value.setText((selectedSeconds / HOUR_SECONDS).toString())
                }
                selectedSeconds % MINUTE_SECONDS == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_minutes)
                    dialog_custom_interval_value.setText((selectedSeconds / MINUTE_SECONDS).toString())
                }
                else -> {
                    dialog_radio_view.check(R.id.dialog_radio_seconds)
                    dialog_custom_interval_value.setText(selectedSeconds.toString())
                }
            }

            dialog_custom_interval_value.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                        return true
                    }

                    return false
                }
            })
        }
    }

    private fun confirmReminder() {
        val value = view.dialog_custom_interval_value.value
        val multiplier = getMultiplier(view.dialog_radio_view.checkedRadioButtonId)
        val minutes = Integer.valueOf(if (value.isEmpty()) "0" else value)
        callback(minutes * multiplier)
        activity.hideKeyboard()
        dialog?.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        R.id.dialog_radio_days -> DAY_SECONDS
        R.id.dialog_radio_hours -> HOUR_SECONDS
        R.id.dialog_radio_minutes -> MINUTE_SECONDS
        else -> 1
    }
}
