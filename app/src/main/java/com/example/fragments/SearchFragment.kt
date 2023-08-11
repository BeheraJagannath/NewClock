package com.example.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.util.SparseBooleanArray
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.Activities.SearchActivity
import com.example.Adapters.SelectedCityZoneAdapter
import com.example.extendions.getNextAlarm
import com.example.helpers.getPassedSeconds
import com.example.javamodel.*
import com.example.newclock.R
import com.example.prefrence.PreferenceManager
import com.simplemobiletools.commons.extensions.beVisibleIf
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment(){
    private val SECOND_ACTIVITY_REQUEST_CODE = 0
    private var cityTimeZoneArrayList: ArrayList<CityTimeZone>? = null
    private var selectedCityAdapter: SelectedCityZoneAdapter? = null
    private var iCityTimeZoneDAO: ICityTimeZoneDAO? = null
    private var thread: Thread? = null
    private var delay = 0

    @SuppressLint("NewApi")
    private var calendar = Calendar.getInstance()
    private var passedSeconds = 0

    private val updateHandler = Handler()
    private val ONE_SECOND = 1000L

    lateinit var view: ViewGroup
    private var mActionMode: ActionMode ? = null
    private var selected: SparseBooleanArray? = null
    private var i = 0
    lateinit var preferenceManager: PreferenceManager



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false) as ViewGroup
        setupDateTime(view)



        preferenceManager = PreferenceManager(requireActivity())
        twentyfourhour(view)

        iCityTimeZoneDAO = CityTimeZoneDbDAO(requireActivity())
        delay = 1000 //1000 ms = 1 sec

        if (savedInstanceState != null) {
            cityTimeZoneArrayList = savedInstanceState.getParcelableArrayList("cityTimeZone")

            view. main_activity_List.setLayoutManager(LinearLayoutManager(requireContext()))
            selectedCityAdapter = SelectedCityZoneAdapter(cityTimeZoneArrayList!!,requireContext())
            view. main_activity_List.setAdapter(selectedCityAdapter)


        } else {
            cityTimeZoneArrayList = ArrayList()
        }


        //Updating Times

        //Updating Times
        thread = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        sleep(delay.toLong())
                        updateTime()

                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        thread?.start()


        view. select_city_button.setOnClickListener {
            val intent = Intent(requireActivity(), SearchActivity::class.java)
            startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE)
        }

       setAlaogand(view)


        return view
    }

