package com.aayush.what2do.view.activity

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.aayush.what2do.R
import com.aayush.what2do.util.TAG_PENDING_FRAGMENT
import com.aayush.what2do.view.fragment.AboutFragment
import com.aayush.what2do.view.fragment.PendingFragment
import com.google.android.material.navigation.NavigationView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import timber.log.Timber

class MainActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PendingFragment.newInstance(), TAG_PENDING_FRAGMENT)
            .commit()

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment = PendingFragment.newInstance()
        val fragmentClass: Class<out Fragment> = when (item.itemId) {
            R.id.nav_pending -> {
                PendingFragment::class.java
            }
            R.id.nav_about -> {
                AboutFragment::class.java
            }
            else -> {
                PendingFragment::class.java
            }
        }

        try {
            fragment = fragmentClass.newInstance()
        }
        catch (e : IllegalAccessException) {
            Timber.w(e, "Access to method denied")
        }
        catch (e : InstantiationException) {
            Timber.w(e, "Class cannot be instantiated")
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        title = item.title

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
