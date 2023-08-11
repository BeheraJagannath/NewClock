package com.example.Adapters
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Activities.SimpleActivity
import com.example.extendions.formatStopwatchTime

import com.example.models.Lap
import com.example.newclock.R
import kotlinx.android.synthetic.main.stopwatch_item.view.*
import java.util.*

class StopwatchAdapter(activity: SimpleActivity, var laps: ArrayList<Lap>, recyclerView: RecyclerView, itemClick: (Any) -> Unit) :
        MyRecyclerAdapter(activity, recyclerView, itemClick) {
    private var lastLapTimeView: TextView? = null
    private var lastTotalTimeView: TextView? = null
    private var lastLapId = 0

    override fun getActionMenuId() = 0

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = laps.size

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemSelectionKey(position: Int) = laps.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = laps.indexOfFirst { it.id == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.stopwatch_item, parent)

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        val lap = laps[position]
        holder.bindView(lap, false, false) { itemView, layoutPosition ->
            setupView(itemView, lap)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = laps.size

    fun updateItems(newItems: ArrayList<Lap>) {
        lastLapId = 0
        laps = newItems.clone() as ArrayList<Lap>

        notifyDataSetChanged()
        finishActMode()
    }

    fun updateLastField(lapTime: Long, totalTime: Long) {
        lastLapTimeView?.text = lapTime.formatStopwatchTime(false)
        lastTotalTimeView?.text = totalTime.formatStopwatchTime(false)
    }

    private fun setupView(view: View, lap: Lap) {
        view.apply {
            lap_order.text = lap.id.toString()



            lap_lap_time.text = lap.lapTime.formatStopwatchTime(false)



            lap_total_time.text = lap.totalTime.formatStopwatchTime(false)


            if (lap.id > lastLapId) {
                lastLapTimeView = lap_lap_time
                lastTotalTimeView = lap_total_time
                lastLapId = lap.id
            }
        }
    }
}
