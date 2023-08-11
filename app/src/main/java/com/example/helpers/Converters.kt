package com.example.helpers

import androidx.room.TypeConverter
import com.example.extendions.gson
import com.example.models.StateWrapper
import com.example.models.TimerState

class Converters {

    @TypeConverter
    fun jsonToTimerState(value: String): TimerState {
        return try {
            gson.fromJson(value, StateWrapper::class.java).state
        } catch (e: Exception) {
            TimerState.Idle
        }
    }

    @TypeConverter
    fun timerStateToJson(state: TimerState) = gson.toJson(StateWrapper(state))
}
