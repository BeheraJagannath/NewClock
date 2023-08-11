package com.example.newclock

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import com.example.Activities.SettingActivity
import com.example.Activities.SimpleActivity
import com.example.Adapters.ViewPagerAdapter
import com.example.extendions.config
import com.example.helpers.*
import com.example.prefrence.PreferenceManager
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.screensaver_layout.view.*
import kotlinx.android.synthetic.main.selected_city_item.*
import me.grantland.widget.AutofitHelper

class MainActivity : SimpleActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupTabs()
        initFragments()
        preferenceManager = PreferenceManager(this)

        if (preferenceManager.checkMode()!!) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

//        this.window.statusBarColor=resources.getColor(R.color.black)
        main_toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_navigation_icon)
        setSupportActionBar(main_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        main_toolbar.setNavigationOnClickListener{
            my_drawer_layout.openDrawer(GravityCompat.START)

        }


//        toggle = ActionBarDrawerToggle(this, my_drawer_layout,main_toolbar, R.string.nav_open, R.string.nav_close)
//        my_drawer_layout.addDrawerListener(toggle)
//        toggle.syncState()


        navigation_view.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.settings->{
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
//                    navigation_view.visibility = View.GONE


                }
                R.id.rete_app->Toast.makeText(applicationContext,"Rate this app",Toast.LENGTH_SHORT).show()
                R.id.share_friend->Toast.makeText(applicationContext,"Share with friend",Toast.LENGTH_SHORT).show()
                R.id.contact_us->Toast.makeText(applicationContext,"Contact us",Toast.LENGTH_SHORT).show()
                R.id.privacy_policy->Toast.makeText(applicationContext,"Privacy policy",Toast.LENGTH_SHORT).show()
                R.id.ScreenSaver->{
                    ScreenSaverDialog()
                }
            }
            Handler().postDelayed({
                my_drawer_layout.closeDrawer(GravityCompat.START)
            }, 500)

            true
        }

    }

    private fun ScreenSaverDialog() {
        val dialog = Dialog(this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        dialog.setContentView(R.layout.screensaver_layout)
        dialog.window?.statusBarColor = resources.getColor(R.color.tab_indicator_color)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)

       val okay_text = dialog.findViewById<AnalogClock>(R.id.Screen_analog);
       val cancel_text = dialog.findViewById<RelativeLayout>(R.id.Screen_digital);
       val Screen_relativelayout = dialog.findViewById<CoordinatorLayout>(R.id.Screen_relativelayout);
       val screen_time = dialog.findViewById<TextClock>(R.id.screen_time);
       val screen_am_pm = dialog.findViewById<TextClock>(R.id.screen_am_pm);

        if (preferenceManager.ScreenAnalog()!!){

            okay_text.visibility = View.VISIBLE
            cancel_text .visibility  = View.GONE

        }else{
            okay_text.visibility = View.GONE
            cancel_text .visibility  = View.VISIBLE
        }

        if (preferenceManager.seconds()!!){
            screen_time.setFormat12Hour("h:mm:ss")
            screen_time.setFormat24Hour("k:mm:ss")
            if (screen_time.is24HourModeEnabled){
                screen_am_pm.visibility =View.GONE

            }else{
                screen_am_pm.visibility =View.VISIBLE

            }
        } else {
            screen_time.setFormat24Hour("k:mm")
            screen_time.setFormat12Hour("h:mm")
            if (screen_time.is24HourModeEnabled){
                screen_am_pm.visibility =View.GONE

            }else{
                screen_am_pm.visibility =View.VISIBLE

            }
        }
        Screen_relativelayout.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()



    }

    override fun onResume() {
        super.onResume()
        setupTabColors()
    }

    private fun initFragments() {
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        view_pager.adapter = viewPagerAdapter

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if(position == TAB_ALARM) {
                    toolbar_title.setText("Alarm")
                }else if (position == TAB_CLOCK){
                    toolbar_title.setText("Clock")
                } else if (position == TAB_TIMER){
                    toolbar_title.setText("Timer")
                }else if (position == TAB_STOPWATCH){
                    toolbar_title.setText("Stopwatch")
                }
            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        view_pager.onPageChangeListener {
            main_tabs_holder.getTabAt(it)?.select()
        }

        val tabToOpen = intent.getIntExtra(OPEN_TAB, config.lastUsedViewPagerPage)
        intent.removeExtra(OPEN_TAB)
        if (tabToOpen == TAB_TIMER) {
            val timerId = intent.getIntExtra(TIMER_ID, INVALID_TIMER_ID)
            viewPagerAdapter.updateTimerPosition(timerId)
            viewPagerAdapter.startStopWatch()
        }

        if (tabToOpen == TAB_STOPWATCH) {
            config.toggleStopwatch = intent.getBooleanExtra(TOGGLE_STOPWATCH, false)
        }

        view_pager.offscreenPageLimit = TABS_COUNT - 1
        view_pager.currentItem = tabToOpen
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_AUDIO_FILE_INTENT_ID && resultCode == RESULT_OK && resultData != null) {
            storeNewAlarmSound(resultData)
        }
    }

    private fun storeNewAlarmSound(resultData: Intent) {
        val newAlarmSound = storeNewYourAlarmSound(resultData)

        when (view_pager.currentItem) {
            TAB_ALARM -> getViewPagerAdapter()?.updateAlarmTabAlarmSound(newAlarmSound)
            TAB_TIMER -> getViewPagerAdapter()?.updateTimerTabAlarmSound(newAlarmSound)
        }
    }


    private fun setupTabs() {
        main_tabs_holder.removeAllTabs()
        val tabDrawables = arrayOf(R.drawable.ic_alarm_usselect,R.drawable.ic_clock_vector,R.drawable.ic_timer_vector,R.drawable.ic_stopwatch_vector)
        val tabLabels = arrayOf( R.string.alarm,R.string.clock,R.string.timer, R.string.stopwatch )

        tabDrawables.forEachIndexed { i, drawableId ->
            main_tabs_holder.newTab().setCustomView(R.layout.bottom_tablayout).apply {
                customView?.findViewById<ImageView>(R.id.tab_item_icon)?.setImageDrawable(getDrawable(drawableId))
                customView?.findViewById<TextView>(R.id.tab_item_label)?.setText(tabLabels[i])
//                toolbar_title.setText(tabLabels[i])
                AutofitHelper.create(customView?.findViewById(R.id.tab_item_label))
                main_tabs_holder.addTab(this)
            }
        }

        main_tabs_holder.onTabSelectionChanged(
            tabUnselectedAction = {

                updateBottomTabItemColor(it.customView, false)
            },
            tabSelectedAction = {
                view_pager.currentItem = it.position
                updateBottomTabItemColor(it.customView, true)

            }
        )
    }

    private fun setupTabColors() {
        val activeView = main_tabs_holder.getTabAt(view_pager.currentItem)?.customView
        updateBottomTabItemColor(activeView, true)

        getInactiveTabIndexes(view_pager.currentItem).forEach { index ->
            val inactiveView = main_tabs_holder.getTabAt(index)?.customView
            updateBottomTabItemColor(inactiveView, false)
        }

        main_tabs_holder.getTabAt(view_pager.currentItem)?.select()
        val bottomBarColor = getBottomNavigationBackgroundColor()
//        main_tabs_holder.setBackgroundColor(bottomBarColor)
//        updateNavigationBarColor(bottomBarColor)
    }

    override fun onPause() {
        super.onPause()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        config.lastUsedViewPagerPage = view_pager.currentItem
    }

    private fun getInactiveTabIndexes(activeIndex: Int) = arrayListOf(0, 1, 2, 3).filter { it != activeIndex }

    fun updateClockTabAlarm() {
        getViewPagerAdapter()?.updateClockTabAlarm()
    }

    private fun getViewPagerAdapter() = view_pager.adapter as? ViewPagerAdapter
}