//    private fun Swiperecycler(view: ViewGroup) {
//
//       view. main_activity_List.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
//        view. main_activity_List.disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
//        view. main_activity_List.behindSwipedItemBackgroundColor = ContextCompat.getColor(requireContext(), R.color.tab_indicator_color)
//        view. main_activity_List.behindSwipedItemIconDrawableId = R.drawable.ic_item_delete
//        view.main_activity_List.behindSwipedItemBackgroundColor = ContextCompat.getColor(requireContext(), R.color.tab_indicator_color)
//        view.main_activity_List.behindSwipedItemIconSecondaryDrawableId = R.drawable.ic_item_delete
//
//        view.main_activity_List.swipeListener = onItemSwipeListener
//        view.main_activity_List.dragListener = onItemDragListener
//        view.main_activity_List.scrollListener = onListScrollListener
//
//    }


    private fun twentyfourhour(view: ViewGroup) {
        if (view.clock_time.is24HourModeEnabled){
            view.am_pm.visibility =View.GONE

        }else{
            view.am_pm.visibility =View.VISIBLE

        }

    }

    override fun onResume() {
        super.onResume()
        //Load items from database when the app is resumed
        loadItem()
//        view.let { setupDateTime(it) }
        setupDateTime(view)

        setAlaogand(view)
        setTextclockSeconds(view)


    }


    private fun setTextclockSeconds(view: ViewGroup) {
        if (preferenceManager.seconds()!!){
           view.clock_time.setFormat12Hour("h:mm:ss")
           view.clock_time.setFormat24Hour("k:mm:ss")
            twentyfourhour(view)
        } else {
           view.clock_time.setFormat24Hour("k:mm")
           view.clock_time.setFormat12Hour("h:mm")
            twentyfourhour(view)
        }


    }

    private fun setAlaogand(view: ViewGroup) {
        if (preferenceManager.Analog()!!){
            view.analog.visibility = View.VISIBLE
            view.clock_time_rel .visibility  = View.GONE

        }else{
            view.analog.visibility = View.GONE
            view.clock_time_rel .visibility  = View.VISIBLE


        }

    }



    @SuppressLint("NewApi")
    private fun setupDateTime(view: ViewGroup) {
        calendar = Calendar.getInstance()
        passedSeconds = getPassedSeconds()
        updateCurrentTime(view)
        updateDate()
        updateAlarm()
    }

    private fun updateCurrentTime(view: ViewGroup) {
        val hours = (passedSeconds / 3600) % 24
        val minutes = (passedSeconds / 60) % 60
        val seconds = passedSeconds % 60

//        if (!DateFormat.is24HourFormat(requireContext())) {
//            view.clock_time.textSize = resources.getDimension(R.dimen.clock_text_size_smaller) / resources.displayMetrics.density
//        }

        if (seconds == 0) {
            if (hours == 0 && minutes == 0) {
                updateDate()
            }

        }

        updateHandler.postDelayed({
            passedSeconds++
            updateCurrentTime(view)
        }, ONE_SECOND)

    }
    @SuppressLint("NewApi")
    private fun updateDate() {
        calendar = Calendar.getInstance()

    }
    fun updateAlarm() {
        view.apply {
            val nextAlarm = requireContext().getNextAlarm()
            clock_alarm.beVisibleIf(nextAlarm.isNotEmpty())
            clock_alarm.text = nextAlarm
        }
    }


    private fun updateTime() {
        val size = cityTimeZoneArrayList!!.size
        for (i in 0 until size) {
            val timeZone = cityTimeZoneArrayList!![i].name
            cityTimeZoneArrayList!![i].time = Helper.convertTimeZoneToTime(timeZone)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("cityTimeZone", cityTimeZoneArrayList)
    }

    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacksAndMessages(null)
        saveItem()
        mActionMode = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Thread.interrupted()
        mActionMode = null
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val size = data?.getIntExtra("Size", 0)
                if (size == 0) {
                    showMessage("0 Cities Selected!")
                } else {
                    if (cityTimeZoneArrayList!!.size == 0) {
                        cityTimeZoneArrayList = data?.getParcelableArrayListExtra("result")
                        main_activity_List.setLayoutManager(LinearLayoutManager(requireContext()))
                        selectedCityAdapter = SelectedCityZoneAdapter(cityTimeZoneArrayList!!, requireContext())
                        main_activity_List.setAdapter(selectedCityAdapter)
                    } else {
                        val temp = data?.getParcelableArrayListExtra<CityTimeZone>("result")
                        for (i in temp!!.indices) {
                            if (cityTimeZoneArrayList!!.contains(temp[i])) {
                                showMessage("The City has already been selected.")
                            } else {
                                cityTimeZoneArrayList!!.add(temp[i])
                                selectedCityAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                showMessage("Failure!")
            }
        }
    }

    private fun showMessage(message: String) {
        val toast: Toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun showCitiesOnListView() {
        cityTimeZoneArrayList = Helper.mergeCityTimeZoneArrayLists(cityTimeZoneArrayList,
            iCityTimeZoneDAO!!.selectedCityTimeZones)
        main_activity_List.setLayoutManager(LinearLayoutManager(requireContext()))
        selectedCityAdapter = SelectedCityZoneAdapter(cityTimeZoneArrayList!!, requireContext())
        main_activity_List.setAdapter(selectedCityAdapter)
    }

    private fun loadItem() {
        val dbCityTimeZoneArrayList:ArrayList<CityTimeZone>
        dbCityTimeZoneArrayList = iCityTimeZoneDAO!!.selectedCityTimeZones
        val size = dbCityTimeZoneArrayList.size
        if (size > 0) {
            showCitiesOnListView()
//            showMessage("$size Items loaded from the database.")
        } else {
//            showMessage("No Items in the database to load.")
        }
    }

    private fun saveItem() {
        val dbCityTimeZoneArrayList: ArrayList<CityTimeZone>
        val ctz: ArrayList<CityTimeZone> = Helper.getCheckedCities(cityTimeZoneArrayList)
        dbCityTimeZoneArrayList =
            iCityTimeZoneDAO!!.selectedCityTimeZones //get already existing cities in database
        if (ctz.size > 0) {
            for (i in ctz.indices) {
                if (dbCityTimeZoneArrayList.contains(ctz[i]) == false) { //only add if it does not exist in database already
                    val success = iCityTimeZoneDAO!!.addSelectedCityTimeZone(ctz[i])
                    if (!success) {
//                        showMessage("Selected City already exists in the Database. Cannot add.")
                    }
                }
            }
        }
    }




//    override fun OnfileClicked(view: View?, timeZone: CityTimeZone?, position: Int) {
//        if (selectedCityAdapter?.getSelectedItemCount()!! >0){
//            view?.let {
//                onListItemSelect(it,timeZone!!,position)
//            }
//        }else {
//            selectedCityAdapter?.removeData(position)
//            iCityTimeZoneDAO?.deleteSelectedCityTimeZone(timeZone)
//
//        }
//
//
//    }
//    override fun OnLongClick(view: View?, timeZone: CityTimeZone?, position: Int) {
//        view?.let {
//            onListItemSelect(it,timeZone!!,position)
//        }
//
//    }
//
//    @SuppressLint("NewApi")
//    private fun onListItemSelect(view: View, citytime: CityTimeZone, position: Int) {
//        selectedCityAdapter?.toggleSelection(position) //Toggle the selection
//        val hasCheckedItems: Boolean = selectedCityAdapter?.getSelectedItemCount()!! > 0
//        //Check if any items are already selected or not
//        if (hasCheckedItems && mActionMode == null) // there are some selected items, start the actionMode
//            mActionMode = requireActivity().startActionMode(object : ActionMode.Callback {
//                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
//                    mode.menuInflater.inflate(R.menu.alarms_menu, menu)
//                    return true
//                }
//
//                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
//                    return false
//                }
//
//                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
//                    return when (item.itemId) {
//                        R.id.cab_delete -> {
//                            multipledelete(position,view,citytime,mode)
//                            true
//                        }
//                        R.id.cab_select_all -> {
//                            mode.finish()
//                            true
//                        }
//                        else -> false
//                    }
//                }
//
//                @SuppressLint("NewApi")
//                override fun onDestroyActionMode(mode: ActionMode) {
//                    selectedCityAdapter?.clearSelections()
//                    mActionMode = null
//                }
//            }) else if (!hasCheckedItems && mActionMode != null) // there no selected items, finish the actionMode
//            mActionMode!!.finish()
//        val totalImage: Int = selectedCityAdapter!!.getItemCount()
//        if (mActionMode != null) //set action mode title on item selection
//            mActionMode!!.setTitle((selectedCityAdapter?.getSelectedItemCount().toString() + "/" + totalImage))
//    }
//
//    private fun multipledelete(position: Int, view: View, citytime: CityTimeZone, mode: ActionMode) {
//        val builder = AlertDialog.Builder(requireContext())
//        val view = LayoutInflater.from(requireContext()).inflate(R.layout.delete_layout, view.findViewById(R.id.delete_constraint))
//        builder.setView(view)
//        val alertDialog = builder.create()
//        alertDialog.show()
//        if (alertDialog.window != null) {
//            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
//        }
//        view.findViewById<View>(R.id.Yes).setOnClickListener {
//
//            selectedCityAdapter?.removeData(position)
//            iCityTimeZoneDAO?.deleteSelectedCityTimeZone(citytime)
//            mode.finish()
//
//            alertDialog.dismiss()
//        }
//        view.findViewById<View>(R.id.No).setOnClickListener {
//            alertDialog.dismiss()
//            mode.finish()
//        }
//
//    }


}



