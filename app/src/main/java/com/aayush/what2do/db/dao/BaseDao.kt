package com.aayush.what2do.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(obj: T)

    @Update(onConflict = OnConflictStrategy.REPLACE) fun update(obj: T)

    @Delete fun delete(obj: T)
}