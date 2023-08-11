package com.example.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.onGlobalLayout
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.radio_group.view.*

@SuppressLint("ResourceAsColor")
class RadioDialog(
    val activity: Activity, val items: ArrayList<RadioItem>, val checkedItemId: Int = -1, val titleId: Int = 0,
    showOKButton: Boolean = false, val cancelCallback: (() -> Unit)? = null, val callback: (newValue: Any) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var wasInit = false
    private var selectedItemId = -1
    private val view = activity.layoutInflater.inflate(com.example.newclock.R.layout.radio_group, null)

    init {

//        val view = activity.layoutInflater.inflate(R.layout.dialog_radio_group, null)
        view.radio_group.apply {
            for (i in 0 until items.size) {
                val radioButton = (activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton).apply {
                    text = items[i].title
                    isChecked = items[i].id == checkedItemId
                    id = i
                    setOnClickListener { itemSelected(i) }
                }

                if (items[i].id == checkedItemId) {
                    selectedItemId = i
                }

                addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        val builder = activity.getAlertDialogBuilder()
            .setOnCancelListener { cancelCallback?.invoke() }

        if (selectedItemId != -1 && showOKButton) {
            builder.setPositiveButton(R.string.cancel) { dialog, which -> itemSelected(selectedItemId)
            }
        }

        builder.apply {
            activity.setupDialogStuff(view, this, titleId) { alertDialog ->
                dialog = alertDialog
                val radio_button = alertDialog .getButton(DialogInterface.BUTTON_POSITIVE)
                with(radio_button){
                    setTextColor(com.example.newclock.R.color.text_color)

                }
            }
        }

        if (selectedItemId != -1) {
            view.dialog_radio_holder.apply {
                onGlobalLayout {
                    scrollY = view.radio_group.findViewById<View>(selectedItemId).bottom - height
                }
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            callback(items[checkedId].value)
            dialog?.dismiss()
        }
    }
}
