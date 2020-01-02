package com.aayush.what2do.view.activity

import android.os.Bundle
import androidx.fragment.app.commit
import com.aayush.what2do.R
import com.aayush.what2do.view.fragment.ReminderFragment
import kotlinx.android.synthetic.main.base_toolbar.*

class ReminderActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Reminder"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, ReminderFragment.newInstance())
        }
    }
}
