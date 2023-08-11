package com.example.dialogs

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import com.example.newclock.R
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_message.view.*


//class ConformDialog {
//}

class ConformDialog(activity: Activity, message: String = "", messageId: Int = R.string.proceed_with_deletion,
                    positive: Int = R.string.yes, negative: Int = R.string.no, val cancelOnTouchOutside: Boolean = true, val callback: () -> Unit, ) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_message, null)
        view.total.text = if (message.isEmpty()) activity.resources.getString(messageId) else message

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { dialog, which -> dialogConfirmed()
            }

        if (negative != 0) {
                builder!!.setNegativeButton(negative, null)
        }

        builder.apply {
            activity.setupDialogStuff(view, this, cancelOnTouchOutside = cancelOnTouchOutside) { alertDialog ->
                dialog = alertDialog
                val button = alertDialog .getButton(DialogInterface.BUTTON_POSITIVE)
                with(button){
                    setTextColor(Color.WHITE)

                }
                val button1 = alertDialog .getButton(DialogInterface.BUTTON_NEGATIVE)
                with(button1){
                    setTextColor(Color.WHITE)

                }
            }
        }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}
