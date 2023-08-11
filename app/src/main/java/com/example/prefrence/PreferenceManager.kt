package com.example.prefrence

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager (context: Context){

    lateinit var editor: SharedPreferences.Editor
    val sharedPreferences = context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)

    fun saveGalleryView(isAlbumView: Boolean) {
        editor = sharedPreferences!!.edit()
        editor.putBoolean("galleryView", isAlbumView)
        editor.apply()
    }

    fun getGalleryView(): Boolean {
        return sharedPreferences!!.getBoolean("galleryView", true)
    }

    fun saveSecurityQuestion(question: String?) {
        editor = sharedPreferences!!.edit()
        editor.putString("sec_ques", question)
        editor.apply()
    }

    fun getSecurityQuestion(): String? {
        return sharedPreferences!!.getString("sec_ques", null)
    }

    fun getSecurityAnswer(): String? {
        return sharedPreferences!!.getString("sec_ans", null)
    }

    fun saveSecurityAnswer(answer: String?) {
        editor = sharedPreferences!!.edit()
        editor.putString("sec_ans", answer)
        editor.apply()
    }

    fun savePin(pin: String?) {
        editor = sharedPreferences!!.edit()
        editor.putString("pin", pin)
        editor.apply()
    }

    fun getPin(): String? {
        return sharedPreferences!!.getString("pin", null)
    }

    fun pinSet(isSeteed: Boolean) {
        editor = sharedPreferences!!.edit()
        editor.putBoolean("pinSet", isSeteed)
        editor.apply()
    }

    fun isPinSeted(): Boolean {
        return sharedPreferences!!.getBoolean("pinSet", false)
    }
    fun isNightMode(nightMode: Boolean) {
        editor = sharedPreferences!!.edit()
        editor.putBoolean("night_mode", nightMode)
        editor.apply()
    }

    fun checkMode(): Boolean? {
        return sharedPreferences?.getBoolean("night_mode", false)
    }

    fun Analog(): Boolean? {
        return sharedPreferences?.getBoolean("analog_mode", false)
    }
    fun ScreenAnalog(): Boolean? {
        return sharedPreferences?.getBoolean("screen_mode", false)
    }
    fun seconds(): Boolean? {
        return sharedPreferences?.getBoolean("seconds_mode", false)
    }


    fun isalogMode(nightMo: Boolean) {
        editor = sharedPreferences!!.edit()
        editor.putBoolean("analog_mode", nightMo)
        editor.apply()
    }
    fun ScreenalogMode(nightM: Boolean) {
        editor = sharedPreferences!!.edit()
        editor.putBoolean("screen_mode", nightM)
        editor.apply()
    }

    fun issecondsMode(nightMo: Boolean) {
        editor = sharedPreferences!!.edit()
        editor.putBoolean("seconds_mode", nightMo)
        editor.apply()
    }

    fun saveAdsTime(time: Long) {
        editor = sharedPreferences!!.edit()
        editor.putLong("adsTime", time)
        editor.apply()
    }

    fun getAdsTime(): Long {
        return sharedPreferences!!.getLong("adsTime", 0)
    }
}