package com.aayush.what2do.db

import android.os.ParcelUuid
import androidx.room.TypeConverter
import com.aayush.what2do.model.Priority
import java.util.*

class CustomTypeConverters {
    @TypeConverter
    fun uuidFromString(value: String): ParcelUuid {
        return ParcelUuid.fromString(value)
    }

    @TypeConverter
    fun uuidToString(uuid: ParcelUuid): String {
        return uuid.toString()
    }

    @TypeConverter
    fun dateFromTimestamp(value: Long?): Date? {
        return if (value == null)
            null
        else
            Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun priorityFromString(value: Int): Priority {
        return when (value) {
            0 -> Priority.LOW
            1 -> Priority.MEDIUM
            else -> Priority.HIGH
        }
    }

    @TypeConverter
    fun priorityToString(value: Priority): Int {
        return value.priorityNumber
    }
}