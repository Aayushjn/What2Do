package com.aayush.what2do

import android.app.Application
import android.content.Context
import com.aayush.what2do.db.AppDatabase
import com.aayush.what2do.util.NoLogTree
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
        }
        else {
            Timber.plant(NoLogTree())
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton design for the app database
        fun getAppDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = AppDatabase.getDatabase(context)
                INSTANCE = instance
                return instance
            }
        }
    }
}