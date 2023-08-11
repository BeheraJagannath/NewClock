package com.example.prefrence

import android.os.CountDownTimer

//class MycountDownTimer {
//}
open class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
    CountDownTimer(millisInFuture, countDownInterval) {
    // ADDED
    var pause = false
//
//    // ADDED
    fun pause() {
        pause = true
    }

    // ADDED
    fun resume() {
        pause = false
    }

    override fun onTick(millisUntilFinished: Long) {
        val progress = (millisUntilFinished / 100).toInt()

        // ADDED
//        if (!pause) {
////                main_progressBarCircle.setProgress(progress)
//        }
    }

    override fun onFinish() {
        // ADDED
//            isRunning = false;
//            main_progressBarCircle.setProgress(0)
        //            Intent intent = new Intent(getApplicationContext(), GameOver.class);
//            startActivity(intent);
//            finish();
    }
}