package com.aayush.what2do.view.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.aayush.what2do.R
import com.aayush.what2do.util.android.toast
import com.aayush.what2do.util.android.toastLong
import com.aayush.what2do.util.common.PERMISSION_RECORD_AUDIO
import com.aayush.what2do.util.common.PERMISSION_SETTINGS_REQUEST
import com.aayush.what2do.util.common.TAG_PENDING_FRAGMENT
import com.aayush.what2do.view.fragment.AboutFragment
import com.aayush.what2do.view.fragment.PendingFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity: BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private var sentToSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        checkPermissions()

        supportFragmentManager.commit {
            replace(R.id.fragment_container, PendingFragment.newInstance(), TAG_PENDING_FRAGMENT)
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() = if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
        drawer_layout.closeDrawer(GravityCompat.START)
    } else {
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> true
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Need audio recording permission")
                        .setMessage("The app needs this permission to use speech recognition")
                        .setPositiveButton("Grant") { dialog, _ ->
                            dialog.cancel()
                            ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                PERMISSION_RECORD_AUDIO
                            )
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    builder.show()
                } else {
                    Toast.makeText(this, "Unable to get permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_SETTINGS_REQUEST) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume()

        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_pending -> PendingFragment.newInstance()
            R.id.nav_about -> AboutFragment.newInstance()
            else -> PendingFragment.newInstance()
        }
        supportFragmentManager.commit { replace(R.id.fragment_container, fragment) }

        title = item.title

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                        AlertDialog.Builder(this)
                            .setTitle("Need audio recording permission")
                            .setMessage("The app needs this permission to use speech recognition")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                    PERMISSION_RECORD_AUDIO
                                )
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                            .show()
                    }
                    sharedPreferences.getBoolean(Manifest.permission.RECORD_AUDIO, false) -> {
                        AlertDialog.Builder(this)
                            .setTitle("Need audio recording permission")
                            .setMessage("The app needs this permission to use speech recognition")
                            .setPositiveButton("Grant") { dialog, _ ->
                                dialog.cancel()
                                sentToSettings = true
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST)
                                this.toastLong("Go to permission to grant audio permission")
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                            .show()
                    }
                    else -> ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_RECORD_AUDIO
                    )
                }
            }
            sharedPreferences.edit()
                .putBoolean(Manifest.permission.RECORD_AUDIO, true)
                .apply()
        } else {
            proceedAfterPermission()
        }
    }

    private fun proceedAfterPermission() = this.toast("Permission granted")
}
