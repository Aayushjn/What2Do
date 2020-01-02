package com.aayush.what2do.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aayush.what2do.db.dao.TodoNoteDao
import com.aayush.what2do.model.TodoNote

@Database(entities = [TodoNote::class], version = 1)
@TypeConverters(CustomTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun todoNoteDao(): TodoNoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "What2Do")
                .build().also { INSTANCE = it }
        }
    }
}