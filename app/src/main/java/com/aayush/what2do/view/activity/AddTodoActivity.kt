package com.aayush.what2do.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aayush.what2do.R
import com.aayush.what2do.view.fragment.AddTodoFragment
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.base_toolbar.*

class AddTodoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Add new todo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddTodoFragment.newInstance())
            .commit()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }
}
