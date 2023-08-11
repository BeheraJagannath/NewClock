package com.example.Activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.Adapters.TimeZonesAdapter
import com.example.javamodel.*
import com.example.newclock.R
import com.example.prefrence.PreferenceManager
import kotlinx.android.synthetic.main.activity_search.*
import java.util.*

class SearchActivity : AppCompatActivity() {

    private var cityTimeZoneArrayList: ArrayList<CityTimeZone>? = null
    private var timeZoneAdapter: TimeZonesAdapter? = null
    private var iCityTimeZoneDAO: ICityTimeZoneDAO? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var localBroadcastManager: LocalBroadcastManager? = null
    lateinit var preferenceManager:PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        preferenceManager = PreferenceManager(this)
        iCityTimeZoneDAO = CityTimeZoneDbDAO(this)

        if (preferenceManager.checkMode()!!) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }



        main_toolbar.getNavigationIcon()?.setColorFilter(ContextCompat.getColor(this, R.color.text_color), PorterDuff.Mode.SRC_ATOP);
        main_toolbar.setNavigationOnClickListener{
            onBackPressed()
        }


        this.window.statusBarColor=resources.getColor(R.color.color_primary)




        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadDataFromDb()
            }
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter("DOWNLOAD_COMPLETED")
        localBroadcastManager?.registerReceiver(broadcastReceiver as BroadcastReceiver, intentFilter)


        if (savedInstanceState != null) {
            cityTimeZoneArrayList = savedInstanceState.getParcelableArrayList("cityTimeZone")
        }


        val intent = Intent(this, TimeZoneDLService::class.java)
        startService(intent)
        loadDataFromDb()


        mSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                try {
                    timeZoneAdapter!!.filter.filter(newText);

                }catch (e:Exception){

                }

                return true
            }
        })
        mSearch.setOnSearchClickListener(View.OnClickListener {
            mTextLin.visibility = View.GONE
        })
        mSearch.setOnCloseListener(SearchView.OnCloseListener {
            mTextLin.visibility = View.VISIBLE
            loadDataFromDb()
            false
        })


    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, TimeZoneDLService::class.java)
        startService(intent)
        loadDataFromDb()
    }


    override fun onDestroy() {
        localBroadcastManager!!.unregisterReceiver(broadcastReceiver!!)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("cityTimeZone", cityTimeZoneArrayList)
    }

    private fun prepareResult() {
        val resultIntent = Intent()
        val checkedCities: ArrayList<CityTimeZone>
        checkedCities = Helper.getCheckedCities(cityTimeZoneArrayList)
        resultIntent.putExtra("result", checkedCities)
        resultIntent.putExtra("Size", checkedCities.size)
        setResult(RESULT_OK, resultIntent)
    }

    override fun onBackPressed() {
        prepareResult()
        super.onBackPressed()
    }

    private fun loadDataFromDb() {
        cityTimeZoneArrayList = iCityTimeZoneDAO!!.cityTimeZones
        val size = cityTimeZoneArrayList?.size
        if (size!! > 0) {
            second_activity_list.setLayoutManager(LinearLayoutManager(this))
            timeZoneAdapter = TimeZonesAdapter(
                cityTimeZoneArrayList,
                this)
            second_activity_list.adapter = timeZoneAdapter
            lottie_animation.visibility=View.GONE
            second_activity_list.visibility = View.VISIBLE

        } else {
            lottie_animation.visibility=View.VISIBLE
            second_activity_list.visibility = View.GONE

        }
    }



}


