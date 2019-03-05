package com.aayush.what2do.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aayush.what2do.R
import com.aayush.what2do.view.fragment.ReminderFragment
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.base_toolbar.*

class ReminderActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Reminder"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ReminderFragment.newInstance())
            .commit()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }
}
