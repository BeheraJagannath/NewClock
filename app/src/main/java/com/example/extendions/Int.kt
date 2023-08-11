package com.example.extendions

import com.simplemobiletools.commons.extensions.toInt
import java.util.*
import java.util.concurrent.TimeUnit

val Int.secondsToMillis get() = TimeUnit.SECONDS.toMillis(this.toLong())
val Int.millisToSeconds get() = TimeUnit.MILLISECONDS.toSeconds(this.toLong())

fun Int.getDuration(forceShowHours: Boolean = true):String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        sb.append("00:")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}
fun Int.asDuration(forceShowHours: Boolean = false,forceShowMinutes: Boolean = false,forceShowSeconds: Boolean = false): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%2d", hours)).append(" H ")
    } else if (forceShowHours) {
        sb.append("00:")
    }
    if (this >= 3600 / 60) {
        sb.append(String.format(Locale.getDefault(), "%2d", minutes)).append(" M ")
    } else if (forceShowMinutes) {
        sb.append("00:")
    }
        sb.append(String.format(Locale.getDefault(), "%2d", seconds) + " S")


//    sb.append(String.format(Locale.getDefault(), "%2d", minutes))
//    sb.append(String.format(Locale.getDefault(), "%2d", seconds) + " second")

    return sb.toString()
}



