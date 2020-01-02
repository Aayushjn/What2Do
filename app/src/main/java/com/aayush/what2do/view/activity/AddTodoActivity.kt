package com.aayush.what2do.view.activity

import android.os.Bundle
import androidx.fragment.app.commit
import com.aayush.what2do.R
import com.aayush.what2do.view.fragment.AddTodoFragment
import kotlinx.android.synthetic.main.base_toolbar.*

class AddTodoActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Add new todo"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        supportFragmentManager.commit {
            replace(R.id.fragment_container, AddTodoFragment.newInstance())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
