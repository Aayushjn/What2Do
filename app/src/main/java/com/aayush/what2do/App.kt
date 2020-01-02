package com.aayush.what2do

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.aayush.what2do.db.AppDatabase
import com.aayush.what2do.util.android.getNotificationManager
import com.aayush.what2do.util.common.CHANNEL_REMINDER
import com.aayush.what2do.util.common.CHANNEL_REMINDER_ID
import com.aayush.what2do.util.logging.ProdLogTree
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import timber.log.Timber
import timber.log.Timber.DebugTree

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // Setup for custom font injection
        ViewPump.init(ViewPump.builder()
            .addInterceptor(CalligraphyInterceptor(
                CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build())
            ).build()
        )

        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ProdLogTree())
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_REMINDER_ID, CHANNEL_REMINDER, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Reminders channel"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                setShowBadge(true)
                getNotificationManager().createNotificationChannel(this)
            }
        }
    }

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Singleton design for the app database
        fun getAppDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            AppDatabase.getDatabase(context).also { INSTANCE = it }
        }
    }
}