package com.example.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.javamodel.CityTimeZone
import com.example.javamodel.CityTimeZoneDbDAO
import com.example.javamodel.Helper
import com.example.newclock.R
import com.zerobranch.layout.SwipeLayout
import com.zerobranch.layout.SwipeLayout.SwipeActionsListener
class SelectedCityZoneAdapter(
    private var filteredTimeZoneList: ArrayList<CityTimeZone>,
    private var context: Context) :
    RecyclerView.Adapter<SelectedCityZoneAdapter.ViewHolder?>(){
    var selected_items: SparseBooleanArray
    var current_selected_idx = -1
    var deleteImageMap = HashMap<Int, String>()
    var cityTimeZoneDbDAO:CityTimeZoneDbDAO?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_city_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val cityTimeZone = filteredTimeZoneList[position]
        holder.cityName.text = cityTimeZone.name
        holder.cityTime.text = Helper.convertTimeZone(cityTimeZone.name)
        holder.timeDifference.text = Helper.getTimeDifference(cityTimeZone.name)
        holder.am_pm.text = Helper.getTimeAMPM(cityTimeZone.name)

        toggleCheckedIcon(holder, position)
        displayImage(holder, cityTimeZone, position)

        holder.swipeLayout.setOnActionsListener(object : SwipeActionsListener {
            override fun onOpen(direction: Int, isContinuous: Boolean) {
                if (direction == SwipeLayout.RIGHT) {
                    if (filteredTimeZoneList!=null){
                        cityTimeZoneDbDAO!!.deleteSelectedCityTimeZone(cityTimeZone)
                        filteredTimeZoneList.removeAt(position)
                        notifyDataSetChanged()
                        notifyItemRemoved(position)

                    }

                } else if (direction == SwipeLayout.LEFT) {
                    if (filteredTimeZoneList!=null){
                        cityTimeZoneDbDAO!!.deleteSelectedCityTimeZone(cityTimeZone)
                        filteredTimeZoneList.removeAt(position)
                        notifyDataSetChanged()
                        notifyItemRemoved(position)

                    }

                }
            }

            override fun onClose() {
                // the main view has returned to the default state
            }
        })
    }

    private fun displayImage(holder: ViewHolder, videomodel: CityTimeZone, position: Int) {
        if (filteredTimeZoneList[position].countryCode!= null) {
//            holder.imageView.colorFilter = null
            holder.all_list.visibility = View.VISIBLE
        } else {
            holder.all_list.visibility = View.GONE
        }

    }

    private fun toggleCheckedIcon(holder: ViewHolder, position: Int) {
        if (selected_items.get(position, false)) {

            holder.selected_rel.visibility = View.VISIBLE
            holder.selected_rel.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.fade_in))

        } else {

            holder.selected_rel.visibility = View.GONE

            if (current_selected_idx == position) resetCurrentIndex()
        }

    }
    private fun resetCurrentIndex() {
        current_selected_idx = -1
    }


    override fun getItemCount(): Int {
        return filteredTimeZoneList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cityName: TextView
        var cityTime: TextView
        var timeDifference: TextView
        var am_pm: TextView
        var all_list: RelativeLayout
        var selected_rel: RelativeLayout
        var swipeLayout: SwipeLayout


        init {
            cityName = itemView.findViewById(R.id.city_tv_first_activity)
            cityTime = itemView.findViewById(R.id.time_tv_first_activity)
            timeDifference = itemView.findViewById(R.id.time_diff_tv)
            am_pm = itemView.findViewById(R.id.am_pm)
            all_list = itemView.findViewById(R.id.all_list)
            selected_rel = itemView.findViewById(R.id.selected_rel)
            swipeLayout = itemView.findViewById(R.id.swipe_layout)
        }
    }

    init {
        this.context = context
        this.filteredTimeZoneList = filteredTimeZoneList
        selected_items = SparseBooleanArray()
        this.cityTimeZoneDbDAO = CityTimeZoneDbDAO(context)
    }

    fun getSelectedItemCount(): Int {
        return selected_items.size()
    }

    fun clearSelections() {
        selected_items.clear()
        notifyDataSetChanged()
    }

    fun getDeleteItems(): HashMap<Int, String> {
        return deleteImageMap
    }

    fun toggleSelection(pos: Int) {
        current_selected_idx = pos
        if (selected_items[pos, false]) {
            selected_items.delete(pos)
            deleteImageMap.remove(pos)
        } else {
            selected_items.put(pos, true)
            deleteImageMap.put(pos, filteredTimeZoneList[pos].name)
            Log.d("add", "toggleCheckedIcon: $pos")
        }
        notifyItemChanged(pos)
    }
    fun removeData(position: Int) {
        filteredTimeZoneList.removeAt(position)
//        resetCurrentIndex()
        notifyItemRemoved(position)
    }

    fun getSelectedIds(): SparseBooleanArray? {
        return selected_items
    }


